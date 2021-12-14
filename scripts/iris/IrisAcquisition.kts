package iris

import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.IRIS
import esw.ocs.dsl.par
import esw.ocs.dsl.params.invoke
import iris.IrisConstants.sciResolutionAssembly.command.spectralResolutionKey
import iris.IrisConstants.sciScaleAssembly.command.scaleKey
import kotlinx.coroutines.async
import kotlin.time.seconds
import iris.IrisConstants.ifsDetectorAssembly.command.itimeKey as ifsItimeKey
import iris.IrisConstants.ifsDetectorAssembly.command.rampsKey as ifsRampsKey
import iris.IrisConstants.imagerDetectorAssembly.command.itimeKey as imagerItimeKey
import iris.IrisConstants.imagerDetectorAssembly.command.rampsKey as imagerRampsKey
import iris.IrisConstants.iseq.command.filterKey as seqFilterKey
import iris.IrisConstants.iseq.command.ifsConfigurationsKey as seqIfsConfigurationsKey
import iris.IrisConstants.iseq.command.ifsItimeKey as seqIfsItimeKey
import iris.IrisConstants.iseq.command.ifsRampsKey as seqIfsRampsKey
import iris.IrisConstants.iseq.command.ifsRepeatsKey as seqIfsRepeatsKey
import iris.IrisConstants.iseq.command.imagerItimeKey as seqImagerItimeKey
import iris.IrisConstants.iseq.command.imagerRampsKey as seqImagerRampsKey
import iris.IrisConstants.iseq.command.imagerRepeatsKey as seqImagerRepeatsKey
import iris.IrisConstants.iseq.command.resolutionKey as seqResolutionKey
import iris.IrisConstants.iseq.command.scaleKey as seqScaleKey


script {
    val filterAssembly = Assembly(IRIS, IrisConstants.sciFilterAssembly.componentName, 5.seconds)
    val scaleAssembly = Assembly(IRIS, IrisConstants.sciScaleAssembly.componentName, 5.seconds)
    val spectralResAssembly = Assembly(IRIS, IrisConstants.sciResolutionAssembly.componentName, 5.seconds)
    val imagerDetectorAssembly = Assembly(IRIS, IrisConstants.imagerDetectorAssembly.componentName, 5.seconds)
    val ifsDetectorAssembly = Assembly(IRIS, IrisConstants.ifsDetectorAssembly.componentName, 5.seconds)

    onSetup("Slew IRIS") {command ->
        // set ADC to follow
        // set cold stop to follow

        // waitfor ADC state.target to be follow
        // waitfor coldstop state.target to be follow

    }

    onSetup("configure") { command ->

        // configure IRIS filter (and other upstream optics)
        val filter = command(seqFilterKey)
        val filterResponse = async {
            filterAssembly.submit(Setup(IrisConstants.iseq.prefixStr,
                    "setFilter", command.obsId).add(filter))
        }
        // configure IFS
        val scales = command(seqScaleKey)
        val resolutions = command(seqResolutionKey)
        val scaleAndResolutionResponses = async {
            par(
                    {
                        scaleAssembly.submit(Setup(IrisConstants.iseq.prefixStr, "SELECT", command.obsId)
                                .add(scaleKey.set(scales.head())))
                    },
                    {
                        spectralResAssembly.submit(Setup(IrisConstants.iseq.prefixStr, "GRATING_SELECT",
                                command.obsId)
                                .add(spectralResolutionKey.set(resolutions.head())))
                    }
            )
        }


        // filter needs to finish, but IFS doesn't
        val response = filterResponse.await()

        scaleAndResolutionResponses.await()
    }
}