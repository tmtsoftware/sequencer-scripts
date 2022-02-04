import common.currentEqCoordKey
import common.demandEqCoordKey
import csw.params.core.models.Angle
import csw.params.events.SystemEvent
import esw.ocs.dsl.params.invoke

fun getMountPositionError(event: SystemEvent): Double {
    val current = event(currentEqCoordKey).head()
    val demand = event(demandEqCoordKey).head()
    return Angle.distance(current.ra().toRadian(), current.dec().toRadian(), demand.ra().toRadian(), demand.dec().toRadian())
}