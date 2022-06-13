@file:Repository("https://jitpack.io/")
@file:DependsOn("com.github.tmtsoftware.esw:esw-ocs-dsl-kt_2.13:adc26faf3413a9e70a6627c397563e88ea04afb6")
@file:DependsOn("com.github.tmtsoftware.esw:esw-ocs-app_2.13:adc26faf3413a9e70a6627c397563e88ea04afb6")

@file:Import("../common/CommonUtils.seq.kts")
@file:Import("../common/Keys.seq.kts")
@file:Import("../common/Utils.seq.kts")

//package wfos
//import common.*

import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.ExposureId
import esw.ocs.dsl.highlevel.models.WFOS
import esw.ocs.dsl.par
import esw.ocs.dsl.params.invoke
import esw.ocs.dsl.params.params
import kotlin.time.Duration.Companion.minutes

script {

    val blueFilterAssembly = Assembly(WFOS, "blue.filter")
    val redFilterAssembly = Assembly(WFOS, "red.filter")
    val blueDetector = Assembly(WFOS, "blue.detector", 5.minutes)
    val redDetector = Assembly(WFOS, "red.detector", 5.minutes)

    sendCommandAndLog(blueDetector, Setup(this.prefix, "INIT"))
    sendCommandAndLog(redDetector, Setup(this.prefix, "INIT"))

    onSetup("setupObservation") { command ->
        par(
                { setupAssembly(blueFilterAssembly, "SELECT", wfosBlueFilterKey, wfosBlueWheel1Key, command.params) },
                { setupAssembly(redFilterAssembly, "SELECT", wfosRedFilterKey, wfosRedWheel1Key, command.params) }
        )
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
                { startExposure(blueDetector, obsId, exposureTimeoutFrom(blueNumRamps, blueIntegrationTime)) },
                { startExposure(redDetector, obsId, exposureTimeoutFrom(redNumRamps, redIntegrationTime)) }
        )
    }

    onShutdown {
        sendSetupCommandToAssembly(blueDetector, "SHUTDOWN")
        sendSetupCommandToAssembly(redDetector, "SHUTDOWN")
    }
}
