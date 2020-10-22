@file:Suppress("UNUSED_VARIABLE")

package aps.rigidbodym3alignment

import csw.params.events.SystemEvent
import esw.ocs.dsl.core.script
import esw.ocs.dsl.epics.CommandFlag
import esw.ocs.dsl.params.*

script {




    // PitStabilization Fsm states
    val NOT_STABLE = "NOT_STABLE"
    val STABLE = "STABLE"


    // Event-based variable for current temperature
    val stabilityKey = longKey("stability")
    val stabilityVar = ParamVariable(0, "aps.pit.state.stability", stabilityKey)


    // FSM definition
    val pitStabilityFsm = Fsm("TEMP", NOT_STABLE) {


        state(NOT_STABLE) {
            val currentStability = stabilityVar.first()

            println(currentStability)

            on(currentStability < 20L) {
                become(STABLE)
            }
        }

        state(STABLE) {
            completeFsm()
        }
    }

    // bind reactives to FSM
    stabilityVar.bind(pitStabilityFsm)



    onSetup("wait-for-pit-stable") {
        pitStabilityFsm.start()
        pitStabilityFsm.await()
        info("FSM is no longer running.")
    }




    onDiagnosticMode { startTime, hint ->
        // do some actions to go to diagnostic mode based on hint
        //testAssembly.diagnosticMode(startTime, hint)
    }

    onOperationsMode {
        // do some actions to go to operations mode
        //testAssembly.operationsMode()
    }

    onGoOffline {
        // do some actions to go offline
        //testAssembly.goOffline()
    }

    onGoOnline {
        // do some actions to go online
        //testAssembly.goOnline()
    }

    onAbortSequence {
        //do some actions to abort sequence

        //send abortSequence command to downstream sequencer
        //lgsfSequencer.abortSequence()
    }

    onStop {
        //do some actions to stop

        //send stop command to downstream sequencer
        //lgsfSequencer.stop()
    }
}

