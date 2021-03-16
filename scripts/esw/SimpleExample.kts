package esw

import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.ESW
import esw.ocs.dsl.params.invoke
import esw.ocs.dsl.params.longKey
import esw.ocs.dsl.params.stringKey
import esw.ocs.dsl.params.values
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlin.time.seconds

script {

    onGlobalError { exception ->
        val errorEvent = SystemEvent("ESW.IRIS_DARKNIGHT", exception.reason)
        publishEvent(errorEvent)
    }

    val assemblySleepTimeKey = longKey("timeInMs")
    val myPrefix = "esw.seq"

    onSetup("SimpleSleep") { command  ->
        val componentNames = command(stringKey("AssemblyNames"))
        val sleepTimes = command(longKey("sleepTimes"))

        val tupleList = componentNames.values.zip(sleepTimes.values)
        val submitResponsesD = tupleList.map {
            val setup = Setup(myPrefix, "sleep").add(assemblySleepTimeKey.set(it.second))
            val assembly = Assembly(ESW, it.first, 5.seconds)
            async {
                assembly.submitAndWait(setup)
            }
        }
        val submitResponses = submitResponsesD.awaitAll()
    }.onError {scriptError ->
        val errorEvent = SystemEvent("ESW.IRIS_DARKNIGHT", "Error occurred in setup block $scriptError")
        publishEvent(errorEvent)
    }
}
