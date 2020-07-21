package aps

import csw.params.commands.CommandResponse.{Completed, Started}
import csw.params.commands.{CommandName, Sequence, Setup}
import csw.prefix.models.Prefix
import csw.prefix.models.Subsystem.{APS, IRIS}
import esw.ocs.api.SequencerApi
import esw.ocs.api.models.ObsMode
import esw.ocs.testkit.EswTestKit

class Test1Test extends EswTestKit {

  private var apsSequencerClient: SequencerApi = _
  private val subsystem = APS
  private val obsMode = ObsMode("takeExposureWhileProcessingPrevious")
  private val command1 = Setup(Prefix("esw.test"), CommandName("takeExposureWhileProcessingPrevious"), None)
  private val sequence = Sequence(command1)
  
  override protected def beforeEach(): Unit = {
    spawnSequencer(subsystem, obsMode)
    spawnSequencer(subsystem, ObsMode("takeExposureAndCorrectPit"))
    spawnSequencer(subsystem, ObsMode("calcRigidBodyAndSegmentPtt"))
    apsSequencerClient = sequencerClient(subsystem, obsMode)
  }

  override protected def afterEach(): Unit = shutdownAllSequencers()

  "should submitAndWait sequence and get Completed" in {

    apsSequencerClient.submitAndWait(sequence).futureValue shouldBe a[Completed]
  }

  /*
  "should submit sequence and get Started and then Completed on queryFinal" in {

    val submitResponse = apsSequencerClient.submit(sequence).futureValue
    submitResponse shouldBe a[Started]
    apsSequencerClient.queryFinal(submitResponse.runId).futureValue shouldBe a[Completed]
  }

   */
}


