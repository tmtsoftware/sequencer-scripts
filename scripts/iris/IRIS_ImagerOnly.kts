package iris

import common.*
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

    onSetup("observationStart") {
        imagerDetector.submitAndWait(Setup(this.prefix, "INIT"))
        retractAdcAssembly(adcAssembly, "IN")
    }

    onSetup("setupAcquisition") { command ->
        val params = command.params

        par(
                { setupAssembly(imagerAssembly, "SELECT", filterKey, wheel1Key, params) },
                { setupAdcAssembly(adcAssembly, params) }
        )
    }

    onSetup("setupObservation") { command ->
        val params = command.params

        par(
                { setupAssembly(imagerAssembly, "SELECT", filterKey, wheel1Key, params) },
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

    onSetup("observationEnd") {
        imagerDetector.submitAndWait(Setup(this.prefix, "SHUTDOWN"))
        adcAssembly.submitAndWait(Setup(this.prefix, "PRISM_STOP"))
        retractAdcAssembly(adcAssembly, "OUT")
    }
}
