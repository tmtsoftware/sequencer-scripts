package tcs

import common.*
import csw.params.events.SystemEvent
import degreeToArcSec
import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.RADEC
import esw.ocs.dsl.highlevel.models.TCS
import esw.ocs.dsl.params.invoke
import esw.ocs.dsl.params.params
import getMountPositionError
import kotlin.math.abs

script {
    val pkAssembly = Assembly(TCS, "PointingKernelAssembly")

    val slewToTargetTolerance = actorSystem.settings().config().getString("tcs.slew_to_target_tolerance").toDouble()
    val setOffsetTolerance = actorSystem.settings().config().getString("tcs.set_offset_tolerance").toDouble()

    onSetup("preset") { command ->
        val obsId = getObsId(command).toString()
        val incomingBaseParamValue = command.params(baseCoordKey).head()
        val parameterTobeSend = baseKey.set(incomingBaseParamValue)
        val slewToTarget = Setup(prefix, "SlewToTarget", obsId).madd(parameterTobeSend)
        sendCommandAndLog(pkAssembly, slewToTarget)

        var mcsMountPositionWithinError = false
        var encBasePositionWithinError = false
        var encCapPositionWithinError = false

        val mcsMountPositionKey = EventKey("TCS.MCSAssembly.MountPosition").key()
        val encCurrentPositionKey = EventKey("TCS.ENCAssembly.CurrentPosition").key()
        val subscription = onEvent(mcsMountPositionKey, encCurrentPositionKey) { event ->
            when (event) {
                is SystemEvent ->
                    when (event.eventName().name()) {
                        "MountPosition" -> {
                            if (event.paramSet().size() >= 2) {
                                val error = getMountPositionError(event)
                                logger.info("$prefix : MountPosition error: ${error.toArcSec()}")
                                mcsMountPositionWithinError = error.toArcSec() < slewToTargetTolerance
                            }
                        }
                        "CurrentPosition" -> {

                            if (event.paramSet().size() >= 4) {

                                val baseCurrentValue = event(baseCurrentKey).head()
                                val capCurrentValue = event(capCurrentKey).head()
                                val baseDemandValue = event(baseDemandKey).head()
                                val capDemandValue = event(capDemandKey).head()

                                val capError = abs(degreeToArcSec(capCurrentValue) - degreeToArcSec(capDemandValue))
                                logger.info("$prefix : cap current error: $capError")
                                encCapPositionWithinError = capError < slewToTargetTolerance
                                val baseError = abs(degreeToArcSec(baseCurrentValue) - degreeToArcSec(baseDemandValue))
                                logger.info("$prefix : base current error: $baseError")
                                encBasePositionWithinError = baseError < slewToTargetTolerance

                            }
                        }
                    }
            }
        }

        waitFor {
            mcsMountPositionWithinError && encBasePositionWithinError && encCapPositionWithinError
        }
        subscription.cancel()
    }

    onSetup("setupObservation") { command ->
        val obsId = getObsId(command)
        val pParamValue = command.params(pKey).head()
        val qParamValue = command.params(qKey).head()
        val parameterXCoordinate = xCoordinateKey.set(pParamValue)
        val parameterYCoordinate = yCoordinateKey.set(qParamValue)
        val icrsFrame = refFrameKey.set(icrsChoice)
        val publishOffsetEvents = pParamValue > 0 || qParamValue > 0
        if(publishOffsetEvents) {
            publishEvent(offsetStart(obsId, RADEC, pParamValue, qParamValue))
        }
        val setOffset = Setup(prefix, "SetOffset", obsId.toString()).madd(parameterXCoordinate, parameterYCoordinate, icrsFrame)
        sendCommandAndLog(pkAssembly, setOffset)

        var withinError = false

        val mcsMountPositionKey = EventKey("TCS.MCSAssembly.MountPosition").key()
        val subscription = onEvent(mcsMountPositionKey) { event ->
            when (event) {
                is SystemEvent -> {
                    val error = getMountPositionError(event)
                    logger.info("$prefix : MountPosition error: ${error.toArcSec()}")
                    withinError = error.toArcSec() < setOffsetTolerance
                }
            }
        }
        waitFor {
            withinError
        }
        subscription.cancel()
        if(publishOffsetEvents) {
            publishEvent(offsetEnd(obsId))
        }
    }
}
