package com.izivia.ocpp.core16.model.bootnotification

import com.izivia.ocpp.core16.model.Request

data class BootNotificationReq(
    val chargePointModel: String,
    val chargePointVendor: String,
    val chargePointSerialNumber: String? = null,
    val chargeBoxSerialNumber: String? = null,
    val firmwareVersion: String? = null,
    val iccid: String? = null,
    val imsi: String? = null,
    val meterSerialNumber: String? = null,
    val meterType: String? = null
) : Request
