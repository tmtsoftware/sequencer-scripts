//@file:Repository("https://jitpack.io/")
//@file:DependsOn("com.github.tmtsoftware.esw:esw-ocs-dsl-kt_2.13:adc26faf3413a9e70a6627c397563e88ea04afb6")
//@file:DependsOn("com.github.tmtsoftware.esw:esw-ocs-app_2.13:adc26faf3413a9e70a6627c397563e88ea04afb6")

@file:Import("../common/CommonUtils.seq.kts")
@file:Import("../common/Keys.seq.kts")
@file:Import("../common/Utils.seq.kts")

//package tcs
//import common.*
//
//import common.currentEqCoordKey
//import common.demandEqCoordKey

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