package aps

import csw.params.commands.CommandResponse.Completed
import csw.params.commands.{CommandName, Sequence, Setup}
import csw.prefix.models.Prefix
import csw.prefix.models.Subsystem.APS
import esw.ocs.api.SequencerApi
import esw.ocs.api.models.ObsMode
import esw.ocs.testkit.EswTestKit

class EventUnitTest extends EswTestKit {


  private var testEventClient: SequencerApi = _
  private val subsystem = APS
  private val obsMode = ObsMode("testEvent")


  override protected def beforeEach(): Unit = {
    spawnSequencer(subsystem, ObsMode("testEvent"))

    testEventClient = sequencerClient(subsystem, ObsMode("testEvent"))

  }

  override protected def afterEach(): Unit = shutdownAllSequencers()


  "should publish events" in {

    val command1 = Setup(Prefix("esw.test"), CommandName("publish-event"), None)
    val sequence1 = Sequence(command1)

    testEventClient.submitAndWait(sequence1).futureValue shouldBe a[Completed]

  }


}


