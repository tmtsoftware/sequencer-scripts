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
    println("Loaded aps setupAcquisition")


    onSetup("selectStar") {
        println("setupAcquisition::onSetup::selectStar")

        Thread.sleep(1_000)  // simulate time spent
        println("setupAcquisition::onSetup::selectStar::COMPLETED")

    }

    onSetup("stopPitLoop") {
        println("setupAcquisition::onSetup::stopPitLoop")

        Thread.sleep(100)  // simulate time spent
        println("setupAcquisition::onSetup::stopPitLoop::COMPLETED")
    }

    onSetup("sendStarInfoToEsw") {
        println("setupAcquisition::onSetup::sendStarInfoToEsw")

        Thread.sleep(100)  // simulate time spent

        println("setupAcquisition::onSetup::sendStarInfoToEsw::COMPLETED")
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
