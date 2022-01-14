package common

import csw.params.core.generics.Key
import csw.params.core.models.Choice
import csw.params.core.models.ExposureId
import csw.params.core.models.ObsId
import esw.ocs.dsl.core.CommandHandlerScope
import esw.ocs.dsl.core.HandlerScope
import esw.ocs.dsl.highlevel.RichComponent
import esw.ocs.dsl.params.Params
import esw.ocs.dsl.params.first
import esw.ocs.dsl.params.invoke

suspend fun <T> CommandHandlerScope.setupAssembly(assembly: RichComponent, commandName: String, key: Key<T>, assemblyKey: Key<T>, params: Params) {
    val assemblyParam = params.get(key)
    if (assemblyParam.isDefined) {
        val command = Setup(assembly.prefix.toString(), commandName).add(assemblyKey.set(assemblyParam.get().first))
        assembly.submitAndWait(command)
    } else throw Error("Param of $key not found for ${assembly.prefix}")
}

suspend fun CommandHandlerScope.loadConfiguration(assembly: RichComponent, obsId: ObsId?, directory: String, exposureId: ExposureId, rampIntegrationTime: Int, ramps: Int) {
    val fileName = "${directory}/${exposureId}.fits"
    val fileNameParam = fileNameKey.set(fileName)
    val exposureIdParam = exposureIdKey.set(exposureId.toString())
    val rampIntegrationTimeParam = rampIntegrationTimeKey.set(rampIntegrationTime)
    val rampsParam = rampsKey.set(ramps)
    val command = Setup(assembly.prefix.toString(), "LOAD_CONFIGURATION", obsId?.toString()).madd(exposureIdParam, fileNameParam, rampIntegrationTimeParam, rampsParam)
    assembly.submitAndWait(command)
}

suspend fun CommandHandlerScope.startExposure(assembly: RichComponent, obsId: ObsId?) {
    assembly.submitAndWait(Observe(assembly.prefix.toString(), "START_EXPOSURE", obsId?.toString()))
}

suspend fun CommandHandlerScope.setupAdcAssembly(adcAssembly: RichComponent, params: Params) {
    //if true do PRISM_FOLLOW
    //if false do nothing
    //if not present - show error
    val followParam = params(scienceAdcFollowKey).head()
            ?: throw Error("Param of $scienceAdcFollowKey not found for ${adcAssembly.prefix}")

    if (followParam) {
        val followCommand = Setup(adcAssembly.prefix.toString(), "PRISM_FOLLOW")
        adcAssembly.submitAndWait(followCommand)
    }
}

suspend fun HandlerScope.retractAdcAssembly(adcAssembly: RichComponent, position: String) {
    val retractParam = retractSelectKey.set(Choice(position))
    val retractCommand = Setup(adcAssembly.prefix.toString(), "RETRACT_SELECT").add(retractParam)
    adcAssembly.submitAndWait(retractCommand)
}

suspend fun HandlerScope.sendSetupCommandToAssembly(assembly: RichComponent, commandName: String) {
    assembly.submitAndWait(Setup(this.prefix, commandName))
}

suspend fun HandlerScope.cleanUp(imagerDetector: RichComponent, adcAssembly: RichComponent, ifsDetector: RichComponent? = null) {
    ifsDetector?.let { ifsAssembly -> sendSetupCommandToAssembly(ifsAssembly, "SHUTDOWN") }
    sendSetupCommandToAssembly(imagerDetector, "SHUTDOWN")
    sendSetupCommandToAssembly(adcAssembly, "PRISM_STOP")
    retractAdcAssembly(adcAssembly, "OUT")
}
