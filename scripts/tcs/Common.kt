import common.currentAltAzCoordKey
import common.demandAltAzCoordKey
import csw.params.events.SystemEvent
import esw.ocs.dsl.params.invoke
import kotlin.math.sqrt

fun getMountPositionError(event: SystemEvent): Double {
    val current = event(currentAltAzCoordKey).head()
    val demand = event(demandAltAzCoordKey).head()

    val atlDiff = current.alt().`$minus`(demand.alt())
    val azDiff = current.az().`$minus`(demand.az())
    return sqrt(atlDiff.`$times`(2).`$plus`(azDiff.`$times`(2)).toDegree())
}