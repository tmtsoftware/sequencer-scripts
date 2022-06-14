//@file:Repository("https://jitpack.io/")
//@file:DependsOn("com.github.tmtsoftware.esw:esw-ocs-dsl-kt_2.13:adc26faf3413a9e70a6627c397563e88ea04afb6")
//@file:DependsOn("com.github.tmtsoftware.esw:esw-ocs-app_2.13:adc26faf3413a9e70a6627c397563e88ea04afb6")

@file:Import("../common/CommonUtils.seq.kts")
@file:Import("../common/Keys.seq.kts")
@file:Import("../common/Utils.seq.kts")

import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.ExposureId
import esw.ocs.dsl.highlevel.models.IRIS
import esw.ocs.dsl.par
import esw.ocs.dsl.params.invoke
import esw.ocs.dsl.params.params

script {
    val imagerAssembly = Assembly(IRIS, "imager.filter")
    val adcAssembly = Assembly(IRIS, "imager.adc")
    val imagerDetector = Assembly(IRIS, "imager.detector")

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
        startExposure(imagerDetector, obsId, exposureTimeoutFrom(imagerNumRamps, imagerIntegrationTime))
    }

    onObserve("singleExposure") { command ->
        val directory = command(directoryKey).head()
        val obsId = getObsId(command)

        val imagerExposureId = command(imagerExposureIdKey).head()
        val imagerIntegrationTime = command(imagerIntegrationTimeKey).head()
        val imagerNumRamps = command(imagerNumRampsKey).head()

        loadConfiguration(imagerDetector, obsId, directory, ExposureId(imagerExposureId), imagerIntegrationTime, imagerNumRamps)
        startExposure(imagerDetector, obsId, exposureTimeoutFrom(imagerNumRamps, imagerIntegrationTime))

    }

    onGlobalError { exception ->
        val errorEvent = SystemEvent(this.prefix, "onError-event")
        publishEvent(errorEvent)
        error(exception.reason, exception.cause)
        cleanUp(imagerDetector, adcAssembly)
    }

    onShutdown {
        cleanUp(imagerDetector, adcAssembly)
    }

    onSetup("observationEnd") {
        cleanUp(imagerDetector, adcAssembly)
    }
}
