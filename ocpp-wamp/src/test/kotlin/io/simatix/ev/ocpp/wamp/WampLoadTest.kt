package io.simatix.ev.ocpp.wamp

import io.simatix.ev.ocpp.CSOcppId
import io.simatix.ev.ocpp.OcppVersion
import io.simatix.ev.ocpp.wamp.client.OcppWampClient
import io.simatix.ev.ocpp.wamp.messages.WampMessage
import io.simatix.ev.ocpp.wamp.messages.WampMessageMeta
import io.simatix.ev.ocpp.wamp.messages.WampMessageType
import io.simatix.ev.ocpp.wamp.server.OcppWampServer
import io.simatix.ev.ocpp.wamp.server.OcppWampServerHandler
import kotlinx.datetime.Clock
import org.http4k.core.Uri
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class WampLoadTest {
    @Test
    fun `should handle 1k clients`() {
        val chargingStationNumber = 1000
        val heartbeatsNumber = 10000
        val threadsNumber = 50

        println("preparing load test on one server serving $chargingStationNumber charging stations and $heartbeatsNumber heartbeats")

        val port = 12345
        val server = OcppWampServer.newServer(port, setOf(OcppVersion.OCPP_1_6, OcppVersion.OCPP_2_0))
        server.register(object : OcppWampServerHandler {
            override fun accept(ocppId: CSOcppId): Boolean = true

            override fun onAction(meta: WampMessageMeta, msg: WampMessage): WampMessage? =
                when (msg.action?.lowercase()) {
                    "heartbeat" ->
                        WampMessage.CallResult(msg.msgId, """{"currentTime":"${Clock.System.now()}"}""")
                    else -> {
                        println("unhandled action for message: ${msg.toJson()}")
                        WampMessage.CallError(msg.msgId, "{}")
                    }
                }
        })
        server.start()

        try {
            val executor = Executors.newFixedThreadPool(threadsNumber)
            val connections = ConcurrentHashMap<CSOcppId, OcppWampClient>()
            val ocppIds = (1..chargingStationNumber).map { "CS-$it" }
            val connectedCount = AtomicInteger(0)
            val heartbeatCount = AtomicInteger(0)
            val closedCount = AtomicInteger(0)
            val getClient = { ocppId:CSOcppId ->
                var client = connections[ocppId]
                var attempts = 10
                while (client == null && attempts > 0) {
                    Thread.sleep(20)
                    client = connections[ocppId]
                    attempts--
                }
                client?:throw IllegalStateException("connection $ocppId not available")
            }

            val connectionLatch = CountDownLatch(chargingStationNumber)
            ocppIds.forEach { ocppId ->
                executor.submit {
                    val client = OcppWampClient.newClient(
                        Uri.of("ws://localhost:$port/ws"), ocppId, OcppVersion.OCPP_1_6, 20_000)
                    client.connect()
                    connections[ocppId] = client
                    connectionLatch.countDown()
                    connectedCount.incrementAndGet()
                    println("opened $connectedCount connections")
                }
            }
            connectionLatch.await(1, TimeUnit.MINUTES)
            val start = Clock.System.now()
            val heartbeatLatch = CountDownLatch(heartbeatsNumber)
            (1..heartbeatsNumber).forEach {
                val ocppId = ocppIds.random()
                executor.submit {
                    try {
                        val client = getClient(ocppId)
                        val r = client.sendBlocking(WampMessage.Call(UUID.randomUUID().toString(), "Heartbeat", "{}"))
                        expectThat(r) {
                            get { msgType }.isEqualTo(WampMessageType.CALL_RESULT)
                        }
                        heartbeatCount.incrementAndGet()
                    } catch (e:Exception) {
                        System.err.println("error when sending heartbeat: $e")
                    } finally {
                        heartbeatLatch.countDown()
                        println("${heartbeatLatch.count} remaining heartbeats")
                    }
                }
            }
            if (heartbeatLatch.await(1, TimeUnit.MINUTES)) {
                println("all heartbeats done")
            } else {
                println("timeout waiting for heartbeats - remaining ${heartbeatLatch.count}")
            }
            val elapsed = Clock.System.now() - start

            ocppIds.forEach { ocppId ->
                executor.submit {
                    val client = getClient(ocppId)
                    client.close()
                    closedCount.incrementAndGet()
                    println("closed $closedCount connections")
                }
            }
            executor.shutdown()
            println("awaiting termination...")
            executor.awaitTermination(1, TimeUnit.MINUTES)

            expectThat(connectedCount.get()).isEqualTo(chargingStationNumber)
            expectThat(heartbeatCount.get()).isEqualTo(heartbeatsNumber)
            expectThat(closedCount.get()).isEqualTo(chargingStationNumber)

            server.shutdown()

            val heartbeatPerSecond = (heartbeatsNumber.toDouble() / elapsed.inWholeMilliseconds) * 1000
            println(
                """
                    -------------------------------------------------------------------------------------------
                    -LOAD TEST SUCCESSFUL
                     heartbeats:            $heartbeatsNumber  
                     charging stations:     $chargingStationNumber 
                     duration:              $elapsed
                     req per second:        $heartbeatPerSecond/s
                    -------------------------------------------------------------------------------------------                    
                """.trimIndent())
        } finally {
            server.stop()
        }
    }
}
