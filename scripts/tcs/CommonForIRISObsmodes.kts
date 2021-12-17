package tcs

import common.*
import csw.params.events.SystemEvent
import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.TCS
import esw.ocs.dsl.params.*
import kotlin.math.sqrt
import csw.params.core.models.Angle.*
import kotlin.math.abs

script {
    val pkAssembly = Assembly(TCS, "PointingKernelAssembly")

    onSetup("preset") { command ->
        val obsId = getObsId(command).toString()
        val incomingBaseParamValue = command.params(baseCoordKey).head()
        val parameterTobeSend = baseKey.set(incomingBaseParamValue)
        val slewToTarget = Setup(prefix, "SlewToTarget", obsId).madd(parameterTobeSend)
        pkAssembly.submitAndWait(slewToTarget)

        var mcsMountPositionWithinError = false
        var encBasePositionWithinError = false
        var encCapPositionWithinError = false

        val mcsMountPositionKey = EventKey("TCS.MCSAssembly.MountPosition").key()
        val encCurrentPositionKey = EventKey("TCS.ENCAssembly.CurrentPosition").key()
        onEvent(mcsMountPositionKey, encCurrentPositionKey) { event ->
            when (event) {
                is SystemEvent ->
                    when (event.eventName().name()) {
                        "MountPosition" -> {

                            val current = event(currentAltAzCoordKey).head()
                            val demand = event(demandAltAzCoordKey).head()

                            val atlDiff = current.alt().`$minus`(demand.alt())
                            val azDiff = current.az().`$minus`(demand.az())
                            val error = sqrt(atlDiff.`$times`(2).`$plus`(azDiff.`$times`(2)).toDegree())
                            val tolerance = actorSystem.settings().config().getString("tcs.position_tolerance")
                            println("tollerance from config: " + tolerance)
                            mcsMountPositionWithinError = error < 0.5
                        }
                        "CurrentPosition" -> {
                            val baseCurrentValue = event(baseCurrentKey).head()
                            val capCurrentValue = event(capCurrentKey).head()
                            val baseDemandValue = event(baseDemandKey).head()
                            val capDemandValue = event(capDemandKey).head()

                            encCapPositionWithinError = abs(capCurrentValue - capDemandValue) < 0.5
                            encBasePositionWithinError = abs(baseCurrentValue - baseDemandValue) < 0.5

                        }
                    }
            }
        }


        waitFor {
            mcsMountPositionWithinError && encBasePositionWithinError && encCapPositionWithinError
        }


    }

    onSetup("setupObservation") { command ->
//        val obsId = getObsId(command).toString()
//        val incomingBaseParamValue = command.params(baseCoordKey).head()
//        val parameterTobeSend = baseKey.set(incomingBaseParamValue)
//        val setOffset = Setup(prefix, "SetOffset", obsId).madd(paramSet)
//        pkAssembly.submitAndWait(setOffset)
    }

}