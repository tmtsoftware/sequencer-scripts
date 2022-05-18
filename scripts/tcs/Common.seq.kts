@file:Import("../common/CommonUtils.seq.kts")

import csw.params.core.models.Angle
import csw.params.events.SystemEvent
import esw.ocs.dsl.params.invoke

fun getMountPositionError(event: SystemEvent): Angle {
    val current = event(currentEqCoordKey).head()
    val demand = event(demandEqCoordKey).head()
    return Angle.double2angle(Angle.distance(current.ra().toRadian(), current.dec().toRadian(), demand.ra().toRadian(), demand.dec().toRadian())).radian() // converting distance in (radian) to Angle Class
}

// takes Anle input in degrees
fun degreeToArcSec(angle: Double) = Angle.double2angle(angle).degree().toArcSec()