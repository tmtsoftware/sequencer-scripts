package tcs

import esw.ocs.api.models.ObsMode
import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.TCS
import esw.ocs.dsl.par
import kotlin.time.minutes
import kotlin.time.seconds

script {

    val tpkAssembly = Assembly(TCS, "tpk", 5.seconds)

    onSetup("Slew TCS") { command ->

    }

    // 3.1.2 in Workflows
    onSetup("acquire") { command ->
    }
}