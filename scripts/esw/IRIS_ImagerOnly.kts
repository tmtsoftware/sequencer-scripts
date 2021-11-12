package esw

import common.*
import csw.params.commands.Observe
import csw.params.commands.SequenceCommand
import csw.params.core.models.StandaloneExposureId
import csw.time.core.models.UTCTime
import esw.ocs.api.models.ObsMode
import esw.ocs.dsl.core.CommandHandlerScope
import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.RichSequencer
import esw.ocs.dsl.highlevel.models.ExposureNumber
import esw.ocs.dsl.highlevel.models.IRIS
import esw.ocs.dsl.highlevel.models.TYPLevel
import esw.ocs.dsl.isStarted
import esw.ocs.dsl.params.invoke
import kotlin.time.Duration

script {
    val irisSequencer = Sequencer(IRIS, ObsMode("IRIS_ImagerOnly"))
    var observeCounter = 0
    // sequence :  Preset CoarseAcquisition FineAcquisition setupObservation Observe setupObservation Observe setupObservation Observe
    loadScripts(commonHandlers(irisSequencer))

    onObserve("coarseAcquisition") { command ->
        val obsId = getObsId(command)
        publishEvent(guidestarAcqStart(obsId))

        observeCounter++
        val exposureId = observeWithExposureId(command, observeCounter, DET.IMG, imagerExposureTypeKey)
        val observe = Observe(command.source().toString(), "acquisitionExposure", command.obsId).madd(command.paramSet())
        submitAndWaitForStart(irisSequencer, observe.madd(imagerExposureIdKey.set(exposureId)))

        publishEvent(guidestarAcqEnd(obsId))
    }

    onObserve("observe") { command ->
        val obsId = getObsId(command)

        publishEvent(observeStart(obsId))

        observeCounter++
        val exposureId = observeWithExposureId(command, observeCounter, DET.IMG, imagerExposureTypeKey)
        val observe = Observe(command.source().toString(), "singleExposure", command.obsId).madd(command.paramSet())
        submitAndWaitForStart(irisSequencer, observe.madd(imagerExposureIdKey.set(exposureId)))

        publishEvent(observeEnd(obsId))
    }

}
