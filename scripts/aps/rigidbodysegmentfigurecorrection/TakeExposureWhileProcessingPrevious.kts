package aps.rigidbodysegmentfigurecorrection

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
import kotlin.time.milliseconds
import kotlin.time.seconds


script {
    println("Loaded aps takeExposureWhileProcessingPrevious")

    val defaultTimeout = 5.seconds
    val exposureSequencer = Sequencer(APS, ObsMode("takeExposureAndCorrectPit"), defaultTimeout)
    val calcSequencer = Sequencer(APS, ObsMode("calcRigidBodyAndSegmentPtt"), defaultTimeout)

    suspend fun setupAndRunExposureSequence(loopCount: Int, maxCount: Int) : CommandResponse.SubmitResponse {



        if (loopCount != maxCount) {

            // setup and submit sequence to take exposure, find and identify, calc PR offsets and correct the PIT tracking

            val takeShExposureCommand: SequenceCommand = Setup("APS.takeExposureWhileProcessingPrevious", "takeShExposure")
            val findAndIdentifyCommand: SequenceCommand = Setup("APS.takeExposureWhileProcessingPrevious", "findAndIdentify")
            val calcImageAndPrOffsetsCommand: SequenceCommand = Setup("APS.takeExposureWhileProcessingPrevious", "calcImageAndPrOffsets")
            val correctPitTrackingCommand: SequenceCommand = Setup("APS.takeExposureWhileProcessingPrevious", "correctPitTracking")
            val exposureSequence: Sequence = sequenceOf(takeShExposureCommand, findAndIdentifyCommand, calcImageAndPrOffsetsCommand, correctPitTrackingCommand)
            val exposureResponse: CommandResponse.SubmitResponse = exposureSequencer.submit(exposureSequence)
            //println(exposureResponse)
            val finalExposureResponse: CommandResponse.SubmitResponse = exposureSequencer.queryFinal(exposureResponse.runId())

            // This is where we would deal with exposure problems, retaking frames or aborting
             return finalExposureResponse
        }


        return CommandResponse.Cancelled(Id(""))
    }

    suspend fun setupAndRunCalcSequence(loopCount: Int, maxCount: Int) : CommandResponse.SubmitResponse {

        if (loopCount != 0) {

            // setup and submit sequence to calculate centroid offsets, M2 PTT and Segment PTT in parallel with exposure sequence

            val calcCentroidOffsetsCommand: SequenceCommand = Setup("APS.takeExposureWhileProcessingPrevious", "calcCentroidOffsets")
            val calcM2PttCommand: SequenceCommand = Setup("APS.takeExposureWhileProcessingPrevious", "calcM2Ptt")
            val calcSegPttCommand: SequenceCommand = Setup("APS.takeExposureWhileProcessingPrevious", "calcSegPtt")
            val calcSequence: Sequence = sequenceOf(calcCentroidOffsetsCommand, calcM2PttCommand, calcSegPttCommand)
            val calcResponse: CommandResponse.SubmitResponse = calcSequencer.submit(calcSequence)
            //println(calcResponse)
            val finalCalcResponse: CommandResponse.SubmitResponse = calcSequencer.queryFinal(calcResponse.runId())

            return finalCalcResponse
        }

        return CommandResponse.Cancelled(Id(""))

    }





    suspend fun executeSingleLoop(loopCount: Int, maxCount: Int) {

        val responses: List<CommandResponse.SubmitResponse> = par(
                {setupAndRunExposureSequence(loopCount, maxCount)},
                {setupAndRunCalcSequence(loopCount, maxCount)}
        )
        // at this stage, both the sequences are finished  - this is another area where exceptions can be handled, but the logic is trickier
        // as we don't want to calc anything from a previous loop that was deemed unacceptable
        // however, a FIFO of acceptable frames for processing would work better in this situation.  If nothing is in the buffer, no processing
        // needed.

     }

    onSetup("takeExposureWhileProcessingPrevious") {
        println("onSetup::takeExposureWhileProcessingPrevious")

        // TODO: should be a better way than this
        executeSingleLoop(0, 7)
        executeSingleLoop(1, 7)
        executeSingleLoop(2, 7)
        executeSingleLoop(3, 7)
        executeSingleLoop(4, 7)
        executeSingleLoop(5, 7)
        executeSingleLoop(6, 7)
        executeSingleLoop(7, 7)

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
