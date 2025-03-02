package com.izivia.ocpp.core20.model.transactionevent

import com.izivia.ocpp.core20.model.common.EVSEType
import com.izivia.ocpp.core20.model.common.IdTokenType
import com.izivia.ocpp.core20.model.common.MeterValueType
import com.izivia.ocpp.core20.model.transactionevent.enumeration.TransactionEventEnumType
import com.izivia.ocpp.core20.model.transactionevent.enumeration.TriggerReasonEnumType
import com.izivia.ocpp.utils.HasActionTimestamp
import kotlinx.datetime.Instant

data class TransactionEventReq(
    val eventType: TransactionEventEnumType,
    override val timestamp: Instant,
    val triggerReason: TriggerReasonEnumType,
    val seqNo: Int,
    val transactionInfo: TransactionType,
    val cableMaxCurrent: Int? = null,
    val evse: EVSEType? = null,
    val idToken: IdTokenType? = null,
    val meterValue: List<MeterValueType>? = null,
    val numberOfPhasesUsed: Int? = null,
    val offline: Boolean? = false,
    val reservationId: Int? = null
) : HasActionTimestamp