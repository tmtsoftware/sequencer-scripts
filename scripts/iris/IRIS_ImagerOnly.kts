package iris

import common.*
import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.ExposureId
import esw.ocs.dsl.highlevel.models.IRIS
import esw.ocs.dsl.par
import esw.ocs.dsl.params.invoke
import esw.ocs.dsl.params.params
import kotlin.time.Duration.Companion.minutes

script {
    val imagerAssembly = Assembly(IRIS, "imager.filter")
    val adcAssembly = Assembly(IRIS, "imager.adc")
    val imagerDetector = Assembly(IRIS, "imager.detector",5.minutes)

    onSetup("observationStart") {
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
        startExposure(imagerDetector, obsId)
    }

    onObserve("singleExposure") { command ->
        val directory = command(directoryKey).head()
        val obsId = getObsId(command)

        val imagerExposureId = command(imagerExposureIdKey).head()
        val imagerIntegrationTime = command(imagerIntegrationTimeKey).head()
        val imagerNumRamps = command(imagerNumRampsKey).head()

        loadConfiguration(imagerDetector, obsId, directory, ExposureId(imagerExposureId), imagerIntegrationTime, imagerNumRamps)
        startExposure(imagerDetector, obsId)

    }

    onGlobalError { exception ->
        val errorEvent = SystemEvent(this.prefix, "onError-event")
        publishEvent(errorEvent)
        error(exception.reason, exception.cause)
        cleanUp(imagerDetector, adcAssembly)
    }

    onSetup("observationEnd") {
        cleanUp(imagerDetector, adcAssembly)
    }
}