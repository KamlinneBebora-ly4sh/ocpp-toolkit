package fr.simatix.cs.simulator.core20.impl

import fr.simatix.cs.simulator.core20.ChargePointOperations
import fr.simatix.cs.simulator.core20.model.HeartbeatRequest
import fr.simatix.cs.simulator.core20.model.HeartbeatResponse
import fr.simatix.cs.simulator.transport.Transport
import java.net.ConnectException

class ChargePointOperationsImpl(private val client: Transport) : ChargePointOperations {

    @Throws(IllegalStateException::class, ConnectException::class)
    override fun heartbeat(request: HeartbeatRequest): HeartbeatResponse {
        return client.sendMessage(HeartbeatResponse::class, "Heartbeat", request)
    }
}
