@file:Import("test.seq.kts")

package esw

import common.IRISDET
import common.getObsId
import common.imagerExposureIdKey
import common.imagerExposureTypeKey
import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.IRIS
import esw.ocs.dsl.highlevel.models.TCS

println("XXX test1 = $test1, test2 = $test2")

val x = 1

script {
    val irisSequencer = Sequencer(IRIS, obsMode)
    val tcsSequencer = Sequencer(TCS, obsMode)
    var observeCounter = 0
    // sequence :  Preset CoarseAcquisition FineAcquisition setupObservation Observe setupObservation Observe setupObservation Observe
    loadScripts(commonHandlers(irisSequencer, tcsSequencer))

    onObserve("coarseAcquisition") { command ->
        val obsId = getObsId(command)
        publishEvent(guidestarAcqStart(obsId))

        observeCounter++
        val exposureId = observeWithExposureId(command, observeCounter, IRISDET.IMG.name, imagerExposureTypeKey)
        val observe = Observe(command.source().toString(), "acquisitionExposure", command.obsId).madd(command.paramSet())
        sendSingleCommandToSequencer(IRIS, irisSequencer, observe.add(imagerExposureIdKey.set(exposureId)))

        publishEvent(guidestarAcqEnd(obsId))
    }

    onObserve("observe") { command ->
        val obsId = getObsId(command)

        publishEvent(observeStart(obsId))

        observeCounter++
        val exposureId = observeWithExposureId(command, observeCounter, IRISDET.IMG.name, imagerExposureTypeKey)
        val observe = Observe(command.source().toString(), "singleExposure", command.obsId).madd(command.paramSet())
        sendSingleCommandToSequencer(IRIS, irisSequencer, observe.add(imagerExposureIdKey.set(exposureId)))

        publishEvent(observeEnd(obsId))
    }

}
