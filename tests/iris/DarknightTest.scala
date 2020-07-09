package iris

import csw.params.commands.CommandResponse.{Completed, Started}
import csw.params.commands.{CommandName, Sequence, Setup}
import csw.prefix.models.Prefix
import csw.prefix.models.Subsystem.IRIS
import esw.ocs.api.SequencerApi
import esw.ocs.api.models.ObsMode
import esw.ocs.testkit.EswTestKit

class DarknightTest extends EswTestKit {
  private var irisSequencerClient: SequencerApi = _
  private val subsystem = IRIS
  private val obsMode = ObsMode("darknight")
  private val command1 = Setup(Prefix("esw.test"), CommandName("test"), None)
  private val sequence = Sequence(command1)


  override protected def beforeEach(): Unit = {
    spawnSequencer(subsystem, obsMode)
    irisSequencerClient = sequencerClient(subsystem, obsMode)
  }

  override protected def afterEach(): Unit = shutdownAllSequencers()

  "should submitAndWait sequence and get Completed" in {

    irisSequencerClient.submitAndWait(sequence).futureValue shouldBe a[Completed]
  }

  "should submit sequence and get Started and then Completed on queryFinal" in {

    val submitResponse = irisSequencerClient.submit(sequence).futureValue
    submitResponse shouldBe a[Started]
    irisSequencerClient.queryFinal(submitResponse.runId).futureValue shouldBe a[Completed]
  }
}
