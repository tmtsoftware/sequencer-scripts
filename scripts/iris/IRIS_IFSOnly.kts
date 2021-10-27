package iris

import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.IRIS
import kotlinx.coroutines.delay

script {
    println("[IRIS IRIS_Darknight]: script loaded")
    val detectorAssmebly = Assembly(IRIS, "ifs.detector")
    val delayDuration = 10000L //10sec

    onSetup("setup-iris") {
        println("[IRIS IRIS_Darknight]: setup-iris command received")
        val submit = detectorAssmebly.submit(Setup("IRIS.iris.sequencer", "INIT"))
        println(submit)
        println("[IRIS IRIS_Darknight]: setup-iris command completed")
    }

    onSetup("iris-command2") {
        println("[IRIS IRIS_Darknight]: iris-command2 command received")
        delay(delayDuration)
        println("[IRIS IRIS_Darknight]: iris-command2 command completed")
    }

    onObserve("iris-command3") {
        println("[IRIS IRIS_Darknight]: iris-command3 command received")
        delay(delayDuration)
        println("[IRIS IRIS_Darknight]: iris-command3 command completed")
    }
}
