package tcs

import common.*
import csw.params.events.SystemEvent
import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.TCS
import esw.ocs.dsl.params.*
import getMountPositionError
import kotlin.math.abs

script {
    val pkAssembly = Assembly(TCS, "PointingKernelAssembly")

    val tolerance = actorSystem.settings().config().getString("tcs.position_tolerance").toDouble()

    onSetup("preset") { command ->
        println("command received:" + command)
        val obsId = getObsId(command).toString()
        val incomingBaseParamValue = command.params(baseCoordKey).head()
        val parameterTobeSend = baseKey.set(incomingBaseParamValue)
        val slewToTarget = Setup(prefix, "SlewToTarget", obsId).madd(parameterTobeSend)
        val submitAndWait = pkAssembly.submitAndWait(slewToTarget)
        println("submitAndWait:" + submitAndWait.toString())

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


                            println("receicved1: " + event.toString())
                            val error = getMountPositionError(event)

                            mcsMountPositionWithinError = error < (tolerance * tolerance)
                            println("mcsMountPositionWithinError:" + mcsMountPositionWithinError + ":" + error)

                        }
                        "CurrentPosition" -> {
                            println("receicved2: " + event.toString())

                            val baseCurrentValue = event(baseCurrentKey).head()
                            val capCurrentValue = event(capCurrentKey).head()
                            val baseDemandValue = event(baseDemandKey).head()
                            val capDemandValue = event(capDemandKey).head()

                            val abs = abs(capCurrentValue - capDemandValue)
                            encCapPositionWithinError = abs < tolerance
                            val abs1 = abs(baseCurrentValue - baseDemandValue)
                            encBasePositionWithinError = abs1 < tolerance

                            println("encCapPositionWithinError:" + encCapPositionWithinError + ":" + abs)
                            println("encBasePositionWithinError:" + encBasePositionWithinError + ":" + abs1)
                        }
                    }
            }
        }


        waitFor {
            mcsMountPositionWithinError && encBasePositionWithinError && encCapPositionWithinError
        }


    }

    onSetup("setupObservation") { command ->
        val obsId = getObsId(command).toString()
        val pParamValue = command.params(pKey).head()
        val qParamValue = command.params(qKey).head()
        val parameterXCoordinate = xCoordinateKey.set(pParamValue)
        val parameterYCoordinate = yCoordinateKey.set(qParamValue)
        val setOffset = Setup(prefix, "SetOffset", obsId).madd(parameterXCoordinate, parameterYCoordinate)
        val submitAndWait = pkAssembly.submitAndWait(setOffset)
        println("submitAndWait2" + submitAndWait.toString())

        var withinError = false

        val mcsMountPositionKey = EventKey("TCS.MCSAssembly.MountPosition").key()
        onEvent(mcsMountPositionKey) { event ->
            when (event) {
                is SystemEvent -> {
                    println("receicved3: " + event.toString())

                    val error = getMountPositionError(event)
                    withinError = error < (tolerance * tolerance)
                }
            }
        }
        waitFor {
            withinError
        }
    }
}