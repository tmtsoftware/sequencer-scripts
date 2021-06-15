package esw

import esw.ocs.api.models.ObsMode
import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.AOESW
import esw.ocs.dsl.highlevel.models.IRIS
import esw.ocs.dsl.highlevel.models.TCS
import esw.ocs.dsl.par
import kotlin.time.Duration

script {
    val tcs = Sequencer(TCS, ObsMode("LGSAcquisition"))
    val aosq = Sequencer(AOESW, ObsMode("LGSAcquisition"))
    val iris = Sequencer(IRIS, ObsMode("LGSAcquisition"))

    // 3.1.1 in Workflows
    onSetup("preset") { command ->
        val slewAOSQCommand = Setup(prefix, "Slew AOSQ", command.obsId)
        val slewTCSCommand = Setup(prefix, "Slew TCS", command.obsId)
        val slewIRISCommand = Setup(prefix, "Slew IRIS", command.obsId)

        par (
                { tcs.submitAndWait(sequenceOf(slewTCSCommand), Duration.minutes(2)) },
                { aosq.submitAndWait(sequenceOf(slewAOSQCommand), Duration.minutes(2))},
                { iris.submitAndWait(sequenceOf(slewIRISCommand), Duration.minutes(2)) }
        )
    }

    // 3.1.2 in Workflows
    onSetup("acquire") { command ->
        // configure OIWFS probes
        // enable Ttf in AOSQ
    }
}