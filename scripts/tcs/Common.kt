import common.currentAltAzCoordKey
import common.demandAltAzCoordKey
import csw.params.core.models.Angle
import csw.params.events.SystemEvent
import esw.ocs.dsl.params.invoke

fun getMountPositionError(event: SystemEvent): Double {
    val current = event(currentAltAzCoordKey).head()
    val demand = event(demandAltAzCoordKey).head()
    return Angle.distance(current.alt().toRadian(), current.az().toRadian(), demand.alt().toRadian(), demand.az().toRadian())
}