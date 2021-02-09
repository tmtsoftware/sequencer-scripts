package aps.setupAps

import csw.params.commands.CommandResponse
import csw.params.commands.SequenceCommand
import csw.params.commands.Sequence
import csw.prefix.models.Subsystem
import esw.ocs.api.models.ObsMode
import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.ESW
import esw.ocs.dsl.highlevel.models.LGSF
import esw.ocs.dsl.highlevel.models.APS
import kotlin.time.milliseconds
import kotlin.time.seconds

script {
    println("Loaded aps createRefMap")

    val defaultTimeout = 20.seconds
    val createRefMapSequencer = Sequencer(APS, ObsMode("createRefMap"), defaultTimeout)


    onSetup("configureApsForShProcedure") {
        println("getRefMap::onSetup::configureApsForShProcedure")

        Thread.sleep(5_000)  // simulate time spent

        println("getRefMap::onSetup::configureApsForShProcedure::COMPLETED")
    }

    onSetup("callRefMapSequence") {
        println("getRefMap::onSetup::checkRefMap")

        // There may be more commands in the final version of createRefMap sequence, but for now just one
        val startCommand: SequenceCommand = Setup("APS.getRefMap", "start")

        val createRefMapSequence: Sequence = sequenceOf(startCommand)
        val createRefMapResponse: CommandResponse.SubmitResponse = createRefMapSequencer.submitAndWait(createRefMapSequence)
        println(createRefMapResponse)

    }




    onDiagnosticMode { startTime, hint ->
        // do some actions to go to diagnostic mode based on hint
        //testAssembly.diagnosticMode(startTime, hint)
    }

    onOperationsMode {
        // do some actions to go to operations mode
        //testAssembly.operationsMode()
    }

    onGoOffline {
        // do some actions to go offline
        //testAssembly.goOffline()
    }

    onGoOnline {
        // do some actions to go online
        //testAssembly.goOnline()
    }

    onAbortSequence {
        //do some actions to abort sequence

        //send abortSequence command to downstream sequencer
        //lgsfSequencer.abortSequence()
    }

    onStop {
        //do some actions to stop

        //send stop command to downstream sequencer
        //lgsfSequencer.stop()
    }

}
