package esw

import common.getObsId
import csw.params.commands.Observe
import csw.params.commands.SequenceCommand
import csw.params.core.generics.Key
import csw.params.core.models.StandaloneExposureId
import csw.time.core.models.UTCTime
import esw.ocs.dsl.core.CommandHandlerScope
import esw.ocs.dsl.core.ReusableScriptResult
import esw.ocs.dsl.core.reusableScript
import esw.ocs.dsl.highlevel.RichSequencer
import esw.ocs.dsl.highlevel.models.ExposureNumber
import esw.ocs.dsl.highlevel.models.IRIS
import esw.ocs.dsl.highlevel.models.TYPLevel
import esw.ocs.dsl.isStarted
import esw.ocs.dsl.params.invoke
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

fun commonHandlers(sequencer: RichSequencer): ReusableScriptResult {
    return reusableScript {

        onGlobalError { exception ->
            val errorEvent = SystemEvent(this.prefix, "onError-event")
            publishEvent(errorEvent)
            error(exception.reason, exception.cause)
        }

        onSetup("observationStart") { command ->
            val obsId = getObsId(command)
            publishEvent(observationStart(obsId))
            submitAndWaitForStart(sequencer, command)
        }

        onSetup("preset") { command ->
            val obsId = getObsId(command)

            publishEvent(presetStart(obsId))
            val setup = Setup(command.source().toString(), "setupAcquisition", command.obsId).madd(command.paramSet())

            submitAndWaitForStart(sequencer, setup)

            publishEvent(presetEnd(obsId))
        }

        onObserve("fineAcquisition") {
            info("In FineAcquisition handler. Currently NOOP.")
        }

        onSetup("setupObservation") { command ->
            val obsId = getObsId(command)
            publishEvent(scitargetAcqStart(obsId))
            submitAndWaitForStart(sequencer, command)

            publishEvent(scitargetAcqEnd(obsId))
        }

        onSetup("observationEnd") { command ->
            val obsId = getObsId(command)
            submitAndWaitForStart(sequencer, command)
            publishEvent(observationEnd(obsId))
        }
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


suspend fun CommandHandlerScope.submitAndWaitForStart(sequencer: RichSequencer, command: SequenceCommand) {
    val initialRes = sequencer.submit(sequenceOf(command))
    loop(Duration.milliseconds(100)) {
        val query = sequencer.query(initialRes.runId())
        stopWhen(!query.isStarted)
    }
}

