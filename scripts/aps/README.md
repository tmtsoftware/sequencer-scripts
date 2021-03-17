# APS Sequencing Prototypes

This package contains APS prototype sequencer scripts.

**Version compatibility**: ESW 0.2.1, CSW 3.0.0


## Prototypes in the package



| prototype | sub-package | test suite filename |
|-------------------|-----|-----|
| Rigid body segment figure correction | rigidbodysegmentfigurecorrection | RigidBodyTest.scala |
| PIT Loop | pitLoop | PitLoopTest.scala |
| Achieve stable PIT Tracking | rigidbodym3alignment | PitLoopTest.scala |
| APS Setup | setupAps | SetupAps.scala |

Unit test suites for APS can be found in the tests/aps folder of the sequencer-scripts project



## Running the tests
1. Install CSW 3.0.0
2. Intall ESW 0.2.1
3. download or clone this repo branch and run "sbt test -mem 2048"

## Description of the prototypes
### Rigid body segment figure correction
This prototype exercises the most complex part of the rigid body segment figure correction procedure involving a heirarchy
of sequencers, and looping over parrallel execution of sequences.

APS calls sequencers within parallel and looping operations.  The example used for the prototype is within the Rigid Body and Segment Figure Correction APS use case, but there are many other examples as well.
The prototype has a top-level sequencer that contains a step that loops over two parallel threads of execution, one that takes an exposure as one of its steps, the other does some processing of the previous loop’s exposure data.
The two threads of execution are implemented as sequences, because the tasks performed are high level enough to be sequence steps (such as taking an exposure) and so that a sequence manager can be utilized to view progress and potentially take appropriate action when errors or exceptions occur.

### PIT Loop
The area of interest to the prototype is creating a sequencer that continuously loops with control commands to start and stop the loop.

APT and PIT sequencers need to be implemented as iterative loops that continue to iterate until commanded to stop.  The state of the looping execution is important as certain commands will be accepted only when the loop is stopped and others only when the loop is running.
External start and stop commands need to be supported.

The pattern to accomplish the goals of the prototype is to create a single State Machine oriented script.  This enables the features of supporting only certain commands when the loop is running and only certain commands when the loop is stopped.

The other feature that needed design was the fact that an indeterminate loop should not be implemented within a sequence onSetup() command, because the command would not return immediately (it in fact could take hours).  For this reason a loopAsync block is used to create a separate thread of execution for the continuous loop.

The prototype script uses two internal control variables (vars): pitLoopControlFlag and pitLoopStoppedFlag.  

The pitLoopControlFlag is set to true prior to starting the asynchronous loop and set to false when the “stop-pit-loop” command is received.  The asynchronous loop checks this flag and stops when it is false.

The pitLoopStoppedFlag is used to accurately control the State Machine.  It is set to true when the asynchronous loop actually completed.  When the “stop-pit-loop” command executes, it does not immediately change the state to “OFF” (from ON).  It waits for the state of the pitLoopStoppedFlag to become true.  In this way, the ON/OFF state machine is kept in correct sync with the asynchronous loop execution state.

### Achieve stable PIT tracking
The M3 alignment procedure needs to command the PIT loop to start and wait until it becomes stable.  This requires communication between the two sequencers and a reasonable way to check repeatedly until the value associated with stability comes within the desired threshold.

The design for this is a common one for ESW, where a state based on an event is reevaluated each time an event is received.  This is a two step sequence (Start PIT Tracking, Wait For Pit Stable).  The Wait for Pit Stable step starts a FSM which tests the value from an event (from the PIT loop) for being within a tolerance.  Once tolerance is reached, the state changes and the FSM completes.  The Wait For Pit Stable step is waiting on FSM completion and when it is complete, the step returns.

### APS Setup
The area of interest to the prototype is how the CreateRefMap sequence can be both a standalone alignment procedure and an optional part of every other alignment procedure thus reusing the sequence (or script code).

The design here is to use a top-level sequence that all alignment procedures use, with two parameters passed in: needNewRefMap and needStarAcq.  The top level sequence submits sequences to two lower-level sequencers, 
one that calls the create ref map script/code, the other commands OCS to aquire a star.

The parameters control whether to call entire sequences (needAcq) or if steps will be included in the submitted sequence (needNewRefMap).
Using these parameters in this way enables the following behaviors to be achieved:

| needNewRefMap | need Acq | Behavior |
|-------------------|-----|-----|
| true| true | A procedure that needs to acquire a new star and needs a new reference map |
| false | true | A procedure that does not need a new reference map |
| true | false | A reference map only procedure* |

In the final behavior, a reference map only procedure is achieved by APS Setup being the only step in the procedure.

The test cases for this prototype also applied a second command to submit (that printed out text) in order to verify that the top level sequence does indeed wait for the step and its sub-sequences to complete prior to moving on to the next step in the top level sequence.