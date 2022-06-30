package com.izivia.ocpp.core15.model.bootnotification

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.izivia.ocpp.core15.model.bootnotification.enumeration.RegistrationStatus
import com.izivia.ocpp.utils.InstantDeserializer
import com.izivia.ocpp.utils.InstantSerializer
import kotlinx.datetime.Instant

data class BootNotificationResp(
    @JsonSerialize(using = InstantSerializer::class)
    @JsonDeserialize(using = InstantDeserializer::class)
    val currentTime: Instant,
    val heartbeatInterval: Int,
    val status: RegistrationStatus
)