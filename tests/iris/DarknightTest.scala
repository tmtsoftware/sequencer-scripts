package iris

import csw.command.client.SequencerCommandServiceImpl
import csw.params.commands.CommandResponse.Completed
import csw.params.commands.{CommandName, Sequence, Setup}
import csw.prefix.models.Prefix
import csw.prefix.models.Subsystem.IRIS
import esw.ocs.api.models.ObsMode
import esw.ocs.testkit.EswTestKit

class DarknightTest extends EswTestKit {
  "should submit and process sequence | ESW-190, ESW-148" in {
    val sequencerLocation = spawnSequencer(IRIS, ObsMode("Darknight"))

    val command1 = Setup(Prefix("esw.test"), CommandName("test"), None)
    val sequence = Sequence(command1)

    val sequencerCommandService = new SequencerCommandServiceImpl(sequencerLocation)
    sequencerCommandService.submitAndWait(sequence).futureValue shouldBe a[Completed]
  }
}
