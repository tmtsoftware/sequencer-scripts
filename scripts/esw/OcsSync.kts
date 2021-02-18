package esw

import esw.ocs.api.models.ObsMode
import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.AOESW
import esw.ocs.dsl.highlevel.models.TCS
import esw.ocs.dsl.par
import esw.ocs.dsl.params.floatKey
import esw.ocs.dsl.params.invoke
import esw.ocs.dsl.params.taiTimeKey
import kotlin.time.seconds

script {
    val aosq = Sequencer(AOESW, ObsMode("OcsSync"), 5.seconds)
    val tcs = Sequencer(TCS, ObsMode("OcsSync"), 5.seconds)

    val offsetTime = taiTimeKey(name = "scheduledTime")
    val offsetXKey = floatKey("x")
    val offsetYKey = floatKey("y")

    val aoOffsetTime = taiTimeKey(name = "scheduledTime")
    val aoOffsetXKey = floatKey("x")
    val aoOffsetYKey = floatKey("y")

    val tcsOffsetTime = taiTimeKey(name = "scheduledTime")
    val tcsOffsetXKey = floatKey("x")
    val tcsOffsetYKey = floatKey("y")

    data class OffsetPair(val x: Float, val y: Float)

    fun transformOffsetToAo(x: Float, y: Float): OffsetPair  {
        // some transformation
        return OffsetPair(x, y)
    }

    onSetup("offset") { command ->
        // extract parameters and time to perform offset move from command
        val scheduledTime = command(offsetTime).head()
        val offsetX = command(offsetXKey).head()
        val offsetY = command(offsetYKey).head()

        val aoOffset = transformOffsetToAo(offsetX, offsetY)

        val aoCommand = Setup(prefix, "scheduledOffset", command.obsId)
                .add(aoOffsetXKey.set(aoOffset.x))
                .add(aoOffsetYKey.set(aoOffset.y))
                .add(aoOffsetTime.set(scheduledTime))

        val tcsCommand = Setup(prefix, "scheduledOffset", command.obsId)
                .add(tcsOffsetXKey.set(offsetX))
                .add(tcsOffsetYKey.set(offsetY))
                .add(tcsOffsetTime.set(scheduledTime))

        par (
                { tcs.submitAndWait(sequenceOf(tcsCommand)) },
                { aosq.submitAndWait(sequenceOf(aoCommand)) }
        )
    }

    onShutdown {
        println("shutdown ocs")
    }
}