package com.izivia.ocpp.utils

import kotlinx.datetime.Instant

/**
 * Interface implemented by OCPP request containing a timestamp field in its payload.
 * @property timestamp : the date and time at which this action occurred
 */
interface HasActionTimestamp {

    val timestamp: Instant?

}