package esw

import esw.ocs.api.models.ObsMode
import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.ESW
import esw.ocs.dsl.highlevel.models.WFOS
import kotlin.time.milliseconds
import kotlin.time.seconds

script {
    println("********** Loaded esw calib new script *********")
    val defaultTimeout = 5.seconds
    val wfosSequencer = Sequencer(WFOS, ObsMode("WFOS_Calib"), defaultTimeout)
    val testAssembly = Assembly(ESW, "test", defaultTimeout)

    onSetup("command-for-assembly") { command ->
        testAssembly.submit(command)
    }

    onSetup("command-4") {
        // try sending concrete sequence
        val setupCommand = Setup(
                "TCS.test",
                "command-3"
        )
        val sequence = sequenceOf(setupCommand)

        // ESW-88, ESW-145, ESW-195
        val wfosSequencer = Sequencer(WFOS, ObsMode("WFOS_Calib"), defaultTimeout)
        wfosSequencer.submitAndWait(sequence, 10.milliseconds)
    }

    onSetup("command-lgsf") {
        // NOT update command response to avoid sequencer to finish immediately
        // so that other Add, Append command gets time
        val setupCommand = Setup("LGSF.test", "command-lgsf")
        wfosSequencer.submitAndWait(sequenceOf(setupCommand), 10.milliseconds)
    }

    onDiagnosticMode { startTime, hint ->
        // do some actions to go to diagnostic mode based on hint
        testAssembly.diagnosticMode(startTime, hint)
    }

    onOperationsMode {
        // do some actions to go to operations mode
        testAssembly.operationsMode()
    }

    onGoOffline {
        // do some actions to go offline
        testAssembly.goOffline()
    }

    onGoOnline {
        // do some actions to go online
        testAssembly.goOnline()
    }

    onAbortSequence {
        //do some actions to abort sequence

        //send abortSequence command to downstream sequencer
        wfosSequencer.abortSequence()
    }

    onStop {
        //do some actions to stop

        //send stop command to downstream sequencer
        wfosSequencer.stop()
    }

}
