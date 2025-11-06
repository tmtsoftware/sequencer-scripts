package aoesw

import esw.ocs.api.models.ObsMode
import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.*
import esw.ocs.dsl.params.choiceKey
import esw.ocs.dsl.params.booleanKey
import csw.params.core.models.Choice
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import shared.Constants
import kotlin.time.Duration.Companion.seconds

script {
    println("********** Loaded AOESW Startup script *********")
    val myPrefix = "AOESW.aosq"
    val defaultTimeout = 5.seconds

    val lgsfSequencer = Sequencer(AOESW, ObsMode("Startup"), defaultTimeout)
    val lgsfPower = Assembly(LGSF, Constants.lgsf.power.componentName)
    val lgsfLaser = Assembly(LGSF, Constants.lgsf.laser.componentName)
    val lgsfLp = Assembly(LGSF, Constants.lgsf.lp.componentName)
    val lgsfOp = Assembly(LGSF, Constants.lgsf.op.componentName)
    val lgsfTe = Assembly(LGSF, Constants.lgsf.te.componentName)
    val lgsfBdm = Assembly(LGSF, Constants.lgsf.bdm.componentName)
    val lgsfFocus = Assembly(LGSF, Constants.lgsf.focus.componentName)

    val nfiraosAt = Assembly(NFIRAOS, Constants.nfiraos.at.componentName)
    val nfiraosBs = Assembly(NFIRAOS, Constants.nfiraos.bs.componentName)
    val nfiraosDm = Assembly(NFIRAOS, Constants.nfiraos.dm.componentName)
    val nfiraosIsm = Assembly(NFIRAOS, Constants.nfiraos.ism.componentName)
    val nfiraosLgsTrombone = Assembly(NFIRAOS, Constants.nfiraos.lgsTrombone.componentName)
    val nfiraosLgsWfs = Assembly(NFIRAOS, Constants.nfiraos.lgsWfs.componentName)
    val nfiraosNscu = Assembly(NFIRAOS, Constants.nfiraos.nscu.componentName)
    val nfiraosNsen = Assembly(NFIRAOS, Constants.nfiraos.nsen.componentName)
    val nfiraosPower = Assembly(NFIRAOS, Constants.nfiraos.power.componentName)
    val nfiraosPwfs = Assembly(NFIRAOS, Constants.nfiraos.pwfs.componentName)
    val nfiraosRtc = Assembly(NFIRAOS, Constants.nfiraos.rtc.componentName)
    val nfiraosSsLgs = Assembly(NFIRAOS, Constants.nfiraos.ssLgs.componentName)
    val nfiraosSsNgs = Assembly(NFIRAOS, Constants.nfiraos.ssNgs.componentName)
    val nfiraosTiming = Assembly(NFIRAOS, Constants.nfiraos.timing.componentName)
    val nfiraosTts = Assembly(NFIRAOS, Constants.nfiraos.tts.componentName)
    val nfiraosVnwAdc = Assembly(NFIRAOS, Constants.nfiraos.vnwAdc.componentName)
    val nfiraosVnwFieldStop = Assembly(NFIRAOS, Constants.nfiraos.vnwFieldStop.componentName)
    val nfiraosVnwFsm = Assembly(NFIRAOS, Constants.nfiraos.vnwFsm.componentName)
    val nfiraosVnwSsm = Assembly(NFIRAOS, Constants.nfiraos.vnwSsm.componentName)


    val irisOiwfsPoa1 = Assembly(IRIS, Constants.iris.oiwfs1.poa.componentName)
    val irisOiwfsPoa2 = Assembly(IRIS, Constants.iris.oiwfs2.poa.componentName)
    val irisOiwfsPoa3 = Assembly(IRIS, Constants.iris.oiwfs3.poa.componentName)
    val irisOiwfsDet1 = Assembly(IRIS, Constants.iris.oiwfs1.detector.componentName)
    val irisOiwfsDet2 = Assembly(IRIS, Constants.iris.oiwfs2.detector.componentName)
    val irisOiwfsDet3 = Assembly(IRIS, Constants.iris.oiwfs3.detector.componentName)
    val irisOiwfsAdc1 = Assembly(IRIS, Constants.iris.oiwfs1.adc.componentName)
    val irisOiwfsAdc2 = Assembly(IRIS, Constants.iris.oiwfs2.adc.componentName)
    val irisOiwfsAdc3 = Assembly(IRIS, Constants.iris.oiwfs3.adc.componentName)

    val rpg = Assembly(AOESW, Constants.aoesw.rpg.componentName)
    val psfr = Assembly(AOESW, Constants.aoesw.psfr.componentName)

    onSetup("Startup") { command ->

        // in parallel, startup LGSF, NFIRAOS, IRIS OIWFS

        // LGSF
        val lgsfStartupResult = async {
            // turn on power
            val lgsfPowerResponse = lgsfPower.submitAndWait(
                Setup(myPrefix, "power", command.obsId)
                    .add(choiceKey("power").set(Choice("ON"))),
            2.seconds)
            // TODO: check lgsfPowerResponse

            // datum mechanisms
            val lgsfDatumResponsesD = listOf(lgsfLp, lgsfOp, lgsfTe, lgsfFocus).map {
                val setup = Setup(myPrefix, "datum", command.obsId)
                async {
                    it.submitAndWait(setup, 60.seconds)
                }
            }

            // TODO: self-test mechanisms

            // close BDM
            val lgsfBdmResponse = lgsfBdm.submitAndWait(
                Setup(myPrefix, "select", command.obsId)
                    .add(choiceKey("BDM").set(Choice("OFF"))),
            10.seconds)
            // TODO: check response

            // set laser state to ready
            val lgsfLaserResponse = lgsfLaser.submitAndWait(
                Setup(myPrefix, "laserState", command.obsId)
                    .add(choiceKey("state").set(Choice("READY"))),
            30.seconds)

            // make sure datums are complete
            val lgsfDatumResponses = lgsfDatumResponsesD.awaitAll()
            // TODO: check responses

            // LUT-based follow mode for op and te
            val lgsfOpFollowResponse = lgsfOp.submitAndWait(
                Setup(myPrefix, "follow", command.obsId)
                    .add(choiceKey("mode").set(Choice("LUT"))),
            2.seconds)
            val lgsfTeFollowResponse = lgsfTe.submitAndWait(
                Setup(myPrefix, "follow", command.obsId)
                    .add(booleanKey("fcsLut").set(true)),
            2.seconds)
            // TODO: check responses

            // TODO: collect responses
            val lgsfAggregateResult = lgsfTeFollowResponse

            // return aggregate result
            lgsfAggregateResult
        }


        val nfiraosStartupResult = async {

            // Datum NFIRAOS mechanisms
            val nfiraosDatumComponents = listOf(
                nfiraosIsm,
                nfiraosNscu,
                nfiraosBs,
                nfiraosLgsTrombone,
                nfiraosNsen,
                nfiraosSsNgs,
                nfiraosSsLgs,
                nfiraosVnwAdc,
                nfiraosVnwFsm,
                nfiraosVnwSsm,
                nfiraosVnwFieldStop
            )

            val nfiraosDatumResponsesD = nfiraosDatumComponents.map {
                val setup = Setup(myPrefix, "datum", command.obsId)
                async {
                    it.submitAndWait(setup, 60.seconds)
                }
            }

            val nfiraosDatumResponses = nfiraosDatumResponsesD.awaitAll()
            // TODO: check responses

            // datum NFIRAOS electronics
            val nfiraosDmDatumResponse = nfiraosDm.submitAndWait(
                Setup(myPrefix, "datum", command.obsId),
            30.seconds)
            // TODO: check response

            // TODO: self-test DM electronics

            // turn on TTS and zero
            val nfiraosTtsDatumResponse = nfiraosTts.submitAndWait(
                Setup(myPrefix, "datum", command.obsId),
            10.seconds)

            // close NSCU shutter
            val nfiraosNscuShutterResponse = nfiraosNscu.submitAndWait(
                Setup(myPrefix, "shutterOpen", command.obsId)
                    .add(booleanKey("open").set(false)),
            2.seconds)

            // turn on LGS VCAM and PWFS VCAM
            val nfiraosPowerResponse = nfiraosPower.submitAndWait(
                Setup(myPrefix, "datum", command.obsId),
            10.seconds)

            // init RTC
            val nfiraosRtcResponse = nfiraosRtc.submitAndWait(
                Setup(myPrefix, "init", command.obsId),
            10.seconds)

            // TODO: collect responses
            val nfiraosAggregateResult = nfiraosRtcResponse

            nfiraosAggregateResult
        }


        val irisStartupResult = async {

            // Datum IRIS mechanisms
            val irisDatumResponsesD = listOf(
                irisOiwfsPoa1,
                irisOiwfsPoa2,
                irisOiwfsPoa3,
                irisOiwfsAdc1,
                irisOiwfsAdc2,
                irisOiwfsAdc3
            ).map {
                val setup = Setup(myPrefix, "datum", command.obsId)
                async {
                    it.submitAndWait(setup, 30.seconds)
                }
            }
            val irisDatumResponses = irisDatumResponsesD.awaitAll()
            // TODO: check responses

            // close NSCU shutter
            val irisNscuShutterResponse = nfiraosNscu.submitAndWait(
                Setup(myPrefix, "shutterOpen", command.obsId)
                    .add(booleanKey("open").set(false)),
            2.seconds)

            // TODO: turn on each OIWFS detector

            // TODO: collect responses
            val irisAggregateResult = irisNscuShutterResponse

            irisAggregateResult
        }

        val aoeswStartupResult = async {
            val aoeswComponents = listOf(rpg, psfr)

            val aoeswInitResponsesD = aoeswComponents.map {
                val setup = Setup(myPrefix, "initialize", command.obsId)
                async {
                    it.submitAndWait(setup, 10.seconds)
                }
            }

            val aoeswInitResponses = aoeswInitResponsesD.awaitAll()

            val aoeswConfigureResponsesD = aoeswComponents.map {
                val setup = Setup(myPrefix, "configure", command.obsId)
                async {
                    it.submitAndWait(setup, 10.seconds)
                }
            }

            val aoeswConfigureResponses = aoeswConfigureResponsesD.awaitAll()

            aoeswConfigureResponses.last()
        }


        // TODO: examine all responses
        val results = awaitAll(lgsfStartupResult, nfiraosStartupResult, irisStartupResult, aoeswStartupResult)

    }

    onDiagnosticMode { startTime, hint ->
        // do some actions to go to diagnostic mode based on hint
    }

    onOperationsMode {
        // do some actions to go to operations mode
    }

    onGoOffline {
        // do some actions to go offline
    }

    onGoOnline {
        // do some actions to go online
    }

    onAbortSequence {
        //do some actions to abort sequence

        //send abortSequence command to downstream sequencer
        lgsfSequencer.abortSequence()
    }

    onStop {
        //do some actions to stop

        //send stop command to downstream sequencer
        lgsfSequencer.stop()
    }

}
