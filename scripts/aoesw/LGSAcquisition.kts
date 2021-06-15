package aoesw

import csw.params.commands.CommandResponse
import csw.params.core.models.Choice
import csw.params.events.SystemEvent
import esw.ocs.api.models.ObsMode
import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.Prefix
import esw.ocs.dsl.highlevel.models.TCS
import esw.ocs.dsl.params.*
import kotlin.math.sqrt
import kotlin.time.Duration

script {

    val oiwfsDetectorAssembly = Assembly(Prefix("iris.oiwfs.detector.oiwfs-detector-assembly"), Duration.seconds(5))
    val rtcPrefix = "nfiraos.rtc.nfiraos-rtc-assembly"
    val tcsSequencer = Sequencer(TCS, ObsMode("darknight"), Duration.seconds(5))

    val oiwfsExposureModeChoices = choicesOf("SINGLE", "CONTINUOUS", "STOP", "NOOP")
    val oiwfsExposureModeKey = choiceKey("mode", oiwfsExposureModeChoices)

    val oiwfsStateEvent = EventKey(rtcPrefix, "oiwfsState")
    val oiwfsStateEnableChoices = choicesOf("NONE", "TT", "TTF")
    val oiwfsStateEnableKey = choiceKey("enable", oiwfsStateEnableChoices)
    val oiwfsStateFluxHighKey = booleanKey("fluxHigh")
    val oiwfsStateFluxLowKey = booleanKey("fluxlow")

    val ttfOffsetEvent = EventKey(rtcPrefix, "telOffloadTt") // ??
    val ttfOffsetXKey = floatKey("x")
    val ttfOffsetYKey = floatKey("y")

    val tcsOffsetCoordinateSystemChoices = choicesOf("RADEC", "XY", "ALTAZ")
    val tcsOffsetCoordSystemKey = choiceKey("coordinateSystem", tcsOffsetCoordinateSystemChoices)
    val tcsOffsetXKey = floatKey("x")
    val tcsOffsetYKey = floatKey("y")
    val tcsOffsetVirtualTelescopeChoices =
            choicesOf("MOUNT", "OIWFS1", "OIWFS2", "OIWFS3", "OIWFS4", "ODGW1", "ODGW2", "ODGW3", "ODGW4", "GUIDER1", "GUIDER2")
    val tcsOffsetVTKey = choiceKey("virtualTelescope", tcsOffsetVirtualTelescopeChoices)

    val loopeventKey = EventKey(rtcPrefix, "loop")
    val oiwfsLoopStatesChoices = choicesOf("IDLE", "LOST", "ACTIVE")
    val oiwfsLoopKey = choiceKey("oiwfsPoa", oiwfsLoopStatesChoices)

    fun handleOiwfsLoopOpen(oiwfsProbeNum: Int) {
        // Do something
    }

    onEvent(loopeventKey.key()) { event ->
        when (event) {
            is SystemEvent -> {
                val oiwfsLoopStates = event(oiwfsLoopKey)
                val ii = oiwfsLoopStates.values.indexOf(Choice("LOST"))
                if (ii != -1) handleOiwfsLoopOpen(ii)
            }
        }
    }

    val TCSOFFSETTHRESHOLD = 2.0 // arcsec ???
    fun isOffsetRequired(x: Float, y: Float): Boolean = sqrt(x * x + y * y) > TCSOFFSETTHRESHOLD

    fun increaseExposureTime() {
        // not sure how this is done
    }

    suspend fun offsetTcs(xoffset: Float, yoffset: Float, probeNum: Int, obsId: String?) =
            tcsSequencer.submitAndWait(
                    sequenceOf(
                            Setup(prefix, "offset", obsId)
                                    .add(tcsOffsetCoordSystemKey.set(Choice("RADEC")))
                                    .add(tcsOffsetXKey.set(xoffset))
                                    .add(tcsOffsetYKey.set(yoffset))
                                    .add(tcsOffsetVTKey.set(Choice("OIWFS$probeNum")))
                    )
            )

    onSetup("enableOiwfsTtf") { command ->
        val ttfProbeNum = when (val event = getEvent(oiwfsStateEvent.key())) {
            is SystemEvent -> event(oiwfsStateEnableKey).values.indexOf(Choice("TTF"))
            else -> -1
        }

        var ttfFluxHigh = false
        var ttfFluxLow = false
        var xoffset = 0.0f
        var yoffset = 0.0f

        val subscription = onEvent(oiwfsStateEvent.key(), ttfOffsetEvent.key()) { event ->
            when {
                event is SystemEvent && event.eventName() == oiwfsStateEvent.eventName() && ttfProbeNum != -1 -> {
                    ttfFluxHigh = event(oiwfsStateFluxHighKey).value(ttfProbeNum)
                    ttfFluxLow = event(oiwfsStateFluxLowKey).value(ttfProbeNum)
                }
                event is SystemEvent && event.eventName() == ttfOffsetEvent.eventName() -> {
                    xoffset = event(ttfOffsetXKey).head()
                    yoffset = event(ttfOffsetYKey).head()
                }
            }
        }

        // start continuous exposures on TTF probe
        val probeExpModes = (0..2).map { if (it == ttfProbeNum) Choice("CONTINUOUS") else Choice("NOOP") }.toTypedArray()
        val startExposureCommand = Setup(prefix, "exposure", command.obsId)
                .add(oiwfsExposureModeKey.setAll(probeExpModes))

        when (val response = oiwfsDetectorAssembly.submitAndWait(startExposureCommand)) {
            is CommandResponse.Completed -> {
                val guideStarLockedThreshold = 5 // number of consecutive loops without an offset to consider stable
                var timesGuideStarLocked = 0
                val maxAttempts = 20 // maximum number of loops on this guide star before rejecting
                var attempts = 0

                loop(Duration.milliseconds(500)) {
                    when {
                        ttfFluxLow -> increaseExposureTime() // period tbd
                        isOffsetRequired(xoffset, yoffset) -> {
                            offsetTcs(xoffset, yoffset, ttfProbeNum, command.obsId)
                            timesGuideStarLocked = 0
                        }
                        else -> timesGuideStarLocked += 1
                    }

                    attempts += 1
                    stopWhen((timesGuideStarLocked == guideStarLockedThreshold) || (attempts == maxAttempts))
                }

                if (timesGuideStarLocked != guideStarLockedThreshold)
                        finishWithError("Guide Star Unstable")
            }
            else -> finishWithError("Error starting WFS exposures: $response")
        }
        subscription.cancel()
    }
}
