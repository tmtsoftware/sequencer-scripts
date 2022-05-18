//@file:Repository("https://jitpack.io/")
//@file:Repository("file://home/abrighto/.ivy2")
//@file:Repository("file://home/abrighto/.m2/repository")
//@file:DependsOn("com.github.tmtsoftware.esw:esw-ocs-script-kt_2.13:6934692f76eb406b73402153eefb3ca0e9f8133f")
@file:DependsOn("com.github.tmtsoftware.esw:esw-ocs-dsl-kt_2.13:b964c761e4f998d64306dd70298da577558a42f2")
@file:DependsOn("com.github.tmtsoftware.esw:esw-ocs-app_2.13:b964c761e4f998d64306dd70298da577558a42f2")

@file:Import("Common.seq.kts")

import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.IRIS
import esw.ocs.dsl.highlevel.models.TCS

println("XXX in esw/IRIS_ImagerOnly.seq.kts")

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
