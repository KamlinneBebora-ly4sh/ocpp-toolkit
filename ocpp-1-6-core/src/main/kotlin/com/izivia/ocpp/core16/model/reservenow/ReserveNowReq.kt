package com.izivia.ocpp.core16.model.reservenow

import com.izivia.ocpp.core16.model.Request
import kotlinx.datetime.Instant

data class ReserveNowReq(
    val connectorId: Int,
    val expiryDate: Instant,
    val idTag: String,
    val reservationId: Int,
    val parentIdTag: String? = null
) : Request
