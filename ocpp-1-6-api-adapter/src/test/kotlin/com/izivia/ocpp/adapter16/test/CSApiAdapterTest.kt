package com.izivia.ocpp.adapter16.test

import com.izivia.ocpp.adapter16.Ocpp16CSApiAdapter
import com.izivia.ocpp.adapter16.Ocpp16TransactionIds
import com.izivia.ocpp.adapter16.impl.RealTransactionRepository
import com.izivia.ocpp.api.CSApi
import com.izivia.ocpp.api.model.common.enumeration.ChargingProfilePurposeEnumType
import com.izivia.ocpp.api.model.setchargingprofile.SetChargingProfileResp
import com.izivia.ocpp.api.model.setchargingprofile.enumeration.ChargingProfileStatusEnumType
import com.izivia.ocpp.core16.model.common.ChargingProfile
import com.izivia.ocpp.core16.model.common.ChargingSchedule
import com.izivia.ocpp.core16.model.common.enumeration.ChargingProfilePurposeType
import com.izivia.ocpp.core16.model.common.enumeration.ChargingRateUnitType
import com.izivia.ocpp.core16.model.remotestart.ChargingSchedulePeriod
import com.izivia.ocpp.core16.model.remotestart.enumeration.ChargingProfileKindType
import com.izivia.ocpp.core16.model.setchargingprofile.SetChargingProfileReq
import com.izivia.ocpp.core16.model.setchargingprofile.enumeration.ChargingProfileStatus
import com.izivia.ocpp.operation.information.ExecutionMetadata
import com.izivia.ocpp.operation.information.OperationExecution
import com.izivia.ocpp.operation.information.RequestMetadata
import com.izivia.ocpp.operation.information.RequestStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class CSApiAdapterTest {
    private lateinit var csApi: CSApi

    @BeforeEach
    fun init() {
        csApi = mockk()
    }

    @AfterEach
    fun destroy() {
        unmockkAll()
    }

    @Test
    fun `setChargingProfile request`() {
        val transactionIds = RealTransactionRepository()
        transactionIds.saveTransactionIds(Ocpp16TransactionIds(localId = "4", csmsId = 2))
        val requestMetadata = RequestMetadata("")

        every { csApi.setChargingProfile(any(), any()) } answers {
            val req = secondArg<com.izivia.ocpp.api.model.setchargingprofile.SetChargingProfileReq>()
            OperationExecution(
                executionMeta = ExecutionMetadata(reqMeta = requestMetadata, status = RequestStatus.SUCCESS),
                request = req,
                response = SetChargingProfileResp(
                    status = when {
                        req.chargingProfile.chargingProfilePurpose == ChargingProfilePurposeEnumType.ChargingStationMaxProfile
                                && req.chargingProfile.transactionId == null -> ChargingProfileStatusEnumType.Accepted

                        req.chargingProfile.chargingProfilePurpose == ChargingProfilePurposeEnumType.TxProfile
                                && req.chargingProfile.transactionId == "4" -> ChargingProfileStatusEnumType.Accepted

                        else -> ChargingProfileStatusEnumType.Rejected
                    }
                )
            )
        }

        val operations = Ocpp16CSApiAdapter(csApi, transactionIds)
        val request = SetChargingProfileReq(
            connectorId = 1,
            csChargingProfiles = ChargingProfile(
                chargingProfileId = 1,
                stackLevel = 2,
                chargingProfilePurpose = ChargingProfilePurposeType.TxProfile,
                chargingProfileKind = ChargingProfileKindType.Relative,
                chargingSchedule = ChargingSchedule(
                    ChargingRateUnitType.A,
                    listOf(ChargingSchedulePeriod(1, 1))
                ),
                transactionId = 2
            )
        )
        val response = operations.setChargingProfile(requestMetadata, request)

        expectThat(response) {
            get { this.request }.isEqualTo(request)
            get { this.response.status }.isEqualTo(ChargingProfileStatus.Accepted)
        }
    }
}
