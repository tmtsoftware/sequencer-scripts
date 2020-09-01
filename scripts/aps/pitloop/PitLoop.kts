@file:Suppress("UNUSED_VARIABLE")

package aps.pitloop

import esw.ocs.dsl.core.FsmScript
import esw.ocs.dsl.params.*



FsmScript("OFF") {

    var pitLoopControlFlag = false
    var pitLoopStoppedFlag = false

    suspend fun runPitLoopIteration() {
        println("running PIT Loop iteration")
        Thread.sleep(1_000)  // simulate time spent doing something
        // make assembly calls directly here or run an additional sequencer
    }

    suspend fun setupPitTracking() {
        // make assembly calls directly here or run an additional sequencer
    }

    suspend fun correctPitTracking() {
        // make assembly calls directly here or run an additional sequencer
    }

    state("ON") {

        onSetup("stop-pit-loop") {
            pitLoopControlFlag = false
            waitFor { pitLoopStoppedFlag } // wait until async loop is completed
            println("becoming OFF")
            become("OFF")
        }

        onSetup("correct-pit-tracking") {
            correctPitTracking()
        }
        
    }

    state("OFF") {

        onSetup("start-pit-loop") {
            pitLoopControlFlag = true

            val pitLoop = loopAsync {
                runPitLoopIteration()
                // possibly send an event here when stop condition is true
                stopWhen(!pitLoopControlFlag)
            }
            pitLoop.invokeOnCompletion { pitLoopStoppedFlag = true }
            println("becoming ON")
            become("ON")
        }

        onSetup("pit-tracking-setup") {
            setupPitTracking()
        }

    }
}

