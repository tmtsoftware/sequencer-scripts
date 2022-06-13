@file:Repository("https://jitpack.io/")
@file:DependsOn("com.github.tmtsoftware.esw:esw-ocs-dsl-kt_2.13:adc26faf3413a9e70a6627c397563e88ea04afb6")
@file:DependsOn("com.github.tmtsoftware.esw:esw-ocs-app_2.13:adc26faf3413a9e70a6627c397563e88ea04afb6")

@file:Import("../common/CommonUtils.seq.kts")
@file:Import("../common/Keys.seq.kts")
@file:Import("../common/Utils.seq.kts")

import csw.params.commands.Observe
import csw.params.commands.SequenceCommand
import csw.params.commands.Setup
import csw.params.core.generics.Key
import csw.params.core.generics.ParameterSetType
import csw.params.core.models.StandaloneExposureId
import csw.params.events.EventName
import csw.params.events.SystemEvent
import csw.prefix.models.Prefix
import csw.prefix.models.Subsystem
import csw.time.core.models.UTCTime
import esw.ocs.dsl.core.CommandHandlerScope
import esw.ocs.dsl.core.ReusableScriptResult
import esw.ocs.dsl.core.reusableScript
import esw.ocs.dsl.highlevel.RichSequencer
import esw.ocs.dsl.highlevel.models.ExposureNumber
import esw.ocs.dsl.highlevel.models.IRIS
import esw.ocs.dsl.highlevel.models.TCS
import esw.ocs.dsl.highlevel.models.TYPLevel
import esw.ocs.dsl.par
import esw.ocs.dsl.params.booleanKey
import esw.ocs.dsl.params.invoke
import esw.ocs.dsl.params.params
import kotlinx.coroutines.delay

fun commonHandlers(irisSequencer: RichSequencer, tcsSequencer: RichSequencer): ReusableScriptResult {
    return reusableScript {

        onGlobalError { exception ->
            val errorEvent = SystemEvent(this.prefix, "onError-event")
            publishEvent(errorEvent)
            error(exception.reason, exception.cause)
        }

        onSetup("observationStart") { command ->
            val obsId = getObsId(command)
            publishEvent(observationStart(obsId))
            sendSingleCommandToSequencer(IRIS, irisSequencer, command)
        }

        onSetup("preset") { command ->
            val obsId = getObsId(command)

            publishEvent(presetStart(obsId))
            val incomingParamValue = command.params(targetCoordKey).head()
            val parameterTobeSend = baseCoordKey.set(incomingParamValue)
            val tcsPreset = Setup(command.source().toString(), "preset", command.obsId).madd(parameterTobeSend)
            val irisSetup = Setup(command.source().toString(), "setupAcquisition", command.obsId).madd(command.paramSet())

            //We want to do tcs and iris adc movement in parallel, so that adc does not wait until tcs has finished its movement.
            //However, adc need to know that target has been set by tcs to avoid false positives, hence we wait tcs to send MountPosition
            //once this happens target should have been updated in adc also, so it can start its movement now.
            submitCommandsAndWaitForAdcOnTarget(tcsSequencer, irisSequencer, tcsPreset, irisSetup)
            publishEvent(presetEnd(obsId))
        }

        onObserve("fineAcquisition") { command ->
            val obsId = getObsId(command)
            publishEvent(scitargetAcqStart(obsId))
            info("In FineAcquisition handler. Currently NOOP.")
            // purposely added delay for simulating some functionality
            delay(500)
            publishEvent(scitargetAcqEnd(obsId))
        }

        onSetup("setupObservation") { command ->
            submitCommandsAndWaitForAdcOnTarget(tcsSequencer, irisSequencer, command, command)
        }

        onSetup("observationEnd") { command ->
            val obsId = getObsId(command)
            sendSingleCommandToSequencer(IRIS, irisSequencer, command)
            publishEvent(observationEnd(obsId))
        }
    }
}

suspend fun <T> CommandHandlerScope.sendSingleCommandToSequencer(subsystem: Subsystem, sequencer: RichSequencer, command: T) where T : SequenceCommand, T : ParameterSetType<T> {
    logger.info("${this.prefix}: send ${command.commandName()} command to $subsystem.$obsMode with param: ${command.params.format()}")
    val subRes = sequencer.submitAndWait(sequenceOf(command))
    logger.info("${this.prefix}: command ${command.commandName()} sent to $subsystem.$obsMode completed with $subRes")
}

suspend fun CommandHandlerScope.submitCommandsAndWaitForAdcOnTarget(tcsSequencer: RichSequencer, irisSequencer: RichSequencer, tcsCommand: Setup, irisCommand: Setup) {
    par(
            { sendSingleCommandToSequencer(TCS, tcsSequencer, tcsCommand) },
            { sendSingleCommandToSequencer(IRIS, irisSequencer, irisCommand) }
    )
    var onTarget = false
    onEvent(csw.params.events.EventKey(Prefix(IRIS, "imager.adc"), EventName("prism_state")).key()) { event ->
        when (event) {
            is SystemEvent -> {
                val state = event(followingKey).head()
                event(booleanKey("onTarget")).head()?.let { x -> onTarget = state.name() == "FOLLOWING" && x }
            }
        }
    }
    waitFor {
        onTarget
    }
}

fun getExposureId(obsId: String?, typLevel: String, observeCounter: Int, det: String): String {
    val exposureNumber = ExposureNumber("%04d".format(observeCounter))
    val typLevelStr = TYPLevel(typLevel + 1)
    return obsId?.let { "$it-$IRIS-$det-$typLevelStr-$exposureNumber" }
            ?: StandaloneExposureId(UTCTime.now(), IRIS, det, typLevelStr, exposureNumber).toString()

}

fun observeWithExposureId(observe: Observe, observeCounter: Int, det: String, exposureTypekey: Key<String>): String {
    val imageExposureType = observe(exposureTypekey).head()
    val obsId = getObsId(observe).toString()
    return getExposureId(obsId, imageExposureType, observeCounter, det)
}
