package com.izivia.ocpp.core15.model.stoptransaction

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.izivia.ocpp.core15.model.common.MeterValue
import com.izivia.ocpp.utils.InstantDeserializer
import com.izivia.ocpp.utils.InstantSerializer
import kotlinx.datetime.Instant

data class StopTransactionReq(
    val idTag: String? = null,
    val meterStop: Int,
    @JsonSerialize(using = InstantSerializer::class)
    @JsonDeserialize(using = InstantDeserializer::class)
    val timestamp: Instant,
    val transactionId: Int,
    val transactionData: List<MeterValue>? = null
)