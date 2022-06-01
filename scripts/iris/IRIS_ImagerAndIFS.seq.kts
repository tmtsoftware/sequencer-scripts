@file:Import("../common/CommonUtils.seq.kts")

package iris
import common.*

import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.ExposureId
import esw.ocs.dsl.highlevel.models.IRIS
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


    onSetup("observationStart") {
        sendSetupCommandToAssembly(ifsDetector, "INIT")
        sendSetupCommandToAssembly(imagerDetector, "INIT")
        retractAdcAssembly(adcAssembly, "IN")
    }

    onSetup("setupAcquisition") { command ->
        val params = command.params

        par(
                { setupAssembly(imagerAssembly, "SELECT", irisFilterKey, irisWheel1Key, params) },
                { setupAdcAssembly(adcAssembly, params) }
        )
    }

    onSetup("setupObservation") { command ->
        val params = command.params

        par(
                { setupAssembly(imagerAssembly, "SELECT", irisFilterKey, irisWheel1Key, params) },
                { setupAssembly(scaleAssembly, "SELECT", scaleKey, scaleKey, params) },
                { setupAssembly(gratingAssembly, "GRATING_SELECT", spectralResolutionKey, spectralResolutionKey, params) },
                { setupAdcAssembly(adcAssembly, params) }
        )
    }

    onObserve("acquisitionExposure") { command ->
        val directory = command(directoryKey).head()
        val obsId = getObsId(command)

        val imagerExposureId = command(imagerExposureIdKey).head()
        val imagerIntegrationTime = command(imagerIntegrationTimeKey).head()
        val imagerNumRamps = command(imagerNumRampsKey).head()
        loadConfiguration(imagerDetector, obsId, directory, ExposureId(imagerExposureId), imagerIntegrationTime, imagerNumRamps)
        startExposure(imagerDetector, obsId, exposureTimeoutFrom(imagerNumRamps, imagerIntegrationTime))
    }

    onObserve("singleExposure") { command ->
        val directory = command(directoryKey).head()
        val obsId = getObsId(command)

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
                { startExposure(imagerDetector, obsId, exposureTimeoutFrom(imagerNumRamps, imagerIntegrationTime)) },
                { startExposure(ifsDetector, obsId, exposureTimeoutFrom(ifsNumRamps, ifsIntegrationTime)) }
        )
    }

    onGlobalError { exception ->
        val errorEvent = SystemEvent(this.prefix, "onError-event")
        publishEvent(errorEvent)
        error(exception.reason, exception.cause)
        cleanUp(imagerDetector, adcAssembly, ifsDetector)
    }

    onShutdown {
        cleanUp(imagerDetector, adcAssembly, ifsDetector)
    }

    onSetup("observationEnd") {
        cleanUp(imagerDetector, adcAssembly, ifsDetector)
    }
}
