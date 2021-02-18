package esw

import esw.ocs.api.models.ObsMode
import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.AOESW
import esw.ocs.dsl.highlevel.models.IRIS
import esw.ocs.dsl.highlevel.models.TCS
import esw.ocs.dsl.par
import kotlin.time.minutes

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
                { tcs.submitAndWait(sequenceOf(slewTCSCommand), 2.minutes) },
                { aosq.submitAndWait(sequenceOf(slewAOSQCommand), 2.minutes)},
                { iris.submitAndWait(sequenceOf(slewIRISCommand), 2.minutes) }
        )
    }

    // 3.1.2 in Workflows
    onSetup("acquire") { command ->
        // configure OIWFS probes
        // enable Ttf in AOSQ
    }
}