@file:Suppress("UNUSED_VARIABLE")

package aps.pitloop

import esw.ocs.dsl.core.FsmScript
import esw.ocs.dsl.params.*



FsmScript("OFF") {

    var pitLoopControlFlag = false

        state("ON") { params ->

            onSetup("stop-pit-loop") {
                pitLoopControlFlag = false
                become("OFF")
            }

            onSetup("correct-pit-tracking") { command ->
                    // TODO
                    correctPitTracking()

            }
        }

        state("OFF") {

            onSetup("start-pit-loop") { command ->

                pitLoopControlFlag = true
                loopAsync {
                    runPitLoopIteration()
                    stopWhen(pitLoopControlFlag)
                }

                become("ON", command.params)
            }

            onSetup("pit-tracking-setup") { command ->
                setupPitTracking()
            }

        }

}

suspend fun setupPitTracking() {
    println("setupPitTracking")

    // make assembly calls directly here or run an additional sequencer

}

suspend fun runPitLoopIteration() {

    println("runPitLoopIteration")
    Thread.sleep(1_000)
    // make assembly calls directly here or run an additional sequencer

}

suspend fun correctPitTracking() {
    println("correctPitTracking")

    // make assembly calls directly here or run an additional sequencer

}

