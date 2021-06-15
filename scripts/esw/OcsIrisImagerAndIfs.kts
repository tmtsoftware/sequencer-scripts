package esw

import csw.params.core.models.Choice
import csw.params.javadsl.JUnits
import esw.ocs.api.models.ObsMode
import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.IRIS
import iris.IrisConstants.iseq.command.filterKey
import iris.IrisConstants.iseq.command.ifsConfigurationsKey
import iris.IrisConstants.iseq.command.ifsItimeKey
import iris.IrisConstants.iseq.command.ifsRampsKey
import iris.IrisConstants.iseq.command.ifsRepeatsKey
import iris.IrisConstants.iseq.command.imagerItimeKey
import iris.IrisConstants.iseq.command.imagerRampsKey
import iris.IrisConstants.iseq.command.imagerRepeatsKey
import iris.IrisConstants.iseq.command.resolutionKey
import iris.IrisConstants.iseq.command.scaleKey
import kotlinx.coroutines.async
import kotlin.time.Duration

script {

    // The following settings would be passed in command parameters
    // but the extraction of these values is omitted here for brevity.
    val filter            = Choice("K")
    val resolutions       = arrayOf(Choice("4000-K"), Choice("4000-K"))
    val scales            = arrayOf(Choice("4"), Choice("9"))
    val imagerItime       = 60
    val imagerNumRamps    = 1
    val imagerRepeats     = 2
    val ifsItimes         = arrayOf(2, 2)
    val ifsNumRamps       = arrayOf(1, 1)
    val ifsRepeats        = arrayOf(16, 16)
    val ifsConfigurations = 2

    onSetup("irisImagerAndIfs") { command ->

        // send entire setup to IRIS seq
        val irisCommand = Setup(prefix, "parallelObservation", command.obsId)
                .add(filterKey.set(filter))
                .add(scaleKey.setAll(scales))
                .add(resolutionKey.setAll(resolutions))
                .add(imagerItimeKey.set(imagerItime).withUnits(JUnits.millisecond))
                .add(imagerRampsKey.set(imagerNumRamps))
                .add(imagerRepeatsKey.set(imagerRepeats))
                .add(ifsItimeKey.setAll(ifsItimes).withUnits(JUnits.millisecond))
                .add(ifsRampsKey.setAll(ifsNumRamps))
                .add(ifsRepeatsKey.setAll(ifsRepeats))
                .add(ifsConfigurationsKey.set(ifsConfigurations))

        val iris = Sequencer(IRIS, ObsMode("IrisImagerAndIfs"))
        iris.submitAndWait(sequenceOf(irisCommand), Duration.minutes(5))
    }

    onSetup("irisImagerAndIfsSeparate") { command ->
        // this would represent logic for a coordinated IRIS Imager and IFS observation
        // if there were two IRIS sequencers, one for the imager, and one for the IFS

        val irisImagerSeq = Sequencer(IRIS, ObsMode("IrisImagerOnly"))
        val irisIfsSeq = Sequencer(IRIS, ObsMode("IrisIfsOnly"))

        // send filter command
        val imagerConfigResponse = async {
            irisImagerSeq.submitAndWait(
                    sequenceOf(
                            Setup(prefix, "configureImager", command.obsId)
                                    .add(filterKey.set(filter))
                    )
            )
        }

        // send IFS setup
        val ifsConfigResponse = async {
            irisIfsSeq.submit(
                    sequenceOf(
                            Setup(prefix, "configureIfs", command.obsId)
                                    .add(scaleKey.set(scales[0]))
                                    .add(resolutionKey.set(resolutions[0]))
                    )
            )
        }

        // wait for filter to finish moving
        imagerConfigResponse.await()

        // send Imager sequence
        val imagerObserveCommand = Observe(prefix, "observe", command.obsId)
                .add(imagerItimeKey.set(imagerItime))
                .add(imagerRampsKey.set(imagerNumRamps))

        val imagerObserveSequence = sequenceOf(*Array(imagerRepeats){imagerObserveCommand})

        // should this command be submitAndWait?  in async block?
        irisImagerSeq.submit(imagerObserveSequence)


        // create IFS sequence
        val firstIfsObserveCommand = Observe(prefix, "observe", command.obsId)
                .add(ifsItimeKey.set(ifsItimes[0]))
                .add(ifsRampsKey.set(ifsNumRamps[0]))
        var ifsObserveSequence = sequenceOf(*Array(ifsRepeats[0]){firstIfsObserveCommand})

        repeat(ifsConfigurations - 1) { counter ->
            // start with second configuration
            val configNum = counter + 1

            val nextSetup = Setup(prefix, "configureIfs", command.obsId)
                    .add(scaleKey.set(scales[configNum]))
                    .add(resolutionKey.set(resolutions[configNum]))
            ifsObserveSequence = ifsObserveSequence.add(sequenceOf(nextSetup))

            val nextObserveCommand = Observe(prefix, "observe", command.obsId)
                    .add(ifsItimeKey.set(ifsItimes[configNum]))
                    .add(ifsRampsKey.set(ifsNumRamps[configNum]))

            ifsObserveSequence = ifsObserveSequence.add(sequenceOf(*Array(ifsRepeats[configNum]){nextObserveCommand}))
        }

        // wait for IFS to finish setting up
        ifsConfigResponse.await()

        // send sequence
        val ifsObserveResponse = irisIfsSeq.submitAndWait(ifsObserveSequence)

    }
}