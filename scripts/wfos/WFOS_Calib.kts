package wfos


import esw.ocs.dsl.core.script
import kotlinx.coroutines.delay

script {
    println("[WFOS WFOS_Calib]: script loaded")

    val delayDuration = 10000L //10sec

    onSetup("setup-wfos") { command1 ->
        println("[WFOS WFOS_Calib]: setup-wfos command received")
        delay(delayDuration)
        println("[WFOS WFOS_Calib]: setup-wfos command completed")
    }

    onSetup("wfos-command2") {
        println("[WFOS IRIS_Darknight]: wfos-command2 command received")
        delay(delayDuration)
        println("[WFOS IRIS_Darknight]: wfos-command2 command completed")
    }

    onSetup("wfos-command3") {
        println("[WFOS IRIS_Darknight]: wfos-command3 command received")
        delay(delayDuration)
        println("[WFOS IRIS_Darknight]: wfos-command3 command completed")
    }
}
