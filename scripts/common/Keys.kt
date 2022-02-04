package common

import csw.params.core.models.Choice
import csw.params.javadsl.JUnits
import esw.ocs.dsl.params.*

//************************iris filter wheel keys************************\\
val irisFilterChoices = choicesOf("Z", "Y", "J", "H", "K", "Ks", "H+K notch", "CO", "BrGamma", "PaBeta", "H2", "FeII", "HeI", "CaII Trip", "J Cont", "H Cont", "K Cont")
val irisWheel1Key = choiceKey("wheel1", JUnits.NoUnits, irisFilterChoices)
val irisFilterKey = choiceKey("filter", JUnits.NoUnits, irisFilterChoices)
//******************************************************************\\

//************************iris scale keys************************\\
val scaleKey = choiceKey("scale", JUnits.marcsec, choicesOf("4", "9", "25", "50"))
//***********************************************************\\

//************************iris adc keys************************\\
val retractSelectKey = choiceKey("position", JUnits.NoUnits, choicesOf("IN", "OUT"))
//***********************************************************\\

//************************iris resolution keys************************\\
val spectralResChoices = choicesOf("4000-Z", "4000-Y", "4000-J", "4000-H", "4000-K", "4000-H+K", "8000-Z", "8000-Y", "8000-J", "8000-H", "8000-Kn1-3", "8000-Kn4-5", "8000-Kbb", "10000-Z", "10000-Y", "10000-J", "10000-H", "10000-K", "Mirror")
val spectralResolutionKey = choiceKey("spectralResolution", JUnits.NoUnits, spectralResChoices)
//****************************************************************\\

//******************iris adc keys******************************\\
val scienceAdcFollowKey = booleanKey("scienceAdcFollow")
val followingKey = choiceKey("following", JUnits.NoUnits, choicesOf("FOLLOWING", "STOPPED"))
//*************************************************************\\

//**********************common detector keys***************************\\
val fileNameKey = stringKey("filename")
val exposureIdKey = stringKey("exposureId")
val rampIntegrationTimeKey = intKey("rampIntegrationTime")
val rampsKey = intKey("ramps")
val directoryKey = stringKey("directory")
//*************************************************************\\

//*******************iris detector keys***********************\\
val imagerExposureIdKey = stringKey("imagerExposureId")
val imagerIntegrationTimeKey = intKey("imagerIntegrationTime")
val imagerNumRampsKey = intKey("imagerNumRamps")
val ifsExposureIdKey = stringKey("ifsExposureId")
val ifsIntegrationTimeKey = intKey("ifsIntegrationTime")
val ifsNumRampsKey = intKey("ifsNumRamps")
val imagerExposureTypeKey = stringKey("imagerExposureType")
val ifsExposureTypeKey = stringKey("ifsExposureType")
//*************************************************************\\

enum class IRISDET {
    IMG,
    IFS
}

enum class WFOSDET {
    BLU,
    RED
}

//************************wfos filter wheel keys************************\\


val blueFilterChoices = choicesOf("u'", "g'", "fused-silica")
val wfosBlueWheel1Key = choiceKey("wheel1", JUnits.NoUnits, blueFilterChoices)
val wfosBlueFilterKey = choiceKey("blueFilter", JUnits.NoUnits, blueFilterChoices)

val redFilterChoice = choicesOf("r'", "i'", "z'", "fused-silica")
val wfosRedWheel1Key = choiceKey("wheel1", JUnits.NoUnits, redFilterChoice)
val wfosRedFilterKey = choiceKey("redFilter", JUnits.NoUnits, redFilterChoice)
//*************************************************************\\

//*******************wfos detector keys***********************\\
val blueExposureIdKey = stringKey("blueExposureId")
val blueIntegrationTimeKey = intKey("blueIntegrationTime")
val blueNumRampsKey = intKey("blueNumRamps")
val redExposureIdKey = stringKey("redExposureId")
val redIntegrationTimeKey = intKey("redIntegrationTime")
val redNumRampsKey = intKey("redNumRamps")
val blueExposureTypeKey = stringKey("blueExposureType")
val redExposureTypeKey = stringKey("redExposureType")
//*************************************************************\\

//*******************tcs keys***********************\\
//sequencer keys
val targetCoordKey = coordKey("targetCoords")
val baseCoordKey = coordKey("baseCoords")
val icrsChoice = Choice("ICRS")
val refFrameKey = choiceKey("Refframe", icrsChoice)
val pKey = doubleKey("p")
val qKey = doubleKey("q")
val xCoordinateKey = doubleKey("Xcoordinate")
val yCoordinateKey = doubleKey("Ycoordinate")

//Assembly commands keys
val baseKey = coordKey("base")
val currentEqCoordKey = eqCoordKey("currentPos")
val demandEqCoordKey = eqCoordKey("demandPos")
val baseCurrentKey = doubleKey("baseCurrent")
val capCurrentKey = doubleKey("capCurrent")
val baseDemandKey = doubleKey("baseDemand")
val capDemandKey = doubleKey("capDemand")
//*************************************************************\\

