package com.izivia.ocpp.wamp.messages

import com.izivia.ocpp.CSOcppId
import com.izivia.ocpp.OcppVersion
import com.izivia.ocpp.utils.MessageErrorCode
import com.izivia.ocpp.wamp.messages.WampMessageType.*
import org.slf4j.LoggerFactory
import kotlin.text.RegexOption.DOT_MATCHES_ALL

data class WampMessage(
    val msgType: WampMessageType,
    val msgId: String,
    val action: String? = null,
    val errorCode: MessageErrorCode? = null,
    val errorDescription: String? = null,
    val payload: String
) {

    fun toJson(): String = when (msgType) {
        CALL -> """[${msgType.id},"$msgId","${action!!}",$payload]"""
        CALL_RESULT -> """[${msgType.id},"$msgId",$payload]"""
        CALL_ERROR -> """[${msgType.id},"$msgId","${errorCode!!.errorCode}","${errorDescription!!}",$payload]"""
    }

    companion object {
        fun Call(msgId: String, action: String, payload: String) =
            WampMessage(
                msgType = CALL,
                msgId = msgId,
                action = action,
                payload = payload
            )

        fun CallResult(msgId: String, payload: String) =
            WampMessage(
                msgType = CALL_RESULT,
                msgId = msgId,
                payload = payload
            )

        fun CallError(msgId: String, errorCode: MessageErrorCode, errorDescription: String, payload: String) =
            WampMessage(
                msgType = CALL_ERROR,
                msgId = msgId,
                errorCode = errorCode,
                errorDescription = errorDescription,
                payload = payload
            )

        fun parse(str: String) = WampMessageParser.parse(str)
    }
}

object WampMessageParser {

    private val logger = LoggerFactory.getLogger(WampMessageParser::class.java)

    private fun String.formatWampMessage(): String =
        this.removeBracketsAndTrim()
            .removeSingleDoubleQuoteInPayload()

    private fun String.removeBracketsAndTrim(): String = this.trim().removePrefix("[").removeSuffix("]")

    fun String.removeSingleDoubleQuoteInPayload(): String {
        this.indexOf("{", 0).let { indexOfFirstBrace ->
            if (indexOfFirstBrace != -1) {
                return this.substring(0, indexOfFirstBrace)
                    .plus(
                        this.substring(indexOfFirstBrace).let { str ->
                            str.takeIf { it.count { it == '"' } == 1 }?.replace("\"", "") ?: str
                        }
                    ).trim()
            } else {
                logger.error("No brace found from WampMessqages $this")
                return this
            }
        }
    }

    object Patterns {
        const val CALL_TYPE = """(\d)\s*""" // call type is a single digit
        const val MSG_ID = """\s*"?([^,"]+)"?\s*""" // anything except comma or quote, maybe surrounded by quotes
        const val ACTION = """\s*"?([^,"]+)"?\s*""" // anything except comma or quote, maybe surrounded by quotes
        const val ERROR_CODE = """\s*"?([^,"]+)"?\s*""" // anything except comma or quote, maybe surrounded by quotes
        const val PAYLOAD = """\s*(.*)""" // anything

        const val ERROR_DESCRIPTION = """\s*(?:"([^"]*)"|([^"][^,]*))\s*"""
            // either anything but quote surrounded by quotes,
            // or anything but a comma
    }

    private val ocppMsgRegexTypeCall = Regex(
        listOf(Patterns.CALL_TYPE, Patterns.MSG_ID, Patterns.ACTION, Patterns.PAYLOAD).joinToString(","),
        DOT_MATCHES_ALL
    )
    private val ocppMsgRegexTypeCallResult = Regex(
        listOf(Patterns.CALL_TYPE, Patterns.MSG_ID, Patterns.PAYLOAD).joinToString(","),
        DOT_MATCHES_ALL
    )
    private val ocppMsgRegexTypeCallError = Regex(
        listOf(Patterns.CALL_TYPE, Patterns.MSG_ID, Patterns.ERROR_CODE, Patterns.ERROR_DESCRIPTION, Patterns.PAYLOAD)
            .joinToString(","),
        DOT_MATCHES_ALL
    )

    fun parse(msg: String): WampMessage? {
        logger.debug("Trying to parse message: {}", msg)
        when (msg) {
            null, "{}" -> return null
            else -> {
                try {
                    val formattedMsg = msg.formatWampMessage()
                    when (WampMessageType.fromId(formattedMsg.split(",")[0].toInt())) {
                        CALL -> {
                            ocppMsgRegexTypeCall.matchEntire(formattedMsg)?.let { matchResult ->
                                return matchResult.destructured.let {
                                    it.let { (_, msgId, action, payload) ->
                                        WampMessage.Call(
                                            msgId.trimNewLines(),
                                            action.trimNewLines(),
                                            payload.trimNewLines()
                                        )
                                    }
                                }
                            }
                            logger.error(
                                "WampMessage CALL doesn't match to the expected format" +
                                    " msg=$msg (formatted msg=`$formattedMsg`)"
                            )
                        }

                        CALL_RESULT -> {
                            ocppMsgRegexTypeCallResult.matchEntire(formattedMsg)?.let { matchResult ->
                                return matchResult.destructured.let {
                                    it.let { (_, msgId, payload) ->
                                        WampMessage.CallResult(
                                            msgId.trimNewLines(),
                                            payload.trimNewLines()
                                        )
                                    }
                                }
                            }
                            logger.error(
                                "WampMessage CALL_RESULT doesn't match to the expected format" +
                                    " msg=$msg (formatted msg=`$formattedMsg`)"
                            )
                        }

                        CALL_ERROR -> {
                            ocppMsgRegexTypeCallError.matchEntire(formattedMsg)?.let { matchResult ->
                                return matchResult.destructured.let {
                                    it.let { (_, msgId, errorCode, errorDescription1, errorDescription2, payload) ->
                                        WampMessage.CallError(
                                            msgId.trimNewLines(),
                                            MessageErrorCode.fromValue(errorCode.trimNewLines()),
                                            // either description 1 or 2 will be filled, it's an OR between the two
                                            errorDescription1.trimNewLines() + errorDescription2.trimNewLines(),
                                            payload.trimNewLines()
                                        )
                                    }
                                }
                            }
                            logger.error(
                                "WampMessage CALL_ERROR doesn't match to the expected format" +
                                    " msg=$msg (formatted msg=`$formattedMsg`)"
                            )
                        }
                    }
                } catch (e: Exception) {
                    logger.error("An error appears when trying to parse message : $msg", e)
                }
            }
        }
        return null
    }

    private fun String.trimNewLines() = trim(' ', '\n', '\r')
}

enum class WampMessageType(val id: Int) {
    CALL(2), CALL_RESULT(3), CALL_ERROR(4);

    companion object {
        fun fromId(id: Int) = values().filter { it.id == id }.firstOrNull()
            ?: throw IllegalArgumentException(
                "$id is not a valid Wamp message type - supported types: ${values().map { it.id }}"
            )
    }
}

data class WampMessageMeta(
    val ocppVersion: OcppVersion,
    val ocppId: CSOcppId,
    val headers: WampMessageMetaHeaders = emptyList()
)

typealias WampMessageMetaHeaders = List<WampMessageMetaHeader>
typealias WampMessageMetaHeader = Pair<String, String?>
