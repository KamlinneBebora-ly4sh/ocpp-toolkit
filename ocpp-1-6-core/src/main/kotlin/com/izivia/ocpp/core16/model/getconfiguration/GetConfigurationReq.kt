package com.izivia.ocpp.core16.model.getconfiguration

import com.izivia.ocpp.core16.model.Request

data class GetConfigurationReq(
    val key: List<String>? = null
) : Request
