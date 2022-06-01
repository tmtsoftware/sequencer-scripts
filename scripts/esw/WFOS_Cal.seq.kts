// @file:Repository("https://jitpack.io/")
@file:DependsOn("com.github.tmtsoftware.esw:esw-ocs-dsl-kt_2.13:0.4.0")
@file:DependsOn("com.github.tmtsoftware.esw:esw-ocs-app_2.13:0.4.0")

@file:Import("Common.seq.kts")
@file:Import("../common/CommonUtils.seq.kts")
@file:Import("../common/Keys.seq.kts")
@file:Import("../common/Utils.seq.kts")

import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.WFOS

script {
    val wfosSequencer = Sequencer(WFOS, obsMode)
    var observeCounter = 0

    onSetup("observationStart") { command ->
        val obsId = getObsId(command)
        publishEvent(observationStart(obsId))
    }


    onObserve("fineAcquisition") {
        info("In FineAcquisition handler. Currently NOOP.")
    }

    onSetup("setupObservation") { command ->
        val obsId = getObsId(command)
        publishEvent(scitargetAcqStart(obsId))
        sendSingleCommandToSequencer(WFOS, wfosSequencer, command)

        publishEvent(scitargetAcqEnd(obsId))
    }

    onObserve("observe") { command ->
        val obsId = getObsId(command)

        publishEvent(observeStart(obsId))

        observeCounter++
        val redExposureId = observeWithExposureId(command, observeCounter, WFOSDET.RED.name, redExposureTypeKey)
        val blueExposureId = observeWithExposureId(command, observeCounter, WFOSDET.BLU.name, blueExposureTypeKey)

        val observe = Observe(command.source().toString(), "singleExposure", command.obsId).madd(command.paramSet())
        sendSingleCommandToSequencer(
            WFOS,
            wfosSequencer,
            observe.madd(redExposureIdKey.set(redExposureId), blueExposureIdKey.set(blueExposureId))
        )

        publishEvent(observeEnd(obsId))
    }

    onSetup("observationEnd") { command ->
        val obsId = getObsId(command)
        publishEvent(observationEnd(obsId))
    }

    onGlobalError { exception ->
        val errorEvent = SystemEvent(this.prefix, "onError-event")
        publishEvent(errorEvent)
        error(exception.reason, exception.cause)
    }
}
