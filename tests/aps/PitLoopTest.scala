package aps

import csw.params.commands.CommandResponse.Completed
import csw.params.commands.{CommandName, Sequence, Setup}
import csw.prefix.models.Prefix
import csw.prefix.models.Subsystem.APS
import esw.ocs.api.SequencerApi
import esw.ocs.api.models.ObsMode
import esw.ocs.testkit.EswTestKit

class PitLoopTest extends EswTestKit {

  /*

  private var pitLoopSequencerClient: SequencerApi = _
  private var achieveStableTrackSequencerClient: SequencerApi = _
  private val subsystem = APS
  private val obsMode = ObsMode("pitLoop")


  override protected def beforeEach(): Unit = {
    spawnSequencer(subsystem, ObsMode("pitLoop"))
    //spawnSequencer(subsystem, ObsMode("achieveStablePitTracking"))

    pitLoopSequencerClient = sequencerClient(subsystem, ObsMode("pitLoop"))
    //achieveStableTrackSequencerClient = sequencerClient(subsystem, ObsMode("achieveStablePitTracking"))
  }

  override protected def afterEach(): Unit = shutdownAllSequencers()

  /*
  "should submitAndWait sequence and get Completed" in {

    val command1 = Setup(Prefix("esw.test"), CommandName("start-pit-loop"), None)
    val sequence1 = Sequence(command1)
    val command2 = Setup(Prefix("esw.test"), CommandName("stop-pit-loop"), None)
    val sequence2 = Sequence(command2)

    pitLoopSequencerClient.submitAndWait(sequence1).futureValue shouldBe a[Completed]
    Thread.sleep(10000)  // wait 10 seconds so that some looping occurs
    pitLoopSequencerClient.submitAndWait(sequence2).futureValue shouldBe a[Completed]
    Thread.sleep(10000)
    pitLoopSequencerClient.submitAndWait(sequence1).futureValue shouldBe a[Completed]
    Thread.sleep(10000)  // wait 10 seconds so that some looping occurs
    pitLoopSequencerClient.submitAndWait(sequence2).futureValue shouldBe a[Completed]
    Thread.sleep(1100)  // wait 1 second for completion

  }
*/


  "should send messages to pitloop and wait until stable" in {

    val command1 = Setup(Prefix("esw.test"), CommandName("start-pit-loop"), None)
    val sequence1 = Sequence(command1)
    val command2 = Setup(Prefix("esw.test"), CommandName("wait-for-pit-stable"), None)
    val sequence2 = Sequence(command2)

    pitLoopSequencerClient.submitAndWait(sequence1).futureValue shouldBe a[Completed]
    Thread.sleep(10000)  // wait 10 seconds so that some looping occurs

    //achieveStableTrackSequencerClient.submitAndWait(sequence2).futureValue shouldBe a[Completed]

  }


   */

}


