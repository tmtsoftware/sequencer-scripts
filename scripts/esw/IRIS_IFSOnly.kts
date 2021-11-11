package esw

import common.getObsId
import common.ifsExposureIdKey
import common.imageExposureTypeKey
import csw.params.commands.Observe
import csw.params.commands.SequenceCommand
import csw.params.core.models.StandaloneExposureId
import csw.time.core.models.UTCTime
import esw.ocs.api.models.ObsMode
import esw.ocs.dsl.core.CommandHandlerScope
import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.RichSequencer
import esw.ocs.dsl.highlevel.models.ExposureNumber
import esw.ocs.dsl.highlevel.models.IRIS
import esw.ocs.dsl.highlevel.models.TYPLevel
import esw.ocs.dsl.isStarted
import esw.ocs.dsl.params.invoke
import kotlin.time.Duration

script {
    val irisSequencer = Sequencer(IRIS, ObsMode("IRIS_IFSOnly"))
    var observeCounter = 0
    // sequence :  Preset CoarseAcquisition FineAcquisition setupObservation Observe setupObservation Observe setupObservation Observe

    onSetup("startObservation") { command ->
        val obsId = getObsId(command)
        publishEvent(observationStart(obsId))
    }

    onSetup("Preset") { command ->
        val obsId = getObsId(command)

        publishEvent(presetStart(obsId))

        submitAndWaitForStart(irisSequencer, command)

        publishEvent(presetEnd(obsId))
    }

    onObserve("CoarseAcquisition") { command ->
        val obsId = getObsId(command)
        publishEvent(guidestarAcqStart(obsId))

        observeCounter++
        exposure(command, irisSequencer, observeCounter)

        publishEvent(guidestarAcqEnd(obsId))
    }

    onObserve("FineAcquisition") {
        info("In FineAcquisition handler. Currently NOOP.")
    }

    onSetup("setupObservation") { command ->
        val obsId = getObsId(command)
        publishEvent(scitargetAcqStart(obsId))

        submitAndWaitForStart(irisSequencer, command)

        publishEvent(scitargetAcqEnd(obsId))
    }

    onObserve("Observe") { command ->
        val obsId = getObsId(command)

        publishEvent(observeStart(obsId))

        observeCounter++
        exposure(command, irisSequencer, observeCounter)

        publishEvent(observeEnd(obsId))
    }

    onSetup("endObservation") { command ->
        val obsId = getObsId(command)
        publishEvent(observationEnd(obsId))
    }
}


suspend fun CommandHandlerScope.exposure(observe: Observe, sequencer: RichSequencer, observeCounter: Int) {
    val imageExposureType = observe(imageExposureTypeKey).head()
    val ifsExposureId = getExposureId(observe.obsId, imageExposureType, observeCounter, "IFS")
    val observeWithExposureId = observe.madd(ifsExposureIdKey.set(ifsExposureId))

    submitAndWaitForStart(sequencer, observeWithExposureId)
}

fun getExposureId(obsId: String?, typLevel: String, observeCounter: Int, det: String): String {
    val exposureNumber = ExposureNumber("%04d".format(observeCounter))
    val typLevelStr = TYPLevel(typLevel + 1)
    return obsId?.let { it + IRIS + det + typLevelStr + exposureNumber }
            ?: StandaloneExposureId(UTCTime.now(), IRIS, det, typLevelStr, exposureNumber).toString()

}

suspend fun CommandHandlerScope.submitAndWaitForStart(sequencer: RichSequencer, command: SequenceCommand) {
    val initialRes = sequencer.submit(sequenceOf(command))
    loop(Duration.milliseconds(100)) {
        val query = sequencer.query(initialRes.runId())
        stopWhen(!query.isStarted)
    }
}
