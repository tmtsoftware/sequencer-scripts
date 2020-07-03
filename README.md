# Sequencer Script

This repo contains subsystem specific sequencer scripts.

## Version Compatibility

| sequencer-scripts | esw        | csw        |
| ----------------- | ---------- | ---------- |
| v0.1.0            | v0.1.0     | v2.0.0     |
| v0.1.0-RC3        | v0.1.0-RC3 | v2.0.0-RC3 |
| v0.1.0-RC2        | v0.1.0-RC2 | v2.0.0-RC2 |
| v0.1.0-RC1        | v0.1.0-RC1 | v2.0.0-RC1 |

## Adding new scripts

`sequencer-scripts` repo is restricted and limited people have admin access to make changes to master and merge pull requests.

Script writers should follow steps mentioned below to add/update scripts

1. Fork `sequencer-scripts` repo
    1. One can fork it to their personal repository and keep working on that fork
    1. Or fork it under your own organization

1. Add new scripts into`scripts` directory under specific `subsystem`.  If your subsystem doesn't exist, create a directory with the name of your subsystem in the `scripts` directory, e.g. `scripts/wfos`.  Also add an observing mode mapping configuration file to provide a mapping from observing modes to scripts in your new directory named your subsystem with the `.conf` extension, e.g. `scripts/wfos/wfos.conf`.  Then, include the new configuration file in the `scripts/application.conf`, for example, if you have newly created `scripts/wfos/wfos.conf`, then add line `include "wfos/wfos.conf"` in `scripts/application.conf` file

1. Add an entry for each observing mode into the subsystem-specific observing mode mapping configuration.  It should include a `scriptClass` property pointing to script file where script logic resides, for example,

    ```hocon
      scripts {
        iris {
          IRIS_darknight {
            scriptClass = iris.Darknight
          }
        }
      }
    ```

    Note: the same script class can be used for multiple observing modes.

1. Once all the changes are completed in a forked repo, then you can submit a pull request to upstream which is `tmtsoftware/sequencer-scripts` repo in this case

1. Admins of `sequencer-scripts` repo will then review changes and merge it to `master`

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

    ```bash
        run sequencer -s <Subsystem> -m <Observing_Mode>
    ```
    Please note that configuration for IRIS (subsystem) and IRIS_darknight (observing mode) should be there in
    config file.

    For example, following command will start IRIS IRIS_darknight script i.e. **_Darknight.kts_** script
    Confiuration for this exist in iris.conf file under iris scripts folder.
    
    ```bash
        run sequencer -s IRIS -m IRIS_darknight
    ```

1. At this stage, your `Sequencer` will be started with provided `script` and waiting for `Sequence` to be received for execution

## Submitting Sequence to Sequencer

Once you have started `Sequencer` with appropriate `script`, next step would be to submit a `Sequence` to the `Sequencer` for execution.

Use [esw-shell](https://github.com/tmtsoftware/esw/tree/master/esw-shell) to do this and more which has detailed documentation on how to submit sequence to sequencer.
