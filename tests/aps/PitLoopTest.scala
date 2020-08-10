package aps

import csw.params.commands.CommandResponse.Completed
import csw.params.commands.{CommandName, Sequence, Setup}
import csw.prefix.models.Prefix
import csw.prefix.models.Subsystem.APS
import esw.ocs.api.SequencerApi
import esw.ocs.api.models.ObsMode
import esw.ocs.testkit.EswTestKit

class PitLoopTest extends EswTestKit {

  private var apsSequencerClient: SequencerApi = _
  private val subsystem = APS
  private val obsMode = ObsMode("takeExposureWhileProcessing")
  private val command1 = Setup(Prefix("esw.test"), CommandName("start-pit-loop"), None)
  private val sequence = Sequence(command1)
  
  override protected def beforeEach(): Unit = {
    spawnSequencer(subsystem, obsMode)

    apsSequencerClient = sequencerClient(subsystem, obsMode)
  }

  override protected def afterEach(): Unit = shutdownAllSequencers()


  "should submitAndWait sequence and get Completed" in {

    apsSequencerClient.submitAndWait(sequence).futureValue shouldBe a[Completed]
    Thread.sleep(10000)  // wait 10 seconds so that some looping occurs
  }

  /*
  "should submit sequence and get Started and then Completed on queryFinal" in {

    val submitResponse = apsSequencerClient.submit(sequence).futureValue
    submitResponse shouldBe a[Started]
    apsSequencerClient.queryFinal(submitResponse.runId).futureValue shouldBe a[Completed]
  }

   */
}


