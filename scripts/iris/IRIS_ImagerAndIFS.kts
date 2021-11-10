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
    val gratingAssembly = Assembly(IRIS, "ifs.res")
    val scaleAssembly = Assembly(IRIS, "ifs.scale")
    val adcAssembly = Assembly(IRIS, "imager.adc")
    val imagerDetector = Assembly(IRIS, "imager.detector")
    val ifsDetector = Assembly(IRIS, "ifs.detector")

    ifsDetector.submitAndWait(Setup(this.prefix, "INIT"))
    imagerDetector.submitAndWait(Setup(this.prefix, "INIT"))

    onSetup("setupAcquisition") { command ->
        val params = command.params

        retractAdcAssemblyIN(adcAssembly)
        par(
                { setupAssembly(imagerAssembly, "SELECT", filterKey, wheel1Key, params) },
                { setupAdcAssembly(adcAssembly, params) }
        )
    }

    onSetup("setupObservation") { command ->
        val params = command.params

        par(
                { setupAssembly(imagerAssembly, "SELECT", filterKey, wheel1Key, params) },
                { setupAssembly(scaleAssembly, "SELECT", scaleKey, scaleKey, params) },
                { setupAssembly(gratingAssembly, "GRATING_SELECT", spectralResolutionKey, spectralResolutionKey, params) },
                { setupAdcAssembly(adcAssembly, params) }
        )
    }

    onObserve("acquisitionExposure") { command ->
        val directory = command(directoryKey).head()
        val obsId = command.obsId?.let { id -> ObsId(id) }

        val imagerExposureId = command(imagerExposureIdKey).head()
        val imagerIntegrationTime = command(imagerIntegrationTimeKey).head()
        val imagerNumRamps = command(imagerNumRampsKey).head()

        loadConfiguration(imagerDetector, obsId, directory, ExposureId(imagerExposureId), imagerIntegrationTime, imagerNumRamps)
        startExposure(imagerDetector, obsId)
    }

    onObserve("singleExposure") { command ->
        val directory = command(directoryKey).head()
        val obsId = command.obsId?.let { id -> ObsId(id) }

        val imagerExposureId = command(imagerExposureIdKey).head()
        val imagerIntegrationTime = command(imagerIntegrationTimeKey).head()
        val imagerNumRamps = command(imagerNumRampsKey).head()

        val ifsExposureId = command(ifsExposureIdKey).head()
        val ifsIntegrationTime = command(ifsIntegrationTimeKey).head()
        val ifsNumRamps = command(ifsNumRampsKey).head()

        par(
                { loadConfiguration(imagerDetector, obsId, directory, ExposureId(imagerExposureId), imagerIntegrationTime, imagerNumRamps) },
                { loadConfiguration(ifsDetector, obsId, directory, ExposureId(ifsExposureId), ifsIntegrationTime, ifsNumRamps) }
        )

        par(
                { startExposure(imagerDetector, obsId) },
                { startExposure(ifsDetector, obsId) }
        )
    }

    onShutdown {
        ifsDetector.submitAndWait(Setup(this.prefix, "SHUTDOWN"))
        imagerDetector.submitAndWait(Setup(this.prefix, "SHUTDOWN"))
    }
}
