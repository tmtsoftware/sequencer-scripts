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
  private val obsMode = ObsMode("pitLoop")

  
  override protected def beforeEach(): Unit = {
    spawnSequencer(subsystem, obsMode)

    apsSequencerClient = sequencerClient(subsystem, obsMode)
  }

  override protected def afterEach(): Unit = shutdownAllSequencers()

  "should submitAndWait sequence and get Completed" in {

    val command1 = Setup(Prefix("esw.test"), CommandName("start-pit-loop"), None)
    val sequence1 = Sequence(command1)
    val command2 = Setup(Prefix("esw.test"), CommandName("stop-pit-loop"), None)
    val sequence2 = Sequence(command2)

    apsSequencerClient.submitAndWait(sequence1).futureValue shouldBe a[Completed]
    Thread.sleep(10000)  // wait 10 seconds so that some looping occurs
    apsSequencerClient.submitAndWait(sequence2).futureValue shouldBe a[Completed]
    Thread.sleep(10000)
    apsSequencerClient.submitAndWait(sequence1).futureValue shouldBe a[Completed]
    Thread.sleep(10000)  // wait 10 seconds so that some looping occurs
    apsSequencerClient.submitAndWait(sequence2).futureValue shouldBe a[Completed]
    Thread.sleep(1100)  // wait 1 second for completion
  }


}


