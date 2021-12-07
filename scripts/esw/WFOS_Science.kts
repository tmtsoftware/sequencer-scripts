package esw

import common.*
import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.Prefix
import esw.ocs.dsl.highlevel.models.WFOS

script {
    val wfosSequencer = Sequencer(WFOS, obsMode)
    var observeCounter = 0

    onSetup("observationStart") { command ->
        val obsId = getObsId(command)
        publishEvent(observationStart(obsId))
    }

    onSetup("preset") { command ->
        val obsId = getObsId(command)

        publishEvent(presetStart(obsId))
        val setup = Setup(command.source().toString(), "setupAcquisition", command.obsId).madd(command.paramSet())

        submitAndWaitForStart(wfosSequencer, setup)

        publishEvent(presetEnd(obsId))
    }

    onObserve("fineAcquisition") {
        info("In FineAcquisition handler. Currently NOOP.")
    }

    onSetup("setupObservation") { command ->
        val obsId = getObsId(command)
        publishEvent(scitargetAcqStart(obsId))
        submitAndWaitForStart(wfosSequencer, command)

        publishEvent(scitargetAcqEnd(obsId))
    }


    onObserve("coarseAcquisition") { command ->
        val obsId = getObsId(command)
        publishEvent(guidestarAcqStart(obsId))

        observeCounter++
        val imgExposureId = observeWithExposureId(command, observeCounter, WFOSDET.BLU.name, blueExposureTypeKey)
        val observe = Observe(command.source().toString(), "acquisitionExposure", command.obsId).madd(command.paramSet())
        submitAndWaitForStart(wfosSequencer, observe.madd(blueExposureIdKey.set(imgExposureId)))

        publishEvent(guidestarAcqEnd(obsId))
    }


    onObserve("observe") { command ->
        val obsId = getObsId(command)

        publishEvent(observeStart(obsId))

        observeCounter++
        val redExposureId = observeWithExposureId(command, observeCounter, WFOSDET.RED.name, redExposureTypeKey)
        val blueExposureId = observeWithExposureId(command, observeCounter, WFOSDET.BLU.name, blueExposureTypeKey)

        val observe = Observe(command.source().toString(), "singleExposure", command.obsId).madd(command.paramSet())
        submitAndWaitForStart(wfosSequencer, observe.madd(redExposureIdKey.set(redExposureId), blueExposureIdKey.set(blueExposureId)))

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
