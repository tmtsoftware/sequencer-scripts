package aoesw

import esw.ocs.api.models.ObsMode
import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.*
import esw.ocs.dsl.params.choiceKey
import esw.ocs.dsl.params.booleanKey
import csw.params.core.models.Choice
import esw.ocs.dsl.par
import esw.ocs.dsl.params.doubleKey
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import shared.Constants
import kotlin.time.Duration.Companion.seconds

script {
    println("********** Loaded AOESW Daytime Calibration script *********")
    val myPrefix = "AOESW.aosq"
    val defaultTimeout = 5.seconds

    val lgsfSequencer = Sequencer(AOESW, ObsMode("DaytimeCalibration"), defaultTimeout)
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

    fun isWithinTolerance(): Boolean {
        // TBD
        return true
    }

    suspend fun deployBroadbandLight(obsId: String?) {
        val responses = par (
            {nfiraosNscu.submitAndWait(
                Setup(myPrefix, "deployMirror", obsId)
                    .add(booleanKey("deploy").set(true)),
                10.seconds)},
            {nfiraosNscu.submitAndWait(
                Setup(myPrefix, "lampPower", obsId)
                    .add(choiceKey("lamp").set(Choice("flat")))
                    .add(booleanKey("on").set(true)),
                10.seconds)},
            {nfiraosNscu.submitAndWait(
                Setup(myPrefix, "shutterOpen", obsId)
                    .add(booleanKey("open").set(true)),
                10.seconds)}
        )

        // TODO aggregate responses
        responses.last()
    }


    onSetup("DeterminePwfsRefAndSsmPointing") { command ->
        val turnOnLight = deployBroadbandLight(command.obsId)

        // Configure PWFS detector params
        nfiraosPwfs.submitAndWait(
            Setup(myPrefix, "configureTBD", command.obsId),
        5.seconds)

        // set no binning of pwfs
        nfiraosPwfs.submitAndWait(
            Setup(myPrefix, "setBinning", command.obsId)
                .add(choiceKey("binning").set(Choice("1"))),
        2.seconds)

        // Set VNW FSM to follow at maximum modulation
        nfiraosVnwFsm.submitAndWait(
            Setup(myPrefix, "follow", command.obsId)
                .add(doubleKey("pwfsModPeriod").set(1250.0)),
        5.seconds)

        // Configure RTC in NGGAO mode (TBD)
        nfiraosRtc.submitAndWait(
            Setup(myPrefix, "mode", command.obsId),
        5.seconds)

        // Configure RTC with 0 gain for TTS and DM control
        nfiraosRtc.submitAndWait(
            Setup(myPrefix, "configureGainTBD", command.obsId),
        2.seconds)

        // Configure SSM in Cal mode
        nfiraosVnwSsm.submitAndWait(
            Setup(myPrefix, "zeroPwfs", command.obsId), 5.seconds
        )

        // Deploy NFIRAOS pinhole mask
        nfiraosSsNgs.submitAndWait(
            Setup(myPrefix, "deploy", command.obsId)
                .add(choiceKey("select").set(Choice("FPM"))), 5.seconds
        )

        // Move SSM to center pinhole
        nfiraosVnwSsm.submitAndWait(
            Setup(myPrefix, "positionPwfs", command.obsId)
                .add(doubleKey("imageX").set(0.0))
                .add(doubleKey("imageY").set(0.0)),
            10.seconds
        )

        // Enable PWFS continuous exposures (frameRate TBD)
        nfiraosPwfs.submitAndWait(
            Setup(myPrefix, "startContinuousExposures", command.obsId)
                .add(doubleKey("frameRate").set(10.0)), 2.seconds
        )

        // Command RTC to close TTF control loop
        nfiraosRtc.submitAndWait(
            Setup(myPrefix, "offloadPwfsSsm", command.obsId)
                .add(booleanKey("enable").set(true)), 2.seconds
        )


        // List of offsets in percentage of pupil to be used to move SSM.
        // Offsets are arbitrary for this prototype.
        // Moves are relative moves, so offsets are
        // specified from current position.
        data class offset(val x: Double, val y: Double)
        val offsetList = listOf(
            offset(0.3, 0.3),
            offset(-0.6, -0.6),
            offset(0.0, 0.6),
            offset(0.6, -0.6),
            offset(-0.3, 0.3))

        offsetList.map { offset ->
            // TBD  Calibrate the SSM differential offset model, record pupil image.
            // RPG uses this to compute the pupil centering matched filter.

            // offset pupil
            nfiraosVnwSsm.submitAndWait(
                Setup(myPrefix, "nudgePwfs", command.obsId)
                    .add(doubleKey("pupilX").set(offset.x))
                    .add(doubleKey("pupilY").set(offset.y)),
                10.seconds
            )
        }

        // List of locations in image pixels to position PWFS to locate on pinholes in FPM.
        // Locations are arbitrary for this prototype.
        // use offset data class even though position is specified
        // in absolute coordinates, since it contains the same necessary fields (x,y)
        val locationList = listOf(
            offset(100.0, 100.0),
            offset(-100.0, -100.0),
            offset(-100.0, 100.0),
            offset(100.0, -100.0),
            offset(0.0, 0.0)
        )
        locationList.map { location ->

            // wait until with tolerance
            loop {
                delay(1000)
                stopWhen(isWithinTolerance())
            }

            // TBD record offsets

            // open loops
            nfiraosRtc.submitAndWait(
                Setup(myPrefix, "loopOpen", command.obsId), 2.seconds
            )

            //  compute pupil centering error using the pupil centering matched filter
            nfiraosRtc.submitAndWait(
                Setup(myPrefix, "algoSetPwfs", command.obsId)
                    .add(choiceKey("algo").set(Choice("COG_UPDATE"))),
                2.seconds
            )

            // Move SSM to  pinhole
            nfiraosVnwSsm.submitAndWait(
                Setup(myPrefix, "positionPwfs", command.obsId)
                    .add(doubleKey("imageX").set(location.x))
                    .add(doubleKey("imageY").set(location.y)),
                10.seconds
            )

        }

        // TBD determine pointing model

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
