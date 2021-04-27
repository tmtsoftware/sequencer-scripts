package esw

import esw.ocs.api.models.ObsMode
import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.IRIS
import esw.ocs.dsl.highlevel.models.TCS
import kotlinx.coroutines.delay

script {
    println("[ESW IRIS_Darknight]: script loaded")

    val obsMode = ObsMode("IRIS_Darknight")
    val irisSequencer = Sequencer(IRIS, obsMode)
    val tcsSequencer = Sequencer(TCS, obsMode)
    val delayDuration = 30000L //30sec

    onSetup("setup-iris") { command1 ->
        println("[ESW IRIS_Darknight]: setup-iris command received")

        val command2 = Setup(command1.source().toString(), "iris-command2")
        val command3 = Setup(command1.source().toString(), "iris-command3")
        irisSequencer.submitAndWait(sequenceOf(command1, command2, command3))
        println("[ESW IRIS_Darknight]: setup-iris command completed")
    }

    onSetup("setup-tcs") { command1 ->
        println("[ESW IRIS_Darknight]: setup-tcs command received")
        val command2 = Setup(command1.source().toString(), "tcs-command2")
        val command3 = Setup(command1.source().toString(), "tcs-command3")
        tcsSequencer.submitAndWait(sequenceOf(command1, command2, command3))
        println("[ESW IRIS_Darknight]: setup-tcs command completed")

    }

    onObserve("observe-command1") {
        println("[ESW IRIS_Darknight]: observe-command1 command received")
        delay(delayDuration)
        println("[ESW IRIS_Darknight]: observe-command1 command completed")

    }

    onObserve("observe-command2") {
        println("[ESW IRIS_Darknight]: observe-command2 command received")
        delay(delayDuration)
        println("[ESW IRIS_Darknight]: observe-command2 command completed")
    }

    onObserve("observe-command3") {
        println("[ESW IRIS_Darknight]: observe-command3 command received")
        delay(delayDuration)
        println("[ESW IRIS_Darknight]: observe-command3 command completed")
    }

    onSetup("fail-setup") {
        throw RuntimeException("handle-setup-failed")
    }

    onSetup("setup-both") { command1 ->
        println("[ESW IRIS_Darknight]: setup-both command received")

        val command2 = Setup(command1.source().toString(), "iris-command2")
        val command3 = Setup(command1.source().toString(), "iris-command3")
        val command2t = Setup(command1.source().toString(), "tcs-command2")
        val command3t = Setup(command1.source().toString(), "tcs-command3")

        val irisRes = irisSequencer.submit(sequenceOf(command2, command3))
        val tcsRes = tcsSequencer.submit(sequenceOf(command2t, command3t))
        println("[ESW IRIS_Darknight]: setup-both command completed")

    }

}
