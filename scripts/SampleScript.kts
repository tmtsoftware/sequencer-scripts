import csw.params.commands.CommandName
import csw.params.commands.Sequence
import csw.params.commands.SequenceCommand
import csw.params.commands.Setup
import csw.params.core.models.Id
import csw.params.core.models.Prefix
import esw.ocs.dsl.core.script
import scala.jdk.javaapi.CollectionConverters
import java.util.*

script {
    val lgsfSequencer = Sequencer("lgsf", "darknight")
    val testAssembly = Assembly("test")

    onSetup("command-for-assembly") { command ->
        testAssembly.submit(command)
    }

    onSetup("command-4") {
        // try sending concrete sequence
        val setupCommand = Setup(
                Prefix("TCS.test"),
                CommandName("command-3"),
                Optional.ofNullable(null)
        )
        val sequence = Sequence(
                Id("testSequenceIdString123"),
                CollectionConverters.asScala(Collections.singleton<SequenceCommand>(setupCommand)).toSeq()
        )

        // ESW-88, ESW-145, ESW-195
        val tcsSequencer = Sequencer("tcs", "darknight")
        tcsSequencer.submitAndWait(sequence)
    }

    onSetup("command-lgsf") {
        // NOT update command response to avoid sequencer to finish immediately
        // so that other Add, Append command gets time
        val setupCommand = setup("LGSF.test", "command-lgsf")
        val sequence = Sequence(
                Id("testSequenceIdString123"),
                CollectionConverters.asScala(Collections.singleton<SequenceCommand>(setupCommand)).toSeq()
        )

        lgsfSequencer.submitAndWait(sequence)
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
        lgsfSequencer.abortSequence()
    }

    onStop {
        //do some actions to stop

        //send stop command to downstream sequencer
        lgsfSequencer.stop()
    }

}
