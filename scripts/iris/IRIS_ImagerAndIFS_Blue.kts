package iris

import esw.ocs.dsl.core.script
import kotlinx.coroutines.delay

script {
    println("[IRIS IRIS_ImagerAndIFS_Blue]: script loaded")

    val delayDuration = 10000L //10sec

    onObserve("iris-command3") {
        println("[IRIS IRIS_ImagerAndIFS_Blue]: iris-command3 command received")
        delay(delayDuration)
        println("[IRIS IRIS_ImagerAndIFS_Blue]: iris-command3 command completed")
    }
}
