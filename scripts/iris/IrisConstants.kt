package iris

import csw.params.core.generics.Key
import csw.params.core.models.Choice
import csw.params.core.models.Choices
import csw.prefix.models.Prefix
import esw.ocs.dsl.highlevel.models.IRIS
import esw.ocs.dsl.params.*

object IrisConstants {
    val samplingModeChoices: Choices = choicesOf("CDS", "MCD", "UTR")

    fun imagerFilterPositions(filter: String): List<String> = when (filter)  {
        "H" -> listOf("H", "OPEN", "OPEN", "OPEN", "OPEN")
        "J" -> listOf("J", "OPEN", "OPEN", "OPEN", "OPEN")
        "K" -> listOf("K", "OPEN", "OPEN", "OPEN", "OPEN")
        "CO" -> listOf("OPEN", "OPEN", "OPEN", "OPEN", "CO")
        "Hn1" -> listOf("OPEN", "Hn1", "OPEN", "OPEN", "OPEN")
        else -> listOf() // invalid (how to handle?)
    }

    val filterChoices: Choices = choicesOf("Z", "Y", "J", "H", "K", "Ks", "H+K notch", "CO",
            "BrGamma", "PaBeta", "H2", "FeII", "HeI", "CaII Trip", "J Cont", "H Cont", "K Cont",
            "Zn1", "Zn2", "Zn3", "Zn4", "Yn1", "Yn2", "Yn3", "Yn4", "Jn1", "Jn2", "Jn3", "Jn4", "Jn5",
            "Hn1", "Hn2", "Hn3", "Hn4", "Hn5", "Kn1", "Kn2", "Kn3", "Kn4", "Kn5")

    val scaleChoices: Choices = choicesOf("4", "9", "25", "50")

    val spectralResolutionChoices: Choices = choicesOf("4000-Z", "4000-Y", "4000-J", "4000-H", "4000-K",
            "4000-H+K", "8000-Z", "8000-Y", "8000-J", "8000-H", "8000-Kn1-3", "8000-Kn4-5", "8000-Kbb", "10000-Z",
            "10000-Y", "10000-J", "10000-H", "10000-K", "Mirror")

    object iseq {
        val componentName = "ici.is"
        val prefixStr = Prefix(IRIS, componentName).toString()

        val imagerObserveChoices: Choices = choicesOf("START", "STOP", "ABORT")

        object command {
            val filterKey: Key<Choice> = choiceKey("filter", filterChoices)
            val scaleKey: Key<Choice>           = choiceKey("scale", scaleChoices)
            val resolutionKey: Key<Choice>      = choiceKey("spectralResolution", spectralResolutionChoices)
            val imagerItimeKey: Key<Int> = intKey("imagerIntegrationTime")
            val imagerRampsKey: Key<Int> = intKey("imagerNumRamps")
            val imagerRepeatsKey: Key<Int>     = intKey("imagerNumRepeats")
            val ifsItimeKey: Key<Int>          = intKey("ifsIntegrationTime")
            val ifsRampsKey: Key<Int>          = intKey("ifsNumRamps")
            val ifsRepeatsKey: Key<Int>        = intKey("ifsNumRepeats")
            val ifsConfigurationsKey: Key<Int> = intKey("ifsConfigurations")

            val imagerObserveKey: Key<Choice> = choiceKey("imagerObserve", imagerObserveChoices)
        }
        object event {
            val observerKeywordsEvent = "observerKeywords"
        }
    }

    object adcAssembly {
        val componentName = "imager.adc"

        object command {
            val retractSelectKey: Key<Choice> = choiceKey("position", choicesOf("IN", "OUT"))
        }
        object event {
            val state = "IRIS.imager.adc.state"
        }
        object eventParameter {
            val stateOnTargetKey: Key<Boolean> = booleanKey("onTarget")
        }
    }

    object coldstopAssembly {
        val componentName = "imager.coldstop"

        object command {
        }
        object event {
            val state = "IRIS.imager.coldstop.state"
        }
        object eventParameter {
            val stateOnTargetKey: Key<Boolean> = booleanKey("onTarget")
        }
    }

    object sciFilterAssembly {
        val componentName = "imager.filter"

        val filterWheelNames = listOf("wheel1", "wheel2", "wheel3", "wheel4", "wheel5")

        object command {
            val wheelKeys: List<Key<String>> = filterWheelNames.map{stringKey(it)} // really an enum key
        }
        object event { }
    }

    object sciScaleAssembly {
        val componentName = "ifs.scale"

        object command {
            val scaleKey: Key<Choice> = choiceKey("scale", scaleChoices)
        }
        object event { }
    }

    object sciResolutionAssembly {
        val componentName = "ifs.res"

        object command {
            val spectralResolutionKey: Key<Choice> = choiceKey("spectralresolution", spectralResolutionChoices)
        }
        object event { }
    }

    object imagerDetectorAssembly {
        val componentName = "imager.detector"

        object command {
            val itimeKey: Key<Int> = intKey("rampIntegrationTime")
            val rampsKey: Key<Int> = intKey("ramps")
            val samplingModeKey: Key<Choice> = choiceKey("imagerSamplingMode", samplingModeChoices)
        }
        object event {
            val exposureState = "IRIS.imager.detector.exposureState"
        }
        object eventParameter {
            val exposureInProgressKey: Key<Boolean> = booleanKey("exposureInProgress")

        }
    }

    object ifsDetectorAssembly {
        val componentName = "ifs.detector"

        object command {
            val itimeKey: Key<Int> = intKey("rampIntegrationTime")
            val rampsKey: Key<Int> = intKey("ramps")
            val samplingModeKey: Key<Choice> = choiceKey("imagerSamplingMode", samplingModeChoices)
        }
        object event {
            val exposureInProgressKey: Key<Boolean> = booleanKey("exposureInProgress")

        }
    }

    object cryoenvAssembly {
        val componentName = "sc.cryoenv"

        val cryoenvStateEventNames: Set<String> = setOf("IMG_STATE", "IFS_STATE", "WIN_STATE", "PV_STATE", "PRESS_STATE")
        val cryoenvVacuumStates: Choices = choicesOf("WARM", "PUMPING", "WARM_VACUUM", "COOLING", "COLD", "WARMING", "PRESSURIZING")

        object command { }
        object event {
            val cryoenvVacuumStateKey: Key<Choice> = choiceKey("thermalVacuumState", cryoenvVacuumStates)
        }
    }

}