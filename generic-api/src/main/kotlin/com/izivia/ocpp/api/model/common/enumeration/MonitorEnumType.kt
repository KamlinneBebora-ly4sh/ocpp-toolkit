package com.izivia.ocpp.api.model.common.enumeration

enum class MonitorEnumType(val value: String) {
    UpperThreshold("UpperThreshold"),

    LowerThreshold("LowerThreshold"),

    Delta("Delta"),

    Periodic("Periodic"),

    PeriodicClockAligned("PeriodicClockAligned")
}
