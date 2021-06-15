package aoesw

import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.NFIRAOS
import esw.ocs.dsl.params.floatKey
import esw.ocs.dsl.params.invoke
import esw.ocs.dsl.params.taiTimeKey
import kotlin.time.Duration

script {
    val aoeswOffsetTime = taiTimeKey(name = "scheduledTime")
    val aoeswOffsetXKey = floatKey("x")
    val aoeswOffsetYKey = floatKey("y")
    val probeOffsetXKey = floatKey("x")
    val probeOffsetYKey = floatKey("y")
    val probeAssembly = Assembly(NFIRAOS, "probeAssembly", Duration.seconds(5))

    onSetup("offset") { command ->

        // extract parameters and time to perform offset move from command
        val scheduledTime = command(aoeswOffsetTime).head()
        val offsetX = command(aoeswOffsetXKey).head()
        val offsetY = command(aoeswOffsetYKey).head()

        // create parameters for probe assembly command
        val probeOffsetXParam = probeOffsetXKey.set(offsetX)
        val probeOffsetYParam = probeOffsetYKey.set(offsetY)

        // create command
        val probeCommand = Setup(prefix, "scheduledOffset", command.obsId)
                .madd(probeOffsetXParam, probeOffsetYParam)

        // schedule command
        scheduleOnce(scheduledTime) {
            probeAssembly.submitAndWait(probeCommand)
        }
    }

    onShutdown {
        println("shutdown aoesw")
    }
}
