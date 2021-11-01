package iris

import csw.params.core.generics.Key
import csw.params.core.models.Choice
import csw.params.core.models.ExposureId
import csw.params.core.models.ObsId
import csw.params.events.EventKey
import csw.params.events.EventName
import csw.params.events.SystemEvent
import csw.prefix.models.Prefix
import esw.ocs.dsl.core.CommandHandlerScope
import esw.ocs.dsl.highlevel.RichComponent
import esw.ocs.dsl.highlevel.models.IRIS
import esw.ocs.dsl.isStarted
import esw.ocs.dsl.params.Params
import esw.ocs.dsl.params.booleanKey
import esw.ocs.dsl.params.first
import esw.ocs.dsl.params.invoke
import kotlin.time.Duration

suspend fun <T> CommandHandlerScope.setupAssembly(assembly: RichComponent, commandName: String, key: Key<T>, assemblyKey: Key<T>, params: Params) {
    val assemblyParam = params.get(key)
    if (assemblyParam.isDefined) {
        val command = Setup(assembly.prefix.toString(), commandName).add(assemblyKey.set(assemblyParam.get().first))
        val initialRes = assembly.submit(command)

        loop(Duration.milliseconds(100)) {
            val query = assembly.query(initialRes.runId())
            stopWhen(!query.isStarted)
        }
    }
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
    val initialRes = assembly.submit(Observe(assembly.prefix.toString(), "START_EXPOSURE", obsId?.toString()))
    loop(Duration.milliseconds(100)) {
        val query = assembly.query(initialRes.runId())
        stopWhen(!query.isStarted)
    }
}

suspend fun CommandHandlerScope.setupAdcAssembly(adcAssembly: RichComponent, params: Params) {
    val followParam = params(scienceAdcFollowKey).head()
    if (followParam) {
        val angle = params(scienceAdcTargetKey).head()
        val retractParam = retractSelectKey.set(Choice("IN"))
        val retractCommand = Setup(adcAssembly.prefix.toString(), "RETRACT_SELECT").add(retractParam)
        adcAssembly.submitAndWait(retractCommand)

        val prismFollowParam = targetAngleKey.set(angle)
        val followCommand = Setup(adcAssembly.prefix.toString(), "PRISM_FOLLOW").add(prismFollowParam)
        adcAssembly.submitAndWait(followCommand)
        waitFor {
            var onTarget = false
            onEvent(EventKey(Prefix(IRIS, "imager.adc"), EventName("prism_state")).key()) { event ->
                when (event) {
                    is SystemEvent -> {
                        event(booleanKey("onTarget")).head()?.let { x -> onTarget = x }
                    }
                }
            }
            onTarget
        }
    }
}
