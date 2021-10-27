package iris

import csw.params.events.SystemEvent
import csw.params.javadsl.JUnits
import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.ExposureId
import esw.ocs.dsl.highlevel.models.IRIS
import esw.ocs.dsl.highlevel.models.ObsId
import esw.ocs.dsl.par
import esw.ocs.dsl.params.*

script {
    val imagerAssembly = Assembly(IRIS, "imager.filter")
    val gratingAssembly = Assembly(IRIS, "ifs.res")
    val scaleAssembly = Assembly(IRIS, "ifs.scale")
    val adcAssembly = Assembly(IRIS, "imager.adc")
    val imagerDetector = Assembly(IRIS, "imager.detector")
    val ifsDetector = Assembly(IRIS, "ifs.detector")



    onSetup("setupObservation") { command ->
        val params = command.params

        choiceKey("scale", JUnits.marcsec)

        par(
                { setupAssembly(imagerAssembly, "SELECT", choiceKey("wheel1"), params) },
                { setupAssembly(scaleAssembly, "SELECT", choiceKey("scale", JUnits.marcsec), params) },
                { setupAssembly(gratingAssembly, "GRATING_SELECT", choiceKey("spectralResolution"), params) },
                { setupAdcAssembly(adcAssembly, params) }
        )

        waitFor {
            var onTarget = false
            onEvent(EventKey("IRIS.imager.adc", "prism_state").key()) { event ->
                when (event) {
                    is SystemEvent -> {
                        event(booleanKey("onTarget")).head()?.let { x -> onTarget = x }
                    }
                }
            }
            onTarget
        }
    }

    onObserve("singleExposure") { command ->
        val directory = command(stringKey("directory")).head()
        val obsId = command.obsId?.let { id -> ObsId(id) }

        val imagerExposureId = command(stringKey("imagerExposureId")).head()
        val imagerIntegrationTime = command(intKey("imagerIntegrationTime")).head()
        val imagerNumRamps = command(intKey("imagerNumRamps")).head()

        val ifsExposureId = command(stringKey("ifsExposureId")).head()
        val ifsIntegrationTime = command(intKey("ifsIntegrationTime")).head()
        val ifsNumRamps = command(intKey("ifsNumRamps")).head()

        par(
                { loadConfiguration(imagerDetector, obsId, directory, ExposureId(imagerExposureId), imagerIntegrationTime, imagerNumRamps) },
                { loadConfiguration(ifsDetector, obsId, directory, ExposureId(ifsExposureId), ifsIntegrationTime, ifsNumRamps) }
        )

        imagerDetector.submit(Observe(imagerDetector.prefix.toString(), "START_EXPOSURE", command.obsId))
        ifsDetector.submit(Observe(ifsDetector.prefix.toString(), "START_EXPOSURE", command.obsId))
    }
}
