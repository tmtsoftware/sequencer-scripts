# Sequencer Script

This repo contains subsystem specific sequencer scripts.

## Version Compaibilty
-----------------------------------------------------------

| sequencer-scripts | esw | csw |
|-------------------|-----|-----|

## Adding new scripts

1. Add new scripts into`scripts` directory under specific `subsystem`

1. Add scripts specific configuration file to the same directory where you have added new script. 
For example, `IRIS` subsystems `Darknight.kts` script and its corresponding configuration file `iris.conf` can reside at the same level inside `scripts/iris` directory

1. Script-specific configuration should include `scriptClass` property pointing to script file where script logic resides, for example,
    ```hocon
    scripts {
      iris {
        darknight {
          scriptClass = iris.Darknight
        }
      }
    }
    ```

1. Include new configuration file in the `scripts/application.conf`, 
for example, if you have newly added `scripts/iris/iris.conf` then add line `include "iris/iris.conf"` in `scripts/application.conf` file

## Running script

### Prerequisite
The [CSW](https://github.com/tmtsoftware/csw) services need to be running before starting the sequencer scripts.
This is done by starting the `csw-services.sh` script, you can get the script as follows:

1. Download compatible `csw-apps` zip from https://github.com/tmtsoftware/csw/releases.
You can refer [version compatibility section](#-version-compaibilty).

1. Unzip the downloaded zip.

1. Go to the bin directory where you will find `csw-services.sh` script.

1. Run `./csw_services.sh --help` to get more information.

1. Run `./csw_services.sh start` to start all the csw services, for example, _Location, Config, Alarm, AAS service_ etc

### Running Sequencer App with script

1. Run `sbt` command at root level of this repo

1. Within the `sbt` shell, run following command which will read _scripts.Subsystem.Observing_Mode.scriptClass_ configuration and start that script.
    ```
    run sequencer -s <Subsystem> -m <Observing_Mode>
    ```

    For example, following command will start iris darknight script i.e. **_Darknight.kts_** script 
    ```
    run sequencer -s IRIS -m darknight
    ``` 

1. At this stage, your `Sequencer` will be started with provided `script` and waiting for `Sequence` to be received for execution

## Submitting Sequence to Sequencer

Once you have started `Sequencer` with appropriate `script`, next step would be to submit a `Sequence` to the `Sequencer` for execution.
Use [csw-shell](https://github.com/tmtsoftware/csw-shell) to do this and more which has detailed documentation on how to submit sequence to sequencer.
