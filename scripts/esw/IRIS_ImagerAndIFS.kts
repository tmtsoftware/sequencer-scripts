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

    onObserve("coarseAcquisition") { command ->
        val obsId = getObsId(command)
        publishEvent(guidestarAcqStart(obsId))

        observeCounter++
        val imgExposureId = observeWithExposureId(command, observeCounter, IRISDET.IMG.name, imagerExposureTypeKey)
        val observe = Observe(command.source().toString(), "acquisitionExposure", command.obsId).madd(command.paramSet())
        submitAndWaitForStart(irisSequencer, observe.madd(imagerExposureIdKey.set(imgExposureId)))

        publishEvent(guidestarAcqEnd(obsId))
    }


    onObserve("observe") { command ->
        val obsId = getObsId(command)

        publishEvent(observeStart(obsId))

        observeCounter++
        val ifsExposureId = observeWithExposureId(command, observeCounter, IRISDET.IFS.name, ifsExposureTypeKey)
        val imgExposureId = observeWithExposureId(command, observeCounter, IRISDET.IMG.name, imagerExposureTypeKey)

        val observe = Observe(command.source().toString(), "singleExposure", command.obsId).madd(command.paramSet())
        submitAndWaitForStart(irisSequencer, observe.madd(ifsExposureIdKey.set(ifsExposureId), imagerExposureIdKey.set(imgExposureId)))

        publishEvent(observeEnd(obsId))
    }


}
