package iris

import csw.params.javadsl.JUnits
import esw.ocs.dsl.params.*

val filterChoices = choicesOf("Z", "Y", "J", "H", "K", "Ks", "H+K notch", "CO", "BrGamma", "PaBeta", "H2", "FeII", "HeI", "CaII Trip", "J Cont", "H Cont", "K Cont", "Zn1", "Zn2", "Zn3", "Zn4", "Yn1", "Yn2", "Yn3", "Yn4", "Jn1", "Jn2", "Jn3", "Jn4", "Jn5", "Hn1", "Hn2", "Hn3", "Hn4", "Hn5", "Kn1", "Kn2", "Kn3", "Kn4", "Kn5")

val wheel1Key = choiceKey("wheel1", JUnits.NoUnits, filterChoices)
val filterKey = choiceKey("filter", JUnits.NoUnits, filterChoices)
val scaleKey = choiceKey("scale", JUnits.marcsec, choicesOf("4", "9", "25", "50"))

val retractSelectKey = choiceKey("position", JUnits.NoUnits, choicesOf("IN", "OUT"))

val spectralResChoices = choicesOf("4000-Z", "4000-Y", "4000-J", "4000-H", "4000-K", "4000-H+K", "8000-Z", "8000-Y", "8000-J", "8000-H", "8000-Kn1-3", "8000-Kn4-5", "8000-Kbb", "10000-Z", "10000-Y", "10000-J", "10000-H", "10000-K", "Mirror")
val spectralResolutionKey = choiceKey("spectralResolution", JUnits.NoUnits, spectralResChoices)

val fileNameKey = stringKey("filename")
val exposureIdKey = stringKey("exposureId")
val rampIntegrationTimeKey = intKey("rampIntegrationTime")
val rampsKey = intKey("ramps")
val scienceAdcFollowKey = doubleKey("scienceAdcFollow")
val targetAngleKey = doubleKey("targetAngle")
val directoryKey = stringKey("directory")
val imagerExposureIdKey = stringKey("imagerExposureId")
val imagerIntegrationTimeKey = intKey("imagerIntegrationTime")
val imagerNumRampsKey = intKey("imagerNumRamps")
val ifsExposureIdKey = stringKey("ifsExposureId")
val ifsIntegrationTimeKey = intKey("ifsIntegrationTime")
val ifsNumRampsKey = intKey("ifsNumRamps")

