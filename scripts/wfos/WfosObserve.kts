package wfos

import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.WFOS
import esw.ocs.dsl.params.intKey
import esw.ocs.dsl.params.invoke

script {
    var stopObserving = false
    val detectorAssembly = Assembly(WFOS, "detectorAssembly")

    onObserve("repeatedObserve") { command ->
        val repeats = command(intKey("repeats")).head()
        var counter = 0
        loopAsync {
            detectorAssembly.submitAndWait(Setup(prefix, "takeExposure", command.obsId))
            counter += 1
            stopWhen((counter == repeats) || stopObserving)
        }
    }

    onAbortSequence {
        stopObserving = true
        detectorAssembly.submitAndWait(Setup(prefix, "abortExposure"))
    }
}