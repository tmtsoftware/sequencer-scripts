package esw

import common.*
import esw.ocs.api.models.ObsMode
import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.IRIS

script {
    val irisSequencer = Sequencer(IRIS, ObsMode("IRIS_ImagerAndIFS"))
    var observeCounter = 0
    // sequence :  Preset CoarseAcquisition FineAcquisition setupObservation Observe setupObservation Observe setupObservation Observe
    loadScripts(commonHandlers(irisSequencer))

    onObserve("CoarseAcquisition") { command ->
        val obsId = getObsId(command)
        publishEvent(guidestarAcqStart(obsId))

        observeCounter++
        val ifsExposureId = observeWithExposureId(command, observeCounter, DET.IFS, ifsExposureTypeKey)
        val imgExposureId = observeWithExposureId(command, observeCounter, DET.IMG, imagerExposureTypeKey)

        submitAndWaitForStart(irisSequencer, command.madd(ifsExposureIdKey.set(ifsExposureId), imagerExposureIdKey.set(imgExposureId)))

        publishEvent(guidestarAcqEnd(obsId))
    }


    onObserve("Observe") { command ->
        val obsId = getObsId(command)

        publishEvent(observeStart(obsId))

        observeCounter++
        val ifsExposureId = observeWithExposureId(command, observeCounter, DET.IFS, ifsExposureTypeKey)
        val imgExposureId = observeWithExposureId(command, observeCounter, DET.IMG, imagerExposureTypeKey)

        submitAndWaitForStart(irisSequencer, command.madd(ifsExposureIdKey.set(ifsExposureId), imagerExposureIdKey.set(imgExposureId)))

        publishEvent(observeEnd(obsId))
    }


}
