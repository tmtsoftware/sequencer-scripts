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
    val ifsDetector = Assembly(IRIS, "ifs.detector")


    onSetup("setupObservation") { command ->
        val params = command.params

        par(
                { setupAssembly(imagerAssembly, "SELECT", filterKey, wheel1Key, params) },
                { setupAssembly(scaleAssembly, "SELECT", scaleKey, scaleKey, params) },
                { setupAssembly(gratingAssembly, "GRATING_SELECT", spectralResolutionKey, spectralResolutionKey, params) },
                { setupAdcAssembly(adcAssembly, params) }
        )
    }

    onObserve("singleExposure") { command ->
        val directory = command(directoryKey).head()
        val obsId = command.obsId?.let { id -> ObsId(id) }

        val ifsExposureId = command(ifsExposureIdKey).head()
        val ifsIntegrationTime = command(ifsIntegrationTimeKey).head()
        val ifsNumRamps = command(ifsNumRampsKey).head()

        loadConfiguration(ifsDetector, obsId, directory, ExposureId(ifsExposureId), ifsIntegrationTime, ifsNumRamps)
        startExposure(ifsDetector, obsId)
    }
}
