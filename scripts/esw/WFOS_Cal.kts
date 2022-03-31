package esw

import common.*
import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.WFOS
import esw.ocs.dsl.params.invoke

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

        var repeats = command(repeatsKey).head()
        if (repeats < 1) error("A repeats value less than zero was used in the WFOS CAL sequence.")

        while (repeats > 0) {

            observeCounter++
            val redExposureId = observeWithExposureId(command, observeCounter, WFOSDET.RED.name, redExposureTypeKey)
            val blueExposureId = observeWithExposureId(command, observeCounter, WFOSDET.BLU.name, blueExposureTypeKey)

            val observe = Observe(command.source().toString(), "singleExposure", command.obsId).madd(command.paramSet())
            sendSingleCommandToSequencer(
                WFOS,
                wfosSequencer,
                observe.madd(redExposureIdKey.set(redExposureId), blueExposureIdKey.set(blueExposureId))
            )
            repeats--
        }

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
