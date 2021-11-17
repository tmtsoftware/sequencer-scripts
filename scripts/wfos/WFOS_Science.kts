package wfos

import common.*
import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.ExposureId
import esw.ocs.dsl.highlevel.models.WFOS
import esw.ocs.dsl.par
import esw.ocs.dsl.params.invoke
import esw.ocs.dsl.params.params

script {

    val blueFilterAssembly = Assembly(WFOS, "blue.filter")
    val redFilterAssembly = Assembly(WFOS, "red.filter")
    val blueDetector = Assembly(WFOS, "blue.detector")
    val redDetector = Assembly(WFOS, "red.detector")

    blueDetector.submitAndWait(Setup(this.prefix, "INIT"))
    redDetector.submitAndWait(Setup(this.prefix, "INIT"))

    onSetup("setupAcquisition") { command ->
        setupAssembly(blueFilterAssembly, "SELECT", wfosBlueFilterKey, wfosWheel1Key, command.params)
    }

    onSetup("setupObservation") { command ->
        par(
                { setupAssembly(blueFilterAssembly, "SELECT", wfosBlueFilterKey, wfosWheel1Key, command.params) },
                { setupAssembly(redFilterAssembly, "SELECT", wfosRedFilterKey, wfosWheel1Key, command.params) }
        )
    }

    onObserve("acquisitionExposure") { command ->
        val directory = command(directoryKey).head()
        val obsId = getObsId(command)

        val blueExposureId = command(blueExposureIdKey).head()
        val blueIntegrationTime = command(blueIntegrationTimeKey).head()
        val blueNumRamps = command(blueNumRampsKey).head()

        loadConfiguration(blueDetector, obsId, directory, ExposureId(blueExposureId), blueIntegrationTime, blueNumRamps)
        startExposure(blueDetector, obsId)
    }

    onObserve("singleExposure") { command ->
        val directory = command(directoryKey).head()
        val obsId = getObsId(command)

        val blueExposureId = command(blueExposureIdKey).head()
        val blueIntegrationTime = command(blueIntegrationTimeKey).head()
        val blueNumRamps = command(blueNumRampsKey).head()

        val redExposureId = command(redExposureIdKey).head()
        val redIntegrationTime = command(redIntegrationTimeKey).head()
        val redNumRamps = command(redNumRampsKey).head()

        par(
                { loadConfiguration(blueDetector, obsId, directory, ExposureId(blueExposureId), blueIntegrationTime, blueNumRamps) },
                { loadConfiguration(redDetector, obsId, directory, ExposureId(redExposureId), redIntegrationTime, redNumRamps) }
        )

        par(
                { startExposure(blueDetector, obsId) },
                { startExposure(redDetector, obsId) }
        )
    }

    onShutdown {
        blueDetector.submitAndWait(Setup(this.prefix, "SHUTDOWN"))
        redDetector.submitAndWait(Setup(this.prefix, "SHUTDOWN"))
    }
}
