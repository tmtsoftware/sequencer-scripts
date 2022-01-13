package esw

import common.baseCoordKey
import common.getObsId
import common.targetCoordKey
import csw.params.commands.Observe
import csw.params.core.generics.Key
import csw.params.core.models.StandaloneExposureId
import csw.time.core.models.UTCTime
import esw.ocs.dsl.core.ReusableScriptResult
import esw.ocs.dsl.core.reusableScript
import esw.ocs.dsl.highlevel.RichSequencer
import esw.ocs.dsl.highlevel.models.ExposureNumber
import esw.ocs.dsl.highlevel.models.IRIS
import esw.ocs.dsl.highlevel.models.TYPLevel
import esw.ocs.dsl.par
import esw.ocs.dsl.params.invoke
import esw.ocs.dsl.params.params

fun commonHandlers(irisSequencer: RichSequencer, tcsSequencer: RichSequencer): ReusableScriptResult {
    return reusableScript {

        onGlobalError { exception ->
            val errorEvent = SystemEvent(this.prefix, "onError-event")
            publishEvent(errorEvent)
            error(exception.reason, exception.cause)
        }

        onSetup("observationStart") { command ->
            val obsId = getObsId(command)
            publishEvent(observationStart(obsId))
            irisSequencer.submitAndWait(sequenceOf(command))
        }

        onSetup("preset") { command ->
            val obsId = getObsId(command)

            publishEvent(presetStart(obsId))
            val incomingParamValue = command.params(targetCoordKey).head()
            val parameterTobeSend = baseCoordKey.set(incomingParamValue)
            val tcsPreset = Setup(command.source().toString(), "preset", command.obsId).madd(parameterTobeSend)
            val irisSetup = Setup(command.source().toString(), "setupAcquisition", command.obsId).madd(command.paramSet())

            //We want to do tcs and iris adc movement in parallel, so that adc does not wait until tcs has finished its movement.
            //However, adc need to know that target has been set by tcs to avoid false positives, hence we wait tcs to send MountPosition
            //once this happens target should have been updated in adc also, so it can start its movement now.
            par(
                    { tcsSequencer.submitAndWait(sequenceOf(tcsPreset)) },
                    {
                        var tcsMovementStarted = false
                        val mcsMountPositionKey = EventKey("TCS.MCSAssembly.MountPosition").key()
                        val subscription = onEvent(mcsMountPositionKey) { _ ->
                            tcsMovementStarted = true
                        }
                        waitFor {
                            if (tcsMovementStarted) subscription.cancel()
                            tcsMovementStarted
                        }
                        irisSequencer.submitAndWait(sequenceOf(irisSetup))
                    }
            )

            publishEvent(presetEnd(obsId))
        }

        onObserve("fineAcquisition") {
            info("In FineAcquisition handler. Currently NOOP.")
        }

        onSetup("setupObservation") { command ->
            val obsId = getObsId(command)
            publishEvent(scitargetAcqStart(obsId))

            par(
                    { tcsSequencer.submitAndWait(sequenceOf(command)) },
                    { irisSequencer.submitAndWait(sequenceOf(command)) }
            )
            publishEvent(scitargetAcqEnd(obsId))
        }

        onSetup("observationEnd") { command ->
            val obsId = getObsId(command)
            irisSequencer.submitAndWait(sequenceOf(command))
            publishEvent(observationEnd(obsId))
        }
    }
}

fun getExposureId(obsId: String?, typLevel: String, observeCounter: Int, det: String): String {
    val exposureNumber = ExposureNumber("%04d".format(observeCounter))
    val typLevelStr = TYPLevel(typLevel + 1)
    return obsId?.let { "$it-$IRIS-$det-$typLevelStr-$exposureNumber" }
            ?: StandaloneExposureId(UTCTime.now(), IRIS, det, typLevelStr, exposureNumber).toString()

}

fun observeWithExposureId(observe: Observe, observeCounter: Int, det: String, exposureTypekey: Key<String>): String {
    val imageExposureType = observe(exposureTypekey).head()
    val obsId = getObsId(observe).toString()
    return getExposureId(obsId, imageExposureType, observeCounter, det)
}
