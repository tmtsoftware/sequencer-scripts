package iris

import esw.ocs.dsl.core.script
import kotlinx.coroutines.delay

script {
    println("[IRIS IRIS_ImagerAndIFS_Red]: script loaded")

    val delayDuration = 10000L //10sec

    onSetup("iris-command2") {
        println("[IRIS IRIS_ImagerAndIFS_Red]: iris-command2 command received")
        delay(delayDuration)
        println("[IRIS IRIS_ImagerAndIFS_Red]: iris-command2 command completed")
    }

}
