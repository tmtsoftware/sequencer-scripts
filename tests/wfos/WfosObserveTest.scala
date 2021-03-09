package wfos

import akka.actor.typed.scaladsl.ActorContext
import csw.command.client.messages.TopLevelActorMessage
import csw.framework.models.CswContext
import csw.framework.scaladsl.ComponentHandlers
import csw.location.api.models.TrackingEvent
import csw.params.commands.CommandResponse.Completed
import csw.params.commands._
import csw.params.core.generics.KeyType
import csw.params.core.models.Id
import csw.prefix.models.Prefix
import csw.prefix.models.Subsystem.WFOS
import csw.time.core.models.UTCTime
import esw.ocs.api.SequencerApi
import esw.ocs.api.models.ObsMode
import esw.ocs.testkit.EswTestKit

class WfosObserveTest extends EswTestKit {
  private var wfosSequencer: SequencerApi = _

  // TODO this can be simplified using DefaultComponentHandlers when added to CSW
  // https://github.com/tmtsoftware/csw/pull/367
  spawnAssembly(Prefix(WFOS, "detectorAssembly"), (ctx: ActorContext[TopLevelActorMessage], cswCtx: CswContext) => new ComponentHandlers(ctx, cswCtx) {
    override def initialize(): Unit = {}
    override def onLocationTrackingEvent(trackingEvent: TrackingEvent): Unit = {}
    override def validateCommand(runId: Id, controlCommand: ControlCommand): CommandResponse.ValidateCommandResponse = CommandResponse.Accepted(runId)
    override def onSubmit(runId: Id, controlCommand: ControlCommand): CommandResponse.SubmitResponse = CommandResponse.Completed(runId)
    override def onOneway(runId: Id, controlCommand: ControlCommand): Unit = CommandResponse.Completed(runId)
    override def onDiagnosticMode(startTime: UTCTime, hint: String): Unit = {}
    override def onOperationsMode(): Unit = {}
    override def onShutdown(): Unit = {}
    override def onGoOffline(): Unit = {}
    override def onGoOnline(): Unit = {}
  })

  override protected def beforeEach(): Unit = {
    spawnSequencer(WFOS, ObsMode("WFOS_simple"))
    wfosSequencer = sequencerClient(WFOS, ObsMode("WFOS_simple"))
  }

  override protected def afterEach(): Unit = {
    shutdownAllSequencers()
  }

  "Repeated Observe command" should {
    "take repeated exposures" in {
      val command = Observe(Prefix("WFOS.test"), CommandName("repeatedObserve")).add(KeyType.IntKey.make("repeats").set(1))
      wfosSequencer.submitAndWait(Sequence(command)).futureValue shouldBe a[Completed]
    }

    "return error when taking repeated exposure but repeats not specified" in {
      val command = Observe(Prefix("WFOS.test"), CommandName("repeatedObserve"))
      val response = wfosSequencer.submitAndWait(Sequence(command)).futureValue
      response shouldBe a[CommandResponse.Error]
      // TODO update when CSW is updated with better error message
      // https://github.com/tmtsoftware/csw/pull/371
      println(response.asInstanceOf[CommandResponse.Error].message)
    }
  }
}
