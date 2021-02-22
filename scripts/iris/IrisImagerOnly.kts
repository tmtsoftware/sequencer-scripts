package iris

import csw.params.core.models.Choice
import csw.params.events.SystemEvent
import csw.params.javadsl.JUnits
import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.IRIS
import esw.ocs.dsl.par
import esw.ocs.dsl.params.invoke
import iris.IrisConstants.imagerFilterPositions
import iris.IrisConstants.iseq.command.imagerObserveKey
import kotlin.time.seconds

script {

    val filterAssembly = Assembly(IRIS, IrisConstants.sciFilterAssembly.componentName, 5.seconds)
    val scaleAssembly = Assembly(IRIS, IrisConstants.sciScaleAssembly.componentName, 5.seconds)
    val spectralResAssembly = Assembly(IRIS, IrisConstants.sciResolutionAssembly.componentName, 5.seconds)
    val imagerDetectorAssembly = Assembly(IRIS, IrisConstants.imagerDetectorAssembly.componentName, 5.seconds)
    val ifsDetectorAssembly = Assembly(IRIS, IrisConstants.ifsDetectorAssembly.componentName, 5.seconds)

    onSetup("setupObservation") { command ->

        // extract info from incoming command
        val filter = command(IrisConstants.iseq.command.filterKey).head().toString()
        val itime = command(IrisConstants.iseq.command.imagerItimeKey).head()
        val ramps = command(IrisConstants.iseq.command.imagerRampsKey).head()

        // get filter target positions for each wheel based on filter in command
        val filterPositions = imagerFilterPositions(filter)

        // construct target parameters
        val wheelTargetParameters =
                IrisConstants.sciFilterAssembly.command.wheelKeys.zip(filterPositions).map {
                    it.first.set(it.second)
                }

        // construct filter command
        val filterCommand = Setup(prefix, "select", command.obsId).madd(*wheelTargetParameters.toTypedArray())

        // construct command to set imager detector settings
        val setupImagerCommand = Setup(prefix, "LOAD_CONFIGURATION", command.obsId)
                .add(IrisConstants.imagerDetectorAssembly.command.itimeKey.set(itime).withUnits(JUnits.millisecond))
                .add(IrisConstants.imagerDetectorAssembly.command.rampsKey.set(ramps))

        // send both commands at same time
        par(
                { filterAssembly.submit(filterCommand) },
                { imagerDetectorAssembly.submit(setupImagerCommand) }
        )
    }

    onSetup("setObserverKeywords") { command ->
        // args to command match event.  simply reuse and pass on as event.
        publishEvent(SystemEvent(prefix, IrisConstants.iseq.event.observerKeywordsEvent,
                *command.jParamSet().toTypedArray()))
    }

    // start a subscription to track exposure status (shown as an exampe, not currently used)
    // note this is done in constuctor of Script, so it is always running (not part of a command).
    var currentExposureInProgressEventValue = false
    onEvent(IrisConstants.imagerDetectorAssembly.event.exposureState) { event ->
        when (event) {
            is SystemEvent ->
                currentExposureInProgressEventValue = event(IrisConstants.imagerDetectorAssembly.eventParameter.exposureInProgressKey).head()
        }
    }

    // Start a loop in the contructor that is always running
    // Take imager exposures while takeImagerExposures flag is set
    // This flag is set on observe commands in the observe handler below
    var takeImagerExposures = false
    var maybeObsId: String? = null
    fun getObsId() = maybeObsId
    var stopExposureLoop = false
    loopAsync {
        if (takeImagerExposures) {
            val observeCommand = Observe(prefix, "START_EXPOSURE", getObsId())
            val response = imagerDetectorAssembly.submitAndWait(observeCommand)
            // check response
        }
        stopWhen(stopExposureLoop) // loop forever.  can be set to true on shutdown.
    }

    // Create a watch on temperatures and stop exposure if temps are unsafe
    // map (EventName -> state) for storing flag for cold states.  Set default state values to true
    val allThermalStatesMap = HashMap<String, Boolean>()
    allThermalStatesMap.putAll(IrisConstants.cryoenvAssembly.cryoenvStateEventNames.map { it to true })
    // set flag as Boolean And of all values in map.  Note this is defined as a method.
    fun okForExposures() = allThermalStatesMap.values.all { it }
    // create event keys from list of EventNames
    val cryoenvStateEvents =
            IrisConstants.cryoenvAssembly.cryoenvStateEventNames.map { "IRIS.${IrisConstants.cryoenvAssembly.componentName}.$it" }.toTypedArray()
    // subscription to watch
    onEvent(*cryoenvStateEvents) { event ->
        when (event) {
            is SystemEvent -> {
                val thisThermalState = event(IrisConstants.cryoenvAssembly.event.cryoenvVacuumStateKey).head()
                allThermalStatesMap.put(event.eventName().name(), (thisThermalState == Choice("COLD")))

                if (!okForExposures()) {
                    // if any state is not cold, stop taking exposures and abort
                    if (takeImagerExposures) {
                        takeImagerExposures = false
                        // This command is not part of original sequence, we need not to add/update it to CRM
                        val observeCommand = Observe(prefix, "ABORT_EXPOSURE", maybeObsId)
                        imagerDetectorAssembly.submitAndWait(observeCommand)
                    }
                }
            }
        }
    }

    onObserve("observe") { command ->
        when (command(imagerObserveKey).head().toString()) {
            "START" -> {
                if (okForExposures()) {
                    takeImagerExposures = true
                    maybeObsId = command.obsId
                } else {
                    finishWithError("Not all IRIS systems are COLD")
                }
            }
            "STOP" -> takeImagerExposures = false
            "ABORT" -> {
                takeImagerExposures = false
                val observeCommand = Observe(prefix, "ABORT_EXPOSURE", command.obsId)
                imagerDetectorAssembly.submitAndWait(observeCommand)
            }
            else -> finishWithError("imagerObserve must be START, STOP, or ABORT")
        }
    }

    // This is an experimental version of observe using a loop started within
    onObserve("observe2") { command ->
        when (command(imagerObserveKey).head().toString()) {
            "START" -> {
                if (stopExposureLoop) {
                    stopExposureLoop = false
                    loopAsync {
                        val observeCommand = Observe(prefix, "START_EXPOSURE", command.obsId)
                        imagerDetectorAssembly.submitAndWait(observeCommand)
                        stopWhen(stopExposureLoop) // loop until flag set
                    }
                }
            }
            "STOP" -> stopExposureLoop = true
            "ABORT" -> {
                stopExposureLoop = true
                val observeCommand = Observe(prefix, "ABORT_EXPOSURE", command.obsId)
                imagerDetectorAssembly.submitAndWait(observeCommand)
            }
        }
    }

    // this doesn't exist in IS command interface, but left here as possible alternative
    onObserve("singleObserve") { command ->
        val commandName = when (command(imagerObserveKey).head().toString()) {
            "START" -> "START_EXPOSURE"
            "STOP" -> "ABORT_EXPOSURE"
            "ABORT" -> "ABORT_EXPOSURE"
            else -> finishWithError("imagerObserve must be START, STOP, or ABORT")
        }

        val observeCommand = Observe(prefix, commandName, command.obsId)
        imagerDetectorAssembly.submitAndWait(observeCommand)
    }
}