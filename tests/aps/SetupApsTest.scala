package aps

import csw.params.commands.CommandResponse.Completed
import csw.params.commands.{CommandName, Sequence, Setup}
import csw.params.core.generics.Key
import csw.prefix.models.Prefix
import csw.prefix.models.Subsystem.APS
import esw.ocs.api.SequencerApi
import esw.ocs.api.models.ObsMode
import esw.ocs.testkit.EswTestKit


import csw.params.commands.Setup
import csw.params.core.generics._

import scala.concurrent.duration._



class SetupApsTest extends EswTestKit {

  implicit val timeout = 30.seconds
  private var apsSequencerClient: SequencerApi = _
  private val subsystem = APS
  private val obsMode = ObsMode("setupAps")



  override protected def beforeEach(): Unit = {
    spawnSequencer(subsystem, obsMode)
    spawnSequencer(subsystem, ObsMode("getRefMap"))
    spawnSequencer(subsystem, ObsMode("setupAcquisition"))
    spawnSequencer(subsystem, ObsMode("createRefMap"))
    apsSequencerClient = sequencerClient(subsystem, obsMode)
  }

  override protected def afterEach(): Unit = shutdownAllSequencers()

  "case where an alignment procedure requires a new reference map should return Completed" in {

    val needNewRefMapKey: Key[Boolean] = KeyType.BooleanKey.make("needNewRefMap")
    val needNewRefMapParam: Parameter[Boolean] = needNewRefMapKey.set(true)
    val needStarAcqKey: Key[Boolean] = KeyType.BooleanKey.make("needStarAcq")
    val needStarAcqParam: Parameter[Boolean] = needStarAcqKey.set(true)
    val command1 = Setup(Prefix("aps.test"), CommandName("start"), None).add(needNewRefMapParam).add(needStarAcqParam)
    val command2 = Setup(Prefix("aps.test"), CommandName("secondCommand"), None)

    val sequence = Sequence(command1, command2)
    apsSequencerClient.submitAndWait(sequence).futureValue shouldBe a[Completed]
  }

  "case where an alignment procedure does not require a new reference map should return Completed" in {

    val needNewRefMapKey: Key[Boolean] = KeyType.BooleanKey.make("needNewRefMap")
    val needNewRefMapParam: Parameter[Boolean] = needNewRefMapKey.set(false)
    val needStarAcqKey: Key[Boolean] = KeyType.BooleanKey.make("needStarAcq")
    val needStarAcqParam: Parameter[Boolean] = needStarAcqKey.set(true)
    val command1 = Setup(Prefix("aps.test"), CommandName("start"), None).add(needNewRefMapParam).add(needStarAcqParam)
    val command2 = Setup(Prefix("aps.test"), CommandName("secondCommand"), None)

    val sequence = Sequence(command1, command2)
    apsSequencerClient.submitAndWait(sequence).futureValue shouldBe a[Completed]
  }


  "case where procedure is standalone reference map should return Completed" in {

    val needNewRefMapKey: Key[Boolean] = KeyType.BooleanKey.make("needNewRefMap")
    val needNewRefMapParam: Parameter[Boolean] = needNewRefMapKey.set(true)
    val needStarAcqKey: Key[Boolean] = KeyType.BooleanKey.make("needStarAcq")
    val needStarAcqParam: Parameter[Boolean] = needStarAcqKey.set(false)
    val command1 = Setup(Prefix("aps.test"), CommandName("start"), None).add(needNewRefMapParam).add(needStarAcqParam)
    val command2 = Setup(Prefix("aps.test"), CommandName("secondCommand"), None)

    val sequence = Sequence(command1, command2)
    apsSequencerClient.submitAndWait(sequence).futureValue shouldBe a[Completed]
  }



}
