
@file:Import("Common.seq.kts")

package esw
import common.*

import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.IRIS
import esw.ocs.dsl.highlevel.models.TCS

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
        val imgExposureId = observeWithExposureId(command, observeCounter, IRISDET.IMG.name, imagerExposureTypeKey)
        val observe = Observe(command.source().toString(), "acquisitionExposure", command.obsId).madd(command.paramSet())
        sendSingleCommandToSequencer(IRIS, irisSequencer, observe.add(imagerExposureIdKey.set(imgExposureId)))
        publishEvent(guidestarAcqEnd(obsId))
    }


    onObserve("observe") { command ->
        val obsId = getObsId(command)

        publishEvent(observeStart(obsId))

        observeCounter++
        val ifsExposureId = observeWithExposureId(command, observeCounter, IRISDET.IFS.name, ifsExposureTypeKey)
        val imgExposureId = observeWithExposureId(command, observeCounter, IRISDET.IMG.name, imagerExposureTypeKey)

        val observe = Observe(command.source().toString(), "singleExposure", command.obsId).madd(command.paramSet())
        sendSingleCommandToSequencer(IRIS, irisSequencer, observe.madd(ifsExposureIdKey.set(ifsExposureId), imagerExposureIdKey.set(imgExposureId)))

        publishEvent(observeEnd(obsId))
    }


}
