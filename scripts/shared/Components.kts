package shared

import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.*
import kotlin.time.Duration.Companion.seconds

object nfiraos {
    val defaultTimeout = 5.seconds
    val atAssembly = Assembly(NFIRAOS, Constants.nfiraos.at.componentName, defaultTimeout)
    val bsAssembly = Assembly(NFIRAOS, Constants.nfiraos.bs.componentName, defaultTimeout)
    val dmAssembly = Assembly(NFIRAOS, Constants.nfiraos.dm.componentName, defaultTimeout)
    val ismAssembly = Assembly(NFIRAOS, Constants.nfiraos.ism.componentName, defaultTimeout)
    val nscuAssembly = Assembly(NFIRAOS, Constants.nfiraos.nscu.componentName, defaultTimeout)
    val nsenAssembly = Assembly(NFIRAOS, Constants.nfiraos.nsen.componentName, defaultTimeout)
    val ssLgsAssembly = Assembly(NFIRAOS, Constants.nfiraos.ssLgs.componentName, defaultTimeout)
    val ssNgsAssembly = Assembly(NFIRAOS, Constants.nfiraos.ssNgs.componentName, defaultTimeout)
    val ttsAssembly = Assembly(NFIRAOS, Constants.nfiraos.tts.componentName, defaultTimeout)
    val vnwAdcAssembly = Assembly(NFIRAOS, Constants.nfiraos.vnwAdc.componentName, defaultTimeout)
    val vnwFieldStopAssembly = Assembly(NFIRAOS, Constants.nfiraos.vnwFieldStop.componentName, defaultTimeout)
    val vnwFsmAssembly = Assembly(NFIRAOS, Constants.nfiraos.vnwFsm.componentName, defaultTimeout)
    val vnwSsmAssembly = Assembly(NFIRAOS, Constants.nfiraos.vnwSsm.componentName, defaultTimeout)
}
