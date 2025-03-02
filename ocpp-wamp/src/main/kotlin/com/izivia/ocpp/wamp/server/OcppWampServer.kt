package com.izivia.ocpp.wamp.server

import com.izivia.ocpp.CSOcppId
import com.izivia.ocpp.OcppVersion
import com.izivia.ocpp.wamp.messages.WampMessage
import com.izivia.ocpp.wamp.messages.WampMessageMeta
import com.izivia.ocpp.wamp.server.impl.EventsListeners
import com.izivia.ocpp.wamp.server.impl.OcppWampServerSettings
import com.izivia.ocpp.wamp.server.impl.UndertowOcppWampServer

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
     * @param timeoutInMs if not specified, the timeout from settings will be used
     * @throws IllegalStateException if no such ChargingStation is currently connected to this server
     */
    fun sendBlocking(ocppId: CSOcppId, message: WampMessage, timeoutInMs: Long? = null): WampMessage

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

    /**
     * Get the charging station ocpp version from the ocpp id
     * @throws IllegalStateException if no such ChargingStation is currently connected to this server
     */
    fun getChargingStationOcppVersion(ocppId: CSOcppId): OcppVersion

    companion object {
        fun newServer(
            port: Int,
            ocppVersions: Set<OcppVersion> = OcppVersion.values().toSet(),
            path: String = "ws",
            settings: OcppWampServerSettings = OcppWampServerSettings(),
            listeners: EventsListeners = EventsListeners()
        ) = UndertowOcppWampServer(port, ocppVersions, path, settings, listeners)
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
