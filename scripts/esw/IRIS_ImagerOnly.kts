package esw

import common.DET
import common.getObsId
import common.imagerExposureIdKey
import common.imagerExposureTypeKey
import esw.ocs.api.models.ObsMode
import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.IRIS

script {
    val irisSequencer = Sequencer(IRIS, ObsMode("IRIS_ImagerOnly"))
    var observeCounter = 0
    // sequence :  Preset CoarseAcquisition FineAcquisition setupObservation Observe setupObservation Observe setupObservation Observe
    loadScripts(commonHandlers(irisSequencer))

    onObserve("coarseAcquisition") { command ->
        val obsId = getObsId(command)
        publishEvent(guidestarAcqStart(obsId))

        observeCounter++
        val exposureId = observeWithExposureId(command, observeCounter, DET.IMG, imagerExposureTypeKey)
        val observe = Observe(command.source().toString(), "acquisitionExposure", command.obsId).madd(command.paramSet())
        submitAndWaitForStart(irisSequencer, observe.madd(imagerExposureIdKey.set(exposureId)))

        publishEvent(guidestarAcqEnd(obsId))
    }

    onObserve("observe") { command ->
        val obsId = getObsId(command)

        publishEvent(observeStart(obsId))

        observeCounter++
        val exposureId = observeWithExposureId(command, observeCounter, DET.IMG, imagerExposureTypeKey)
        val observe = Observe(command.source().toString(), "singleExposure", command.obsId).madd(command.paramSet())
        submitAndWaitForStart(irisSequencer, observe.madd(imagerExposureIdKey.set(exposureId)))

        publishEvent(observeEnd(obsId))
    }

}
