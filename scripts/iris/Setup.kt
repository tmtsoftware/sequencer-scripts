package iris

import csw.params.commands.CommandName
import csw.params.commands.CommandResponse
import csw.params.commands.Setup
import csw.params.core.generics.Key
import csw.params.core.models.Choice
import csw.params.core.models.ExposureId
import csw.params.core.models.ObsId
import csw.params.javadsl.JUnits
import esw.ocs.dsl.highlevel.RichComponent
import esw.ocs.dsl.params.*
import java.util.*


suspend fun <S> setupAssembly(assembly: RichComponent, commandName: String, lookUpKey: Key<S>, params: Params): CommandResponse.SubmitResponse? {
    val assemblyParam = params.get(lookUpKey)
    return if (assemblyParam.isDefined) {
        val command = Setup(assembly.prefix, CommandName(commandName), Optional.empty()).add(assemblyParam.get())
        assembly.submit(command)
    } else null
}

suspend fun loadConfiguration(assembly: RichComponent, obsId: ObsId?, directory: String, exposureId: ExposureId, rampIntegrationTime: Int, ramps: Int) {
    val fileName = "${directory}/${exposureId}.fits"
    val fileNameParam = stringKey("filename").set(fileName)
    val exposureIdParam = stringKey("exposureId").set(exposureId.toString())
    val rampIntegrationTimeParam = intKey("rampIntegrationTime").set(rampIntegrationTime)
    val rampsParam = intKey("ramps").set(ramps)
    val command = Setup(assembly.prefix, CommandName("LOAD_CONFIGURATION"), Optional.ofNullable(obsId)).madd(exposureIdParam, fileNameParam, rampIntegrationTimeParam, rampsParam)
    assembly.submit(command)
}

suspend fun setupAdcAssembly(adcAssembly: RichComponent, params: Params): CommandResponse.SubmitResponse? {
    val followParam = params.get(doubleKey("scienceAdcFollow"))
    return if (followParam.isDefined) {
        val angle = followParam.get().first
        val retractParam = choiceKey("position", JUnits.NoUnits, choicesOf("IN")).set(Choice("IN"))
        val retractCommand = Setup(adcAssembly.prefix, CommandName("RETRACT_SELECT"), Optional.empty()).add(retractParam)
        adcAssembly.submit(retractCommand)
        val prismFollowParam = doubleKey("targetAngle").set(angle)
        val followCommand = Setup(adcAssembly.prefix, CommandName("PRISM_FOLLOW"), Optional.empty()).add(prismFollowParam)
        adcAssembly.submit(followCommand)
    } else null
}
