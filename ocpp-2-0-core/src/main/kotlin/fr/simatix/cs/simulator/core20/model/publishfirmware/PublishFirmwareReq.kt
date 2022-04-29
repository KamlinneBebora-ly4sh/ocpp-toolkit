package fr.simatix.cs.simulator.core20.model.publishfirmware

data class PublishFirmwareReq(
        val location : String,
        val checksum : String,
        val requestId : Int,
        val retries : Int?=null,
        val retryInterval : Int?=null
)
