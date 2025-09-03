package aoesw

import esw.ocs.api.models.ObsMode
import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.*
import esw.ocs.dsl.par
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import shared.Constants
import shared.Components
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

script {
    println("********** Loaded AOESW Startup script *********")
    val myPrefix = "AOESW.AOSQ_Startup"
    val defaultTimeout = 5.seconds

    val lgsfSequencer = Sequencer(AOESW, ObsMode("Startup"), defaultTimeout)

    onSetup("Startup") { command ->

        // in parallel, startup LGSF, NFIRAOS, IRIS OIWFS


        // Datum NFIRAOS mechanisms
        val nfiraosDatumComponents = listOf(
            Constants.nfiraos.at.componentName,
            Constants.nfiraos.bs.componentName,
            Constants.nfiraos.ism.componentName,
            Constants.nfiraos.nscu.componentName,
            Constants.nfiraos.nsen.componentName,
            Constants.nfiraos.ssLgs.componentName,
            Constants.nfiraos.ssNgs.componentName,
            Constants.nfiraos.tts.componentName,
            Constants.nfiraos.vnwAdc.componentName,
            Constants.nfiraos.vnwFieldStop.componentName,
            Constants.nfiraos.vnwFsm.componentName,
            Constants.nfiraos.vnwSsm.componentName
        )

        val nfiraosDatumResponsesD = nfiraosDatumComponents.map {
            val setup = Setup(myPrefix, "datum", command.obsId)
            val assembly = Assembly(NFIRAOS, it, defaultTimeout)
            async {
                assembly.submitAndWait(setup)
            }
        }

        val nfiraosDatumResponses = nfiraosDatumResponsesD.awaitAll()

        // self test mechanisms

        //
        Assembly()


    }

    onDiagnosticMode { startTime, hint ->
        // do some actions to go to diagnostic mode based on hint
    }

    onOperationsMode {
        // do some actions to go to operations mode
    }

    onGoOffline {
        // do some actions to go offline
    }

    onGoOnline {
        // do some actions to go online
    }

    onAbortSequence {
        //do some actions to abort sequence

        //send abortSequence command to downstream sequencer
        lgsfSequencer.abortSequence()
    }

    onStop {
        //do some actions to stop

        //send stop command to downstream sequencer
        lgsfSequencer.stop()
    }

}
