package wfos

import akka.actor.typed.scaladsl.ActorContext
import com.typesafe.config.ConfigFactory
import csw.command.client.messages.TopLevelActorMessage
import csw.framework.models.CswContext
import csw.framework.scaladsl.{ComponentBehaviorFactory, ComponentHandlers}
import csw.location.api.models.TrackingEvent
import csw.params.commands.CommandResponse.Completed
import csw.params.commands.{CommandName, CommandResponse, ControlCommand, Observe, Sequence, Setup}
import csw.params.core.generics.KeyType
import csw.params.core.models.Id
import csw.prefix.models.Prefix
import csw.prefix.models.Subsystem.WFOS
import csw.time.core.models.UTCTime
import esw.ocs.api.SequencerApi
import esw.ocs.api.actor.client.SequencerApiFactory
import esw.ocs.api.models.ObsMode
import esw.ocs.testkit.EswTestKit

class WfosObserveTest extends EswTestKit {
  private var wfosSequencer: SequencerApi = _

  override protected def beforeEach(): Unit = {
    spawnSequencer(WFOS, ObsMode("WFOS_simple"))
    wfosSequencer = sequencerClient(WFOS, ObsMode("WFOS_simple"))
    spawnAssembly(Prefix(WFOS, "detectorAssembly"), new ComponentBehaviorFactory {
      override protected def handlers(ctx: ActorContext[TopLevelActorMessage], cswCtx: CswContext): ComponentHandlers = new ComponentHandlers(ctx, cswCtx) {
        override def initialize(): Unit = ???

        override def onLocationTrackingEvent(trackingEvent: TrackingEvent): Unit = ???

        override def validateCommand(runId: Id, controlCommand: ControlCommand): CommandResponse.ValidateCommandResponse = CommandResponse.Accepted(runId)

        override def onSubmit(runId: Id, controlCommand: ControlCommand): CommandResponse.SubmitResponse = CommandResponse.Completed(runId)

        override def onOneway(runId: Id, controlCommand: ControlCommand): Unit = CommandResponse.Completed(runId)

        override def onDiagnosticMode(startTime: UTCTime, hint: String): Unit = ???

        override def onOperationsMode(): Unit = ???

        override def onShutdown(): Unit = ???

        override def onGoOffline(): Unit = ???

        override def onGoOnline(): Unit = ???
      }
    })
  }

  "should take exposure" in {
    val command = Observe(Prefix("WFOS.test"), CommandName("observe")).add(KeyType.IntKey.make("repeats").set(1))
    wfosSequencer.submitAndWait(Sequence(command)).futureValue shouldBe a[Completed]
  }
}
