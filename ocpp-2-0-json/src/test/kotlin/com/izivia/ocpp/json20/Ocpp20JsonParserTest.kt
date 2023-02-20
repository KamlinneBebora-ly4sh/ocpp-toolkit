package com.izivia.ocpp.json20

import com.izivia.ocpp.core20.model.bootnotification.BootNotificationResp
import com.izivia.ocpp.json.parseFromJson
import com.izivia.ocpp.utils.MessageErrorCode
import com.izivia.ocpp.utils.fault.Fault
import com.networknt.schema.ValidatorTypeCode
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.*
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class Ocpp20JsonParserTest {

    companion object {
        val parser = Ocpp20JsonParser()
    }

    @Test
    fun `should parse Hearbeat request`() {
        val request = """[2,"messageId","Heartbeat",{}]"""

        expectThat(parser.parseAnyFromString<Unit>(request))
            .get { action }.isEqualTo("Heartbeat")
    }

    @Test
    fun `should parse to Fault PROTOCOL_ERROR inconsistent request`() {
        val request = """[2,"messageId","TransactionEvent",{"meterStart": 0,
            |"timestamp": "2022-08-03T11:00:01.916Z", "idTag": "idTag"}]
        """.trimMargin()

        val req = parser.parseAnyFromString<Unit>(request)
        expectThat(req)
            .and {
                get { action }.isEqualTo("Fault")
                get { errorCode }.isEqualTo(MessageErrorCode.PROTOCOL_ERROR)
                get { payload }.isA<Fault>()
                    .and {
                        get { errorCode }.isEqualTo(MessageErrorCode.PROTOCOL_ERROR.errorCode)
                        get { errorDescription }.isEqualTo(MessageErrorCode.PROTOCOL_ERROR.description)
                        get { errorDetails }.hasSize(8)
                            .and {
                                get { get(0) }
                                    .and {
                                        get { code }.isEqualTo("action")
                                        get { detail }.isEqualTo("TransactionEvent")
                                    }
                                get { get(1) }
                                    .and {
                                        get { code }.isEqualTo("message")
                                        get { detail }.isEqualTo(request)
                                    }
                                get { get(2) }
                                    .and {
                                        get { code }.isEqualTo("1028")
                                        get { detail }.contains("Validations error")
                                    }
                                get { get(4) }
                                    .and {
                                        get { code }.isEqualTo("1028")
                                        get { detail }.contains("Validations error")
                                    }
                                get { get(5) }
                                    .and {
                                        get { code }.isEqualTo("1028")
                                        get { detail }.contains("Validations error")
                                    }
                                get { get(6) }
                                    .and {
                                        get { code }.isEqualTo("1001")
                                        get { detail }.contains("Validations error")
                                    }
                                get { get(7) }
                                    .and {
                                        get { code }.isEqualTo("1001")
                                        get { detail }.contains("Validations error")
                                    }
                            }
                    }
            }
    }

    @Test
    fun `should parse to Fault MESSAGE_TYPE_NOT_SUPPORTED inconsistent request`() {
        val request = """[15,"messageId","Heartbeat",{}]""".trimMargin()

        val req = parser.parseAnyFromString<Unit>(request)
        expectThat(req)
            .and {
                get { action }.isEqualTo("Fault")
                get { errorCode }.isEqualTo(MessageErrorCode.MESSAGE_TYPE_NOT_SUPPORTED)
                get { payload }.isA<Fault>()
                    .and {
                        get { errorCode }.isEqualTo(MessageErrorCode.MESSAGE_TYPE_NOT_SUPPORTED.errorCode)
                        get { errorDescription }.isEqualTo(MessageErrorCode.MESSAGE_TYPE_NOT_SUPPORTED.description)
                        get { errorDetails }.hasSize(1)
                            .and {
                                get { get(0) }
                                    .and {
                                        get { code }.isEqualTo("message")
                                        get { detail }.isEqualTo(request)
                                    }
                            }
                    }
            }
    }

    @Test
    fun `should parse to Fault NOT_IMPLEMENTED inconsistent request`() {
        val request = """[2,"messageId","NotAnAction",{}]""".trimMargin()

        val req = parser.parseAnyFromString<Unit>(request)
        expectThat(req)
            .and {
                get { action }.isEqualTo("Fault")
                get { errorCode }.isEqualTo(MessageErrorCode.NOT_IMPLEMENTED)
                get { payload }.isA<Fault>()
                    .and {
                        get { errorCode }.isEqualTo(MessageErrorCode.NOT_IMPLEMENTED.errorCode)
                        get { errorDescription }.isEqualTo(MessageErrorCode.NOT_IMPLEMENTED.description)
                        get { errorDetails }.hasSize(2)
                            .and {
                                get { get(0) }
                                    .and {
                                        get { code }.isEqualTo("message")
                                        get { detail }.isEqualTo(request)
                                    }
                                get { get(1) }
                                    .and {
                                        get { code }.isEqualTo("action")
                                        get { detail }.isEqualTo("NotAnAction")
                                    }
                            }
                    }
            }
    }

    @Test
    fun `should parse to Fault FORMAT_VIOLATION inconsistent request`() {
        val request = """{"Json": "Ok", "Ocpp": "not}""".trimMargin()

        val req = parser.parseAnyFromString<Unit>(request)
        expectThat(req)
            .and {
                get { action }.isEqualTo("Fault")
                get { errorCode }.isEqualTo(MessageErrorCode.FORMAT_VIOLATION)
                get { payload }.isA<Fault>()
                    .and {
                        get { errorCode }.isEqualTo(MessageErrorCode.FORMAT_VIOLATION.errorCode)
                        get { errorDescription }.isEqualTo(MessageErrorCode.FORMAT_VIOLATION.description)
                        get { errorDetails }.hasSize(1)
                            .and {
                                get { get(0) }
                                    .and {
                                        get { code }.isEqualTo("message")
                                        get { detail }.isEqualTo(request)
                                    }
                            }
                    }
            }
    }

    @Test
    fun `should parse to Fault BOOTNOT inconsistent request`() {
        val request = """[2,"messageId","BootNotification",
            {
  "reason": "ApplicationReset",
  "chargingStation": {
    "model": "ABCDEF",
    "vendorName": "ABCDEFGHIJKLMNOPQRSTUVWXYZA",
    "customData": {
      "vendorId": "ABCDEFGHIJKLMNOPQR"
    },
    "serialNumber": "1234567891011121314151617181920",
    "modem": {
      "customData": {
        "vendorId": "ABCDEFGHIJKL"
      },
      "iccid": "ABCDEFGHIJKLM",
      "imsi": "ABCDEFGHIJKLMNOPQ"
    },
    "firmwareVersion": "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
  },
  "customData": {
    "vendorId": "ABCDEFGHIJKLMNOPQR"
  }
}]
        """.trimMargin()

        val req = parser.parseAnyFromString<Unit>(request)
        expectThat(req)
            .and {
                get { action }.isEqualTo("Fault")
                get { errorCode }.isEqualTo(MessageErrorCode.PROTOCOL_ERROR)
                get { payload }.isA<Fault>()
                    .and {
                        get { errorCode }.isEqualTo(MessageErrorCode.PROTOCOL_ERROR.errorCode)
                        get { errorDescription }.isEqualTo(MessageErrorCode.PROTOCOL_ERROR.description)
                        get { errorDetails }.hasSize(3)
                            .and {
                                get { get(0) }
                                    .and {
                                        get { code }.isEqualTo("action")
                                        get { detail }.isEqualTo("BootNotification")
                                    }
                                get { get(1) }
                                    .and {
                                        get { code }.isEqualTo("message")
                                        get { detail }.isEqualTo(request)
                                    }
                                get { get(2) }
                                    .and {
                                        get { code }.isEqualTo("1013")
                                        get { detail }.contains("Validations error")
                                    }
                            }
                    }
            }
    }

    @Test
    fun `should parse to BootNotification on ignoring 1013 inconsistent request`() {
        val ocppIgnore1013 = Ocpp20JsonParser(ignoreValidationCodes = listOf(ValidatorTypeCode.MAX_LENGTH))

        val request = """[2,"messageId","BootNotification",
            {
  "reason": "ApplicationReset",
  "chargingStation": {
    "model": "ABCDEF",
    "vendorName": "ABCDEFGHIJKLMNOPQRSTUVWXYZA",
    "customData": {
      "vendorId": "ABCDEFGHIJKLMNOPQR"
    },
    "serialNumber": "1234567891011121314151617181920",
    "modem": {
      "customData": {
        "vendorId": "ABCDEFGHIJKL"
      },
      "iccid": "ABCDEFGHIJKLM",
      "imsi": "ABCDEFGHIJKLMNOPQ"
    },
    "firmwareVersion": "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
  },
  "customData": {
    "vendorId": "ABCDEFGHIJKLMNOPQR"
  }
}]
        """.trimMargin()

        val req = ocppIgnore1013.parseAnyFromString<Unit>(request)
        expectThat(req)
            .and {
                get { action }.isEqualTo("BootNotification")
            }
    }

    @Test
    fun `should parse to BootNotification on ignoring additional properties request`() {
        val ocppIgnore = Ocpp20JsonParser(ignoreValidationCodes = listOf(ValidatorTypeCode.ADDITIONAL_PROPERTIES))

        val request = """[2,"messageId","BootNotification",
            {
  "reason": "ApplicationReset",
  "additional": "properties",
  "chargingStation": {
    "model": "ABCDEF",
    "vendorName": "ABCDEFGHIJKLMNOPQRSTUVWXYZA",
    "customData": {
      "vendorId": "ABCDEFGHIJKLMNOPQR"
    },
    "serialNumber": "12345678910111",
    "modem": {
      "customData": {
        "vendorId": "ABCDEFGHIJKL"
      },
      "iccid": "ABCDEFGHIJKLM",
      "imsi": "ABCDEFGHIJKLMNOPQ"
    },
    "firmwareVersion": "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
  },
  "customData": {
    "vendorId": "ABCDEFGHIJKLMNOPQR"
  }
}]
        """.trimMargin()

        val req = ocppIgnore.parseAnyFromString<Unit>(request)
        expectThat(req)
            .and {
                get { action }.isEqualTo("BootNotification")
            }
    }

    @Test
    fun `should parse to BootNotificationResp`() {
        val ocppIgnore = Ocpp20JsonParser(enableValidation = false)

        val jsonRequest = """[3,"messageId",
            |{"currentTime":"2022-07-21T12:00:00Z","interval":1800,"status":"Accepted"}]
        """.trimMargin()

        val req = ocppIgnore.parseFromJson<BootNotificationResp>(jsonRequest)
        expectThat(req)
            .and {
                get { action }.isEqualTo("bootNotification")
            }
    }

    @Test
    fun `perf with and without validation`() {
        val request1 = """[2,"messageId","Heartbeat",{}]"""
        val request2 = """[15,"messageId","Heartbeat",{}]""".trimMargin()
        val request3 = """[2,"messageId","BootNotification",
            {"chargePointModel": "testModel", "chargePointVendor": "testVendor",
            "chargePointSerialNumber":"1234567891011"}]
        """.trimMargin()
        val request4 = """[2,"messageId","NotAnAction",{}]""".trimMargin()
        val request5 = """{"Json": "Ok", "Ocpp": "not}""".trimMargin()
        val request6 = """[2,"messageId","BootNotification",
            {
  "reason": "ApplicationReset",
  "chargingStation": {
    "model": "ABCDEF",
    "vendorName": "ABCDEFGHIJKLMNOPQRSTUVWXYZA",
    "customData": {
      "vendorId": "ABCDEFGHIJKLMNOPQR"
    },
    "serialNumber": "1234567891011121314151617181920",
    "modem": {
      "customData": {
        "vendorId": "ABCDEFGHIJKL"
      },
      "iccid": "ABCDEFGHIJKLM",
      "imsi": "ABCDEFGHIJKLMNOPQ"
    },
    "firmwareVersion": "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
  },
  "customData": {
    "vendorId": "ABCDEFGHIJKLMNOPQR"
  }
}]
        """.trimMargin()
        val request7 = """[2,"messageId","TransactionEvent",{"meterStart": 0,
            |"timestamp": "2022-08-03T11:00:01.916Z", "idTag": "idTag"}]
        """.trimMargin()

        val requestList = listOf(request1, request2, request3, request4, request5, request6, request7)

        val parserWithoutValidation = Ocpp20JsonParser(enableValidation = false)
        val parserWithValidation = Ocpp20JsonParser()

        // warn up JIT
        timeit(requestList, parserWithoutValidation)
        timeit(requestList, parserWithValidation)

        // measure time
        val elapsedWithoutValidation = timeit(requestList, parserWithoutValidation)

        // measure time
        val elapsedWithValidation = timeit(requestList, parserWithValidation)

        val percent = 100 - ((elapsedWithoutValidation / elapsedWithValidation) * 100)

        println(
            "elapsed time : with validation $elapsedWithValidation, " +
                "without validation $elapsedWithoutValidation, $percent % faster without validation"
        )

        // disabled because running tests in parallel can overload CPU and lead to unexpected behavior and failed
        // expectThat(percent).isGreaterThan(0.0)
    }

    @OptIn(ExperimentalTime::class)
    fun timeit(requestList: List<String>, parser: Ocpp20JsonParser): Duration {
        return measureTime {
            for (i in 1..10000) {
                parser.parseAnyFromString<Unit>(
                    requestList.get(i % requestList.size).replace("messageId", i.toString())
                )
            }
        }
    }
}
