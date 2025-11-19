package aoesw

import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.*
import csw.params.core.models.Choice
import esw.ocs.dsl.epics.CommandFlag
import esw.ocs.dsl.par
import esw.ocs.dsl.params.*
import esw.ocs.dsl.params.Params
import shared.Constants
import kotlin.time.Duration.Companion.seconds

script {
    println("********** Loaded LGSF Operations script *********")
    val myPrefix = "AOESW.aosq.lgsf"
    val defaultTimeout = 5.seconds

    val lgsfPower = Assembly(LGSF, Constants.lgsf.power.componentName)
    val lgsfLaser = Assembly(LGSF, Constants.lgsf.laser.componentName)
    val lgsfLp = Assembly(LGSF, Constants.lgsf.lp.componentName)
    val lgsfOp = Assembly(LGSF, Constants.lgsf.op.componentName)
    val lgsfTe = Assembly(LGSF, Constants.lgsf.te.componentName)
    val lgsfBdm = Assembly(LGSF, Constants.lgsf.bdm.componentName)
    val lgsfFocus = Assembly(LGSF, Constants.lgsf.focus.componentName)


    var currentState = ""
    val stateKey = stringKey("state")
    suspend fun publishState(state: String) =
        publishEvent(SystemEvent(myPrefix, "lgsfState").add(stateKey.set(state)))


    val UNKNOWN = "UNKNOWN"
    val OFF = "OFF"
    val STANDBY = "STANDBY"
    val READY = "READY"
    val ONLOW = "ON-LOW"
    val ON = "ON"
    val OBSERVATION = "OBSERVATION"

    val ASTERISM_MCAO = Choice("MCAO")
    val ASTERISM_MCAO_ALT = Choice("MCAO_ALT")
    val ASTERISM_GLAO = Choice("GLAO")
    val ASTERISM_MOAO = Choice("MOAO")
    val ASTERISM_MIRAO = Choice("MIRAO")
    val ASTERISM_OTHER = Choice("OTHER")


    // TODO how are these set
    // TODO look for better alternatives than globals, perhaps Command Flags
    var obsId  = ""
    var asterism = ASTERISM_OTHER

    val stateParamsFlag = CommandFlag()

    val lgsfFsm = Fsm("LGSF_FSM", UNKNOWN) {
        state(UNKNOWN) {
            entry {
                info("Entering LGSF UNKNOWN state")
                publishState(UNKNOWN)
            }
        }

        state(OFF) {
            entry {
                info("Entering LGSF OFF state")
                currentState = OFF
                publishState(OFF)

                // Close LGSF Shutters in parallel
                val coverResponses = par (
                    {
                        lgsfTe.submitAndWait(
                            Setup(myPrefix, "lltCover", obsId)
                                .add(choiceKey("select").set(Choice("CLOSED"))),
                            10.seconds
                        )
                    },
                    {
                        lgsfBdm.submitAndWait(
                            Setup(myPrefix, "select", obsId)
                                .add(choiceKey("BDM").set(Choice("ON"))),
                            10.seconds
                        )
                    }
                )

                // Set laser components to OFF
                lgsfLaser.submitAndWait(
                    Setup(myPrefix, "laserState", obsId)
                        .add(choiceKey("state").set(Choice("OFF"))),
                    defaultTimeout
                )

                // Set to FULL power mode
                lgsfLp.submitAndWait(
                    Setup(myPrefix, "select", obsId)
                        .add(choiceKey("enabled").set(Choice("OUT"))),
                )

                // Open BTO Loops
                val openLoopResponses = par (
                    {
                        lgsfOp.submitAndWait(
                            Setup(myPrefix, "follow", obsId)
                                .add(choiceKey("mode").set(Choice("OFF"))),
                            10.seconds
                        )
                    },
                    {
                        lgsfTe.submitAndWait(
                            Setup(myPrefix, "follow", obsId)
                                .add(booleanKey("fcsLut").set(false))
                                .add(booleanKey("fcsLutUpdate").set(false))
                                .add(booleanKey("tcskm").set(false))
                                .add(booleanKey("rtcFsm").set(false))
                                .add(booleanKey("rtcAcq").set(false)),
                            10.seconds
                        )
                    }
                )

                // Park BTO Mechanisms
                val parkResponses = par (
                    {
                        lgsfOp.submitAndWait(
                            Setup(myPrefix, "park", obsId),  // TBD
                            10.seconds
                        )
                    },
                    {
                        lgsfTe.submitAndWait(
                            Setup(myPrefix, "park", obsId),  // TBD
                            10.seconds
                        )
                    }
                )
            }
        }
        state(STANDBY) {
            entry {
                info("Entering LGSF STANDBY state")
                currentState = STANDBY
                publishState(STANDBY)


                // close LGSF Shutters in parallel
                val coverResponses = par (
                    {
                        lgsfTe.submitAndWait(
                            Setup(myPrefix, "lltCover", obsId)
                                .add(choiceKey("select").set(Choice("CLOSED"))),
                            10.seconds
                        )
                    },
                    {
                        lgsfBdm.submitAndWait(
                            Setup(myPrefix, "select", obsId)
                                .add(choiceKey("BDM").set(Choice("ON"))),
                            10.seconds
                        )
                    }
                )

                // TODO check responses

                // Set to FULL power mode
                lgsfLp.submitAndWait(
                    Setup(myPrefix, "select", obsId)
                        .add(choiceKey("enabled").set(Choice("OUT"))),
                    defaultTimeout
                )

                // Open BTO Loops
                val openLoopResponses = par (
                    {
                        lgsfOp.submitAndWait(
                            Setup(myPrefix, "follow", obsId)
                            .add(choiceKey("mode").set(Choice("OFF"))),
                            10.seconds
                        )
                    },
                    {
                        lgsfTe.submitAndWait(
                            Setup(myPrefix, "follow", obsId)
                                .add(booleanKey("fcsLut").set(false))
                                .add(booleanKey("fcsLutUpdate").set(false))
                                .add(booleanKey("tcskm").set(false))
                                .add(booleanKey("rtcFsm").set(false))
                                .add(booleanKey("rtcAcq").set(false)),
                            10.seconds
                        )
                    }
                )

                // Set laser components to STANDBY
                lgsfLaser.submitAndWait(
                    Setup(myPrefix, "laserState", obsId)
                        .add(choiceKey("state").set(Choice("STANDBY"))),
                    defaultTimeout
                )

                // Park BTO Mechanisms
                val parkResponses = par (
                    {
                        lgsfOp.submitAndWait(
                            Setup(myPrefix, "park", obsId),  // TBD
                            10.seconds
                        )
                    },
                    {
                        lgsfTe.submitAndWait(
                            Setup(myPrefix, "park", obsId),  // TBD
                            10.seconds
                        )
                    }
                )
            }
        }

        state(READY) {
            entry {
                info("Entering LGSF READY state")
                currentState = READY
                publishState(READY)

                // Close BDM shutter
                lgsfBdm.submitAndWait(
                    Setup(myPrefix, "select", obsId)
                        .add(choiceKey("BDM").set(Choice("OFF"))),
                    10.seconds
                )

                // Set to FULL power mode
                lgsfLp.submitAndWait(
                    Setup(myPrefix, "select", obsId)
                        .add(choiceKey("enabled").set(Choice("OUT"))),
                    defaultTimeout
                )

                // Set laser components to ON
                lgsfLaser.submitAndWait(
                    Setup(myPrefix, "laserState", obsId)
                        .add(choiceKey("state").set(Choice("ON"))),
                )

                // Form asterism
                val commandParams: Params = stateParamsFlag.value()
                val asterismType = commandParams.kGet(choiceKey("asterismType")).head()
                lgsfTe.submitAndWait(
                    Setup(myPrefix, "asterism", obsId)
                        .add(choiceKey("asterism").set(asterismType)),
                    defaultTimeout
                )
            }
        }

        state(ONLOW) {
            entry {
                info("Entering LGSF ON-LOW state")
                currentState = ONLOW
                publishState(ONLOW)

                // Set to LOW power mode
                lgsfLp.submitAndWait(
                    Setup(myPrefix, "select", obsId)
                        .add(choiceKey("enabled").set(Choice("IN"))),
                    defaultTimeout
                )

                // Set laser components to OBSERVATION
                lgsfLaser.submitAndWait(
                    Setup(myPrefix, "laserState", obsId)
                        .add(choiceKey("state").set(Choice("OBSERVATION"))),
                )

                // Close BTO loops
                val closeLoopsResponses = par (
                    {
                        lgsfOp.submitAndWait(
                            Setup(myPrefix, "follow", obsId)
                                .add(choiceKey("mode").set(Choice("PAC"))),
                            10.seconds
                        )
                    },
                    {
                        lgsfTe.submitAndWait(
                            Setup(myPrefix, "follow", obsId)
                                .add(booleanKey("fcsLut").set(true))
                                .add(booleanKey("fcsLutUpdate").set(true))
                                .add(booleanKey("tcskm").set(true))
                                .add(booleanKey("rtcFsm").set(true))
                                .add(booleanKey("rtcAcq").set(true)),
                            10.seconds
                        )
                    }
                )
            }
        }

        state(ON) {
            entry {
                info("Entering LGSF ON state")
                currentState = ON
                publishState(ON)

                // Close BDM shutter
                lgsfBdm.submitAndWait(
                    Setup(myPrefix, "select", obsId)
                        .add(choiceKey("BDM").set(Choice("OFF"))),
                    10.seconds
                )

                // Set to FULL power mode
                lgsfLp.submitAndWait(
                    Setup(myPrefix, "select", obsId)
                        .add(choiceKey("enabled").set(Choice("OUT"))),
                    defaultTimeout
                )

                // Set laser components to OBSERVATION
                lgsfLaser.submitAndWait(
                    Setup(myPrefix, "laserState", obsId)
                        .add(choiceKey("state").set(Choice("OBSERVATION"))),
                )

                // Close BTO loops
                val closeLoopsResponses = par (
                    {
                        lgsfOp.submitAndWait(
                            Setup(myPrefix, "follow", obsId)
                                .add(choiceKey("mode").set(Choice("PAC"))),
                            10.seconds
                        )
                    },
                    {
                        lgsfTe.submitAndWait(
                            Setup(myPrefix, "follow", obsId)
                                .add(booleanKey("fcsLut").set(true))
                                .add(booleanKey("fcsLutUpdate").set(true))
                                .add(booleanKey("tcskm").set(true))
                                .add(booleanKey("rtcFsm").set(true))
                                .add(booleanKey("rtcAcq").set(true)),
                            10.seconds

                        )
                    }
                )

                // Open LLT cover
                lgsfTe.submitAndWait(
                    Setup(myPrefix, "lltCover", obsId)
                        .add(choiceKey("select").set(Choice("OPEN"))),
                    10.seconds
                )
            }

        }

        state(OBSERVATION) {
            entry {
                info("Entering LGSF OBSERVATION state")
                currentState = OBSERVATION
                publishState(OBSERVATION)

                // Open BDM shutter
                lgsfBdm.submitAndWait(
                    Setup(myPrefix, "select", obsId)
                        .add(choiceKey("BDM").set(Choice("OFF"))),
                    10.seconds
                )
            }
        }

    }

    fun checkTransition(newState: String): Boolean {
        when (currentState) {
            OFF -> return newState == STANDBY
            STANDBY -> return newState == READY || newState == OFF
            READY -> return newState == ONLOW || newState == ON || newState == STANDBY
            ONLOW -> return newState == ON || newState == READY
            ON -> return newState == OBSERVATION || newState == READY
            OBSERVATION -> return newState == ON || newState == STANDBY
        }
    }

    stateParamsFlag.bind(lgsfFsm)
    onSetup("setState") { command ->
        // set global obsId if set in command
        obsId = command.obsId ?: ""

        // put command params in Command Flag
        stateParamsFlag.set(command.params)

        val setStateKey = choiceKey("state")
        val newState = command(setStateKey).head().toString()

        if (checkTransition(newState)) {
            become(newState)
        }
    }

    // start FSM
    lgsfFsm.start()
}