package tcs

import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.TCS
import esw.ocs.dsl.params.floatKey
import esw.ocs.dsl.params.invoke
import esw.ocs.dsl.params.taiTimeKey
import kotlin.time.seconds

script {
    val tcsOffsetTime = taiTimeKey("scheduledTime")
    val tcsOffsetXKey = floatKey("x")
    val tcsOffsetYKey = floatKey("y")
    val tpkOffsetXKey = floatKey("x")
    val tpkOffsetYKey = floatKey("y")
    val tpkAssembly = Assembly(TCS, "tpk", 5.seconds)

    onSetup("offset") { command ->
        // extract parameters and time to perform offset move from command
        val scheduledTime = command(tcsOffsetTime).head()
        val offsetX = command(tcsOffsetXKey).head()
        val offsetY = command(tcsOffsetYKey).head()

        // create parameters for probe assembly command
        val tpkOffsetXParam = tpkOffsetXKey.set(offsetX)
        val tpkOffsetYParam = tpkOffsetYKey.set(offsetY)

        // create command
        val tpkCommand = Setup(prefix, "scheduledOffset", command.obsId)
                .madd(tpkOffsetXParam, tpkOffsetYParam)

        // schedule command
        scheduleOnce(scheduledTime) {
            tpkAssembly.submitAndWait(tpkCommand)
        }
    }

    onShutdown {
        println("shutdown tcs")
    }
}