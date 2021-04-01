package esw

import esw.ocs.api.models.ObsMode
import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.WFOS
import kotlinx.coroutines.delay

script {
    println("[ESW WFOS_Calib]: script loaded")

    val obsMode = ObsMode("WFOS_Calib")
    val wfosSequencer = Sequencer(WFOS, obsMode)
    val delayDuration = 30000L //30sec

    onSetup("setup-wfos") { command1 ->
        println("[ESW WFOS_Calib]: setup-wfos command received")

        val command2 = Setup(command1.source().toString(), "wfos-command2")
        val command3 = Setup(command1.source().toString(), "wfos-command3")
        wfosSequencer.submitAndWait(sequenceOf(command1, command2, command3))

        println("[ESW WFOS_Calib]: setup-wfos command completed")
    }

    onObserve("observe-command1") {
        println("[ESW WFOS_Calib]: observe-command1 command received")
        delay(delayDuration)
        println("[ESW WFOS_Calib]: observe-command1 command completed")

    }

    onObserve("observe-command2") {
        println("[ESW WFOS_Calib]: observe-command2 command received")
        delay(delayDuration)
        println("[ESW WFOS_Calib]: observe-command2 command completed")
    }

    onObserve("observe-command3") {
        println("[ESW WFOS_Calib]: observe-command3 command received")
        delay(delayDuration)
        println("[ESW WFOS_Calib]: observe-command3 command completed")
    }

}
