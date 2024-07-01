package com.izivia.ocpp.core16.model.getdiagnostics

import com.izivia.ocpp.core16.model.Request
import kotlinx.datetime.Instant

data class GetDiagnosticsReq(
    val location: String,
    val retries: Int? = null,
    val retryInterval: Int? = null,
    val startTime: Instant? = null,
    val stopTime: Instant? = null
) : Request
