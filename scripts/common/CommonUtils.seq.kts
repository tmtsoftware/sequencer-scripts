// @file:Repository("https://jitpack.io/")
@file:DependsOn("com.github.tmtsoftware.esw:esw-ocs-dsl-kt_2.13:0.4.0")
@file:DependsOn("com.github.tmtsoftware.esw:esw-ocs-app_2.13:0.4.0")

@file:Import("Keys.seq.kts")
@file:Import("Utils.seq.kts")

import csw.params.commands.ControlCommand
import csw.params.core.generics.Key
import csw.params.core.generics.ParameterSetType
import csw.params.core.models.Choice
import csw.params.core.models.ExposureId
import csw.params.core.models.ObsId
import esw.ocs.dsl.core.CommandHandlerScope
import esw.ocs.dsl.core.HandlerScope
import esw.ocs.dsl.core.ScriptScope
import esw.ocs.dsl.highlevel.RichComponent
import esw.ocs.dsl.params.Params
import esw.ocs.dsl.params.first
import esw.ocs.dsl.params.invoke
import esw.ocs.dsl.params.params
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

suspend fun <T> CommandHandlerScope.setupAssembly(assembly: RichComponent, commandName: String, key: Key<T>, assemblyKey: Key<T>, params: Params) {
    val assemblyParam = params.get(key)
    if (assemblyParam.isDefined) {
        val command = Setup(assembly.prefix.toString(), commandName).add(assemblyKey.set(assemblyParam.get().first))
        sendCommandAndLog(assembly, command)
    } else {
        logger.error("${this.prefix}: Param of $key not found for ${assembly.prefix}")
        throw Error("Param of $key not found for ${assembly.prefix}")
    }
}

suspend fun CommandHandlerScope.loadConfiguration(assembly: RichComponent, obsId: ObsId?, directory: String, exposureId: ExposureId, rampIntegrationTime: Int, ramps: Int) {
    val fileName = "${directory}/${exposureId}.fits"
    val fileNameParam = fileNameKey.set(fileName)
    val exposureIdParam = exposureIdKey.set(exposureId.toString())
    val rampIntegrationTimeParam = rampIntegrationTimeKey.set(rampIntegrationTime)
    val rampsParam = rampsKey.set(ramps)
    val command = Setup(assembly.prefix.toString(), "LOAD_CONFIGURATION", obsId?.toString()).madd(exposureIdParam, fileNameParam, rampIntegrationTimeParam, rampsParam)
    sendCommandAndLog(assembly, command)
}

suspend fun CommandHandlerScope.startExposure(assembly: RichComponent, obsId: ObsId?, timeout: Duration) {
    logger.info("${this.prefix}: send START_EXPOSURE command to ${assembly.prefix} with obsId $obsId")
    val subRes = assembly.submitAndWait(Observe(assembly.prefix.toString(), "START_EXPOSURE", obsId?.toString()), timeout = timeout)
    logger.info("${this.prefix}: command START_EXPOSURE sent to ${assembly.prefix} completed with $subRes")
}

suspend fun CommandHandlerScope.setupAdcAssembly(adcAssembly: RichComponent, params: Params) {
    //if true do PRISM_FOLLOW
    //if false do nothing
    //if not present - show error
    val followParam = params(scienceAdcFollowKey).head()
            ?: throw Error("Param of $scienceAdcFollowKey not found for ${adcAssembly.prefix}")

    if (followParam) {
        val followCommand = Setup(adcAssembly.prefix.toString(), "PRISM_FOLLOW")
        sendCommandAndLog(adcAssembly, followCommand)
    }
}

suspend fun HandlerScope.retractAdcAssembly(adcAssembly: RichComponent, position: String) {
    val retractParam = retractSelectKey.set(Choice(position))
    val retractCommand = Setup(adcAssembly.prefix.toString(), "RETRACT_SELECT").add(retractParam)
    sendCommandAndLog(adcAssembly, retractCommand)
}

suspend fun <T> HandlerScope.sendCommandAndLog(assembly: RichComponent, command: T) where T : ControlCommand, T : ParameterSetType<T> {
    logger.info("${this.prefix}: send ${command.commandName()} command to ${assembly.prefix} with param: ${command.params.format()}")
    val subRes = assembly.submitAndWait(command)
    logger.info("${this.prefix}: command ${command.commandName()} sent to ${assembly.prefix} completed with $subRes")
}

suspend fun <T> ScriptScope.sendCommandAndLog(assembly: RichComponent, command: T) where T : ControlCommand, T : ParameterSetType<T> {
    logger.info("${this.prefix}: send ${command.commandName()} command to ${assembly.prefix} with param: ${command.params.format()}")
    val subRes = assembly.submitAndWait(command)
    logger.info("${this.prefix}: command ${command.commandName()} sent to ${assembly.prefix} completed with $subRes")
}


suspend fun HandlerScope.sendSetupCommandToAssembly(assembly: RichComponent, commandName: String) {
    logger.info("${this.prefix}: send $commandName command to ${assembly.prefix}")
    val subRes = assembly.submitAndWait(Setup(this.prefix, commandName))
    logger.info("${this.prefix}: command $commandName sent to ${assembly.prefix} completed with $subRes")
}

suspend fun HandlerScope.cleanUp(imagerDetector: RichComponent, adcAssembly: RichComponent, ifsDetector: RichComponent? = null) {
    ifsDetector?.let { ifsAssembly -> sendSetupCommandToAssembly(ifsAssembly, "SHUTDOWN") }
    sendSetupCommandToAssembly(imagerDetector, "SHUTDOWN")
    sendSetupCommandToAssembly(adcAssembly, "PRISM_STOP")
    retractAdcAssembly(adcAssembly, "OUT")
}

fun exposureTimeoutFrom(ramps: Int, integrationTime: Int): Duration {
    val timeout = ramps * (integrationTime / 1000) + 30  // integration time will be coming in terms of milliseconds.
    return timeout.seconds
}
