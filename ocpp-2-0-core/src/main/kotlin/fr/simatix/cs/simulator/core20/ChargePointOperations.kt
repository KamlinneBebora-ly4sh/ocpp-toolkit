package fr.simatix.cs.simulator.core20

import fr.simatix.cs.simulator.api.model.RequestMetadata
import fr.simatix.cs.simulator.core20.impl.RealChargePointOperations
import fr.simatix.cs.simulator.core20.model.*
import fr.simatix.cs.simulator.transport.Transport
import java.net.ConnectException

/**
 * Operations initiated by Charge Point
 */
interface ChargePointOperations {
    companion object {
        fun newChargePointOperations(transport: Transport) = RealChargePointOperations(transport)
    }

    /**
     * Sends a Heartbeat request to let the Central System know the Charge Point is still connected
     */
    @Throws(IllegalStateException::class, ConnectException::class)
    fun heartbeat(meta: RequestMetadata, request: HeartbeatReq): CoreExecution<HeartbeatResp>

    /**
     * Sends an Authorize request to the Central System if the identifier is authorize to start/stop
     * to charge.
     */
    @Throws(IllegalStateException::class, ConnectException::class)
    fun authorize(meta: RequestMetadata, request: AuthorizeReq): CoreExecution<AuthorizeResp>

    /**
     * Sends periodic, possibly clock-aligned MeterValues
     */
    @Throws(IllegalStateException::class, ConnectException::class)
    fun meterValues(meta: RequestMetadata, request: MeterValuesReq): CoreExecution<MeterValuesResp>

    /**
     * Sends information about a transaction event (started, updated, ended)
     */
    @Throws(IllegalStateException::class, ConnectException::class)
    fun transactionEvent(meta: RequestMetadata, request: TransactionEventReq): CoreExecution<TransactionEventResp>

    /**
     * Sends a notification to the Central System about a status change or an error within a connector
     */
    @Throws(IllegalStateException::class, ConnectException::class)
    fun statusNotification(meta: RequestMetadata, request: StatusNotificationReq): CoreExecution<StatusNotificationResp>
}