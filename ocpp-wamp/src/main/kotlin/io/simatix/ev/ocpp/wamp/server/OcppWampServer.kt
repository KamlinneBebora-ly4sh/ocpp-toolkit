package io.simatix.ev.ocpp.wamp.server

import io.simatix.ev.ocpp.CSOcppId
import io.simatix.ev.ocpp.OcppVersion
import io.simatix.ev.ocpp.wamp.messages.WampMessage
import io.simatix.ev.ocpp.wamp.messages.WampMessageMeta
import io.simatix.ev.ocpp.wamp.server.impl.UndertowOcppWampServer

interface OcppWampServer {
    /**
     * Starts the wamp server.
     */
    fun start()

    /**
     * attempt a graceful shutdown of the server:
     * - do not accept connection any more
     * - do not accept new calls any more
     * - close opened connections when pending calls are done
     */
    fun shutdown()

    /**
     * Stops the server.
     * This will close all opened web sockets.
     * A stopped server cannot be reused.
     */
    fun stop()

    /**
     * Sends a WampMessage call to a ChargingStation, identified by its ocpp id.
     *
     * @throws IllegalStateException if no such ChargingStation is currently connected to this server
     */
    fun sendBlocking(ocppId:CSOcppId, message:WampMessage): WampMessage

    /**
     * registers a wamp server handler on this server.
     *
     * It is used to check if ocpp id is accepted, and then to handle actions if charging station has been accepted by the handler.
     *
     * The checks are iterated in order, the first returning true will be used to handle the charging station.
     * if no handler accepts by returning true boolean, the connection is refused.
     *
     * The handler which accepted the charging station is later used for action handling.
     *
     * Note that an handler cannot be unregistered.
     */
    fun register(handler: OcppWampServerHandler)

    companion object {
        fun newServer(port:Int, ocppVersions:Set<OcppVersion> = OcppVersion.values().toSet(), timeoutInMs:Long = 30_000)
            = UndertowOcppWampServer(port, ocppVersions, timeoutInMs)
    }
}

interface OcppWampServerHandler {
    /**
     * check for ocpp id existence
     *
     * this is used by server to accept connection for a given ocpp id.
     */
    fun accept(ocppId: CSOcppId): Boolean
    /**
     * a wamp action handler
     */
    fun onAction(meta: WampMessageMeta, msg: WampMessage): WampMessage?
}
