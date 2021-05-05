package tcs

import esw.ocs.dsl.core.script
import kotlinx.coroutines.delay

script {
    println("[TCS IRIS_Darknight]: script loaded")

    val delayDuration = 10000L //10sec

    onSetup("setup-tcs") {
        println("[TCS IRIS_Darknight]: setup-tcs command received")
        delay(delayDuration)
        println("[TCS IRIS_Darknight]: setup-tcs command completed")
    }

    onSetup("tcs-command2") {
        println("[TCS IRIS_Darknight]: tcs-command2 command received")
        delay(delayDuration)
        println("[TCS IRIS_Darknight]: tcs-command2 command completed")
    }

    onObserve("tcs-command3") {
        println("[TCS IRIS_Darknight]: tcs-command3 command received")
        delay(delayDuration)
        println("[TCS IRIS_Darknight]: tcs-command3 command completed")
    }

}
