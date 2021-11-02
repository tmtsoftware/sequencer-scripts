package iris

import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.ExposureId
import esw.ocs.dsl.highlevel.models.IRIS
import esw.ocs.dsl.highlevel.models.ObsId
import esw.ocs.dsl.par
import esw.ocs.dsl.params.invoke
import esw.ocs.dsl.params.params

script {
    val imagerAssembly = Assembly(IRIS, "imager.filter")
    val adcAssembly = Assembly(IRIS, "imager.adc")
    val imagerDetector = Assembly(IRIS, "imager.detector")

    imagerDetector.submitAndWait(Setup(this.prefix, "INIT"))

    onSetup("setupObservation") { command ->
        val params = command.params

        par(
                { setupAssembly(imagerAssembly, "SELECT", filterKey, wheel1Key, params) },
                { setupAdcAssembly(adcAssembly, params) }
        )
    }

    onObserve("singleExposure") { command ->
        val directory = command(directoryKey).head()
        val obsId = command.obsId?.let { id -> ObsId(id) }

        val imagerExposureId = command(imagerExposureIdKey).head()
        val imagerIntegrationTime = command(imagerIntegrationTimeKey).head()
        val imagerNumRamps = command(imagerNumRampsKey).head()

        loadConfiguration(imagerDetector, obsId, directory, ExposureId(imagerExposureId), imagerIntegrationTime, imagerNumRamps)
        startExposure(imagerDetector, obsId)
    }

    onShutdown {
        imagerDetector.submitAndWait(Setup(this.prefix, "SHUTDOWN"))
    }
}
