package esw

import common.IRISDET
import common.getObsId
import common.imagerExposureIdKey
import common.imagerExposureTypeKey
import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.IRIS
import esw.ocs.dsl.highlevel.models.Prefix

script {
    val irisSequencer = Sequencer(IRIS, obsMode)
    var observeCounter = 0
    // sequence :  Preset CoarseAcquisition FineAcquisition setupObservation Observe setupObservation Observe setupObservation Observe
    loadScripts(commonHandlers(irisSequencer))

    onObserve("coarseAcquisition") { command ->
        val obsId = getObsId(command)
        publishEvent(guidestarAcqStart(obsId))

        observeCounter++
        val exposureId = observeWithExposureId(command, observeCounter, IRISDET.IMG.name, imagerExposureTypeKey)
        val observe = Observe(command.source().toString(), "acquisitionExposure", command.obsId).madd(command.paramSet())
        irisSequencer.submitAndWait(sequenceOf(observe.madd(imagerExposureIdKey.set(exposureId))))

        publishEvent(guidestarAcqEnd(obsId))
    }

    onObserve("observe") { command ->
        val obsId = getObsId(command)

        publishEvent(observeStart(obsId))

        observeCounter++
        val exposureId = observeWithExposureId(command, observeCounter, IRISDET.IMG.name, imagerExposureTypeKey)
        val observe = Observe(command.source().toString(), "singleExposure", command.obsId).madd(command.paramSet())
        irisSequencer.submitAndWait(sequenceOf(observe.madd(imagerExposureIdKey.set(exposureId))))

        publishEvent(observeEnd(obsId))
    }

}
