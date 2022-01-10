package esw

import esw.SetupCommands.getIrisCommand2
import esw.SetupCommands.getIrisCommand3
import esw.ocs.api.models.Variation
import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.IRIS

script {
    println("[ESW IRIS_ImagerAndIFS]: script loaded")

    val irisRedSequencer = Sequencer(IRIS, obsMode, Variation("red"))
    val irisBlueSequencer = Sequencer(IRIS, obsMode, Variation("blue"))
    val delayDuration = 30000L //30sec

    onSetup("setup-iris-variation") { command1 ->
        println("[ESW IRIS_ImagerAndIFS]: setup-iris-variation command received")
        val command2 = getIrisCommand2(command1.source(), command1.obsId)
        val command3 = getIrisCommand3(command1.source(), command1.obsId)
        irisRedSequencer.submitAndWait(sequenceOf(command2))
        irisBlueSequencer.submitAndWait(sequenceOf(command3))
        println("[ESW IRIS_ImagerAndIFS]: setup-iris-variation command completed")
    }

}
