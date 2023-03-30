package com.izivia.ocpp.core16.model.securityeventnotification

import com.izivia.ocpp.utils.HasActionTimestamp
import kotlinx.datetime.Instant

data class SecurityEventNotificationReq(
    val type: String,
    override val timestamp: Instant,
    val techInfo: String? = null
) : HasActionTimestamp
