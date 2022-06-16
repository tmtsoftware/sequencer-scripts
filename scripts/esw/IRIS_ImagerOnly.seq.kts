//@file:Repository("https://jitpack.io/")
//@file:DependsOn("com.github.tmtsoftware.esw:esw-ocs-dsl-kt_2.13:d08b0359adada2fe521243aaf85347337369feb4")
//@file:DependsOn("com.github.tmtsoftware.esw:esw-ocs-app_2.13:d08b0359adada2fe521243aaf85347337369feb4")

@file:Import("Common.seq.kts")
@file:Import("../common/CommonUtils.seq.kts")
@file:Import("../common/Keys.seq.kts")
@file:Import("../common/Utils.seq.kts")

import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.IRIS
import esw.ocs.dsl.highlevel.models.TCS

println("XXX 2 in esw/IRIS_ImagerOnly.seq.kts")
testXXX()

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
