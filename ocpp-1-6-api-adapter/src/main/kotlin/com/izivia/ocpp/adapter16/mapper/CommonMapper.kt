package com.izivia.ocpp.adapter16.mapper

import com.izivia.ocpp.api.model.common.ChargingProfileType
import com.izivia.ocpp.api.model.common.ChargingScheduleType
import com.izivia.ocpp.api.model.common.EVSEType
import com.izivia.ocpp.api.model.common.IdTokenInfoType
import com.izivia.ocpp.api.model.common.IdTokenType
import com.izivia.ocpp.api.model.common.MeterValueType
import com.izivia.ocpp.api.model.common.SampledValueType
import com.izivia.ocpp.api.model.common.SignedMeterValueType
import com.izivia.ocpp.api.model.common.enumeration.AuthorizationStatusEnumType
import com.izivia.ocpp.api.model.common.enumeration.ChargingProfilePurposeEnumType
import com.izivia.ocpp.api.model.common.enumeration.LocationEnumType
import com.izivia.ocpp.api.model.common.enumeration.MeasurandEnumType
import com.izivia.ocpp.api.model.common.enumeration.PhaseEnumType
import com.izivia.ocpp.api.model.common.enumeration.ReadingContextEnumType
import com.izivia.ocpp.api.model.common.getTypeByIdToken
import com.izivia.ocpp.core16.model.common.ChargingProfile
import com.izivia.ocpp.core16.model.common.ChargingSchedule
import com.izivia.ocpp.core16.model.common.IdTagInfo
import com.izivia.ocpp.core16.model.common.SampledValue
import com.izivia.ocpp.core16.model.common.enumeration.*
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import org.mapstruct.ReportingPolicy
import com.izivia.ocpp.api.model.common.UnitOfMeasure as UnitOfMeasureGen

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
abstract class CommonMapper {

    companion object {

        fun convertIdTagInfo(idTag: IdTagInfo): IdTokenInfoType {
            val status = AuthorizationStatusEnumType.valueOf(idTag.status.name)
            val parentIdTag = idTag.parentIdTag
            val groupIdToken = if (parentIdTag != null) {
                IdTokenType(parentIdTag, parentIdTag.getTypeByIdToken())
            } else {
                null
            }
            return IdTokenInfoType(
                status = status, cacheExpiryDateTime = idTag.expiryDate, groupIdToken = groupIdToken
            )
        }

        fun filterMeterValues(meterValues: List<MeterValueType>?, action: String, context: ReadingContextEnumType): Int =
            if (meterValues == null) {
                throw IllegalArgumentException("Argument meterValue is required in OCPP 1.6 to $action a transaction")
            } else {
                meterValues.map { (s, t) ->
                    MeterValueType(
                        sampledValue = s.filter { it.context == context && it.measurand == MeasurandEnumType.EnergyActiveImportRegister }
                            .also {
                                if (it.size > 1) {
                                    throw IllegalArgumentException("A meter value MUST have at most 1 sampled value with the context ${context.value} : ${it.size} > 1")
                                }
                            }, timestamp = t
                    )
                }.filter { it.sampledValue.isNotEmpty() }.also {
                    if (it.size > 1) {
                        throw IllegalArgumentException("Multiple meter values have a sampled value with the context ${context.value} : ${it.size} > 1")
                    } else if (it.isEmpty()) {
                        throw IllegalArgumentException("At least 1 sampled value with the context ${context.value} MUST be given in property meterValues")
                    }
                }[0].sampledValue[0].value.toInt()
            }

        private fun convertReadingContext(value: ReadingContextEnumType?): ReadingContext =
            if (value != null) {
                ReadingContext.valueOf(value.name)
            } else ReadingContext.SamplePeriodic


        private fun convertPhase(value: PhaseEnumType?): Phase? =
            if (value != null) {
                Phase.valueOf(value.name)
            } else null

        private fun convertLocation(value: LocationEnumType?): Location =
            if (value != null) {
                Location.valueOf(value.name)
            } else Location.Outlet

        @Throws(IllegalStateException::class)
        private fun convertMeasurand(value: MeasurandEnumType?): Measurand =
            when (value) {
                null -> Measurand.EnergyActiveImportRegister
                MeasurandEnumType.EnergyActiveNet,
                MeasurandEnumType.EnergyReactiveNet,
                MeasurandEnumType.EnergyApparentExport,
                MeasurandEnumType.EnergyApparentImport,
                MeasurandEnumType.EnergyApparentNet -> throw IllegalStateException("INVALID REQUEST : Measurand.${value.name} doesn't exists in OCPP 1.6")
                else -> Measurand.valueOf(value.name)
            }

        private fun convertUnit(value: UnitOfMeasureGen?): UnitOfMeasure =
            if (value != null && enumValues<UnitOfMeasure>().any { it.value == value.unit }) {
                UnitOfMeasure.valueOf(value.unit!!)
            } else {
                UnitOfMeasure.Wh
            }

        private fun convertFormat(value: SignedMeterValueType?): ValueFormat =
            if (value != null) {
                ValueFormat.SignedData
            } else {
                ValueFormat.Raw
            }

        fun convertSampledValue(sampleValue: SampledValueType): SampledValue =
            SampledValue(
                value = sampleValue.value.toString(),
                context = convertReadingContext(sampleValue.context),
                format = convertFormat(sampleValue.signedMeterValue),
                location = convertLocation(sampleValue.location),
                measurand = convertMeasurand(sampleValue.measurand),
                phase = convertPhase(sampleValue.phase),
                unit = convertUnit(sampleValue.unitOfMeasure)
            )
    }

    @Named("convertIdTokenType")
    fun convertIdTokenType(idToken: IdTokenType?): String =
        idToken?.idToken
            ?: throw IllegalArgumentException("Argument idToken is required in OCPP 1.6 to start/update a transaction")

    @Named("convertEVSEType")
    fun convertEVSEType(evse: EVSEType?): Int =
        if (evse != null) {
            evse.connectorId ?: evse.id
        } else {
            throw IllegalArgumentException("Argument evse is required in OCPP 1.6 to start a transaction")
        }

    @Named("convertIdTag")
    fun convertIdTag(idTag: String): IdTokenType = IdTokenType(idTag, idTag.getTypeByIdToken())

    @Named("convertChargingProfilePurpose")
    fun convertChargingProfilePurpose(profilePurpose: ChargingProfilePurposeType): ChargingProfilePurposeEnumType =
        when (profilePurpose) {
            ChargingProfilePurposeType.ChargePointMaxProfile -> ChargingProfilePurposeEnumType.ChargingStationMaxProfile
            else -> ChargingProfilePurposeEnumType.valueOf(profilePurpose.name)
        }

    abstract fun convertChargingSchedule(chargingSchedule: ChargingSchedule): ChargingScheduleType

    @Named("convertChargingScheduleList")
    fun convertChargingScheduleList(chargingSchedule: ChargingSchedule): List<ChargingScheduleType> =
        listOf(convertChargingSchedule(chargingSchedule))

    @Named("convertChargingProfile")
    @Mapping(target = "id", source = "chargingProfileId")
    @Mapping(
        target = "chargingProfilePurpose",
        source = "chargingProfilePurpose",
        qualifiedByName = ["convertChargingProfilePurpose"]
    )
    @Mapping(
        target = "chargingSchedule",
        source = "chargingSchedule",
        qualifiedByName = ["convertChargingScheduleList"]
    )
    abstract fun convertChargingProfile(profile: ChargingProfile): ChargingProfileType

}
