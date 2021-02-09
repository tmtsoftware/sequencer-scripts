package aps.setupAps

import csw.params.commands.CommandResponse
import csw.params.commands.SequenceCommand
import csw.params.commands.Sequence
import csw.params.core.models.Id
import csw.prefix.models.Subsystem
import esw.ocs.api.models.ObsMode
import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.ESW
import esw.ocs.dsl.highlevel.models.LGSF
import esw.ocs.dsl.highlevel.models.APS
import esw.ocs.dsl.par
import esw.ocs.dsl.params.booleanKey
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds
import kotlin.time.seconds


script {
    println("Loaded aps setupAps")


    val defaultTimeout = 20.seconds
    val getRefMapSequencer = Sequencer(APS, ObsMode("getRefMap"), defaultTimeout)
    val setupAcquisitionSequencer = Sequencer(APS, ObsMode("setupAcquisition"), defaultTimeout)

    suspend fun setupAndRunSetupAcquisition(needStarAcq: Boolean) : CommandResponse.SubmitResponse {



        if (needStarAcq) {

            // setup and submit sequence to take exposure, find and identify, calc PR offsets and correct the PIT tracking

            val selectStarCommand: SequenceCommand = Setup("APS.setupAndRunSetupAcquisition", "selectStar")
            val stopPitLoopCommand: SequenceCommand = Setup("APS.setupAndRunSetupAcquisition", "stopPitLoop")
            val sendStarInfoToEswCommand: SequenceCommand = Setup("APS.setupAndRunSetupAcquisition", "sendStarInfoToEsw")

            val acqSequence: Sequence = sequenceOf(selectStarCommand, stopPitLoopCommand, sendStarInfoToEswCommand)
            val acqSequenceResponse: CommandResponse.SubmitResponse = setupAcquisitionSequencer.submitAndWait(acqSequence)
            println(" response = $acqSequenceResponse")
            return acqSequenceResponse;
         }


        return CommandResponse.Completed(Id(""))
    }

    suspend fun setupAndRunGetRefMap(needNewRefMap: Boolean) : CommandResponse.SubmitResponse {


        // setup and submit sequence to calculate centroid offsets, M2 PTT and Segment PTT in parallel with exposure sequence

        val configureApsForShProcedureCommand: SequenceCommand = Setup("APS.setupAps", "configureApsForShProcedure")
        val createRefMapCommand: SequenceCommand = Setup("APS.setupAps", "callRefMapSequence")

        // the sequence either contains both setting up APS for the procedure AND creating a ref map, or just setting up for the case where a new ref map is not needed
        val getRefMapSequence: Sequence = if (needNewRefMap) sequenceOf(configureApsForShProcedureCommand, createRefMapCommand) else sequenceOf(configureApsForShProcedureCommand)

        val getRefMapResponse: CommandResponse.SubmitResponse = getRefMapSequencer.submitAndWait(getRefMapSequence)
        println(" response = $getRefMapResponse")

        return getRefMapResponse;

    }



    onSetup("start") { command ->
        println("onSetup::start")

        // extract parameter values from command
        val needNewRefMapKey = booleanKey("needNewRefMap")
        val needStarAcqKey = booleanKey("needStarAcq")
        val needNewRefMap = command.parameter(needNewRefMapKey).head()!!
        val needStarAcq = command.parameter(needStarAcqKey).head()!!


        val responses: List<CommandResponse.SubmitResponse> = par(
                {setupAndRunGetRefMap(needNewRefMap)},
                {setupAndRunSetupAcquisition(needStarAcq)}
        )

    }


    onSetup("secondCommand") { command ->
        println("onSetup::secondCommand")

    }





    // val testAssembly = Assembly(ESW, "test", defaultTimeout)

    /*
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
        val tcsSequencer = Sequencer(TCS, "darknight", defaultTimeout)
        tcsSequencer.submitAndWait(sequence, 10.milliseconds)
    }

    onSetup("command-lgsf") {
        // NOT update command response to avoid sequencer to finish immediately
        // so that other Add, Append command gets time
        val setupCommand = Setup("LGSF.test", "command-lgsf")
        lgsfSequencer.submitAndWait(sequenceOf(setupCommand), 10.milliseconds)
    }
    */
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
