package esw

import common.*
import esw.ocs.api.models.ObsMode
import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.IRIS

script {
    val irisSequencer = Sequencer(IRIS, ObsMode("IRIS_IFSOnly"))
    var observeCounter = 0
    // sequence :  Preset CoarseAcquisition FineAcquisition setupObservation Observe setupObservation Observe setupObservation Observe

    loadScripts(commonHandlers(irisSequencer))

    onObserve("CoarseAcquisition") { command ->
        val obsId = getObsId(command)
        publishEvent(guidestarAcqStart(obsId))

        observeCounter++
        val exposureId = observeWithExposureId(command, observeCounter, DET.IFS, ifsExposureTypeKey)
        submitAndWaitForStart(irisSequencer, command.madd(ifsExposureIdKey.set(exposureId)))

        publishEvent(guidestarAcqEnd(obsId))
    }

    onObserve("Observe") { command ->
        val obsId = getObsId(command)

        publishEvent(observeStart(obsId))

        observeCounter++
        val exposureId = observeWithExposureId(command, observeCounter, DET.IFS, ifsExposureTypeKey)
        submitAndWaitForStart(irisSequencer, command.madd(ifsExposureIdKey.set(exposureId)))

        publishEvent(observeEnd(obsId))
    }
}


