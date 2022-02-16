# Sequencer Script

This repo contains subsystem specific sequencer scripts.

## Version Compatibility

| sequencer-scripts | esw       | csw        |
|-------------------|-----------|------------|
| v0.4.0            | v0.4.0    | v4.0.1     |
| v0.4.0-RC1        | v0.4.0-RC1 | v4.0.1-RC1 |
| v0.3.0            | v0.3.0    | v4.0.0     |
| v0.3.0-RC2        | v0.3.0-RC2 | v4.0.0-RC2 |
| v0.3.0-RC1        | v0.3.0-RC1 | v4.0.0-RC1 |
| v0.3.0-M1         | v0.3.0-M1 | v4.0.0-M1  |
| v0.2.0            | v0.2.1    | v3.0.1     |
| v0.2.0-RC1        | v0.2.0-RC1 | v3.0.0-RC1 |
| v0.2.0-M1         | v0.2.0-M1 | v3.0.0-M1  |
| v0.1.0            | v0.1.0    | v2.0.0     |
| v0.1.0-RC3        | v0.1.0-RC3 | v2.0.0-RC3 |
| v0.1.0-RC2        | v0.1.0-RC2 | v2.0.0-RC2 |
| v0.1.0-RC1        | v0.1.0-RC1 | v2.0.0-RC1 |

## Adding new scripts

The `sequencer-scripts` repo is restricted and limited people have admin access to make changes to master and merge pull requests.

Script writers should follow steps mentioned below to add/update scripts:

1. Fork `sequencer-scripts` repo:
    1. You can fork it to your personal repository and keep working on that fork.
    1. Or you can fork it under your own organization.

1. Add new scripts into`scripts` directory under specific `subsystem`. If 
   your subsystem doesn't exist, create a directory with the name of your subsystem in the `scripts` directory, 
   e.g. `scripts/wfos`. Also add an observing mode mapping configuration file to provide a mapping from 
   observing modes to scripts in your new directory named your subsystem with the `.conf` extension, 
   e.g. `scripts/wfos/wfos.conf`. Then, include the new configuration file in the `scripts/application.conf`, 
   for example, if you have newly created `scripts/wfos/wfos.conf`, 
   then add line `include "wfos/wfos.conf"` in `scripts/application.conf` file

1. Add an entry for each observing mode into the subsystem-specific observing mode mapping configuration. It 
   should include a `scriptClass` property pointing to script file where script logic resides, for example,

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

1. Once all the changes are completed in a forked repo, then you can submit 
   a pull request to upstream which is `tmtsoftware/sequencer-scripts` repo in this case

1. Admins of the `sequencer-scripts` repo will then review the changes and merge them to `master`.

## Running script

### Prerequisite

The [CSW](https://github.com/tmtsoftware/csw) services need to be running before starting the components.
This is done by starting the `csw-services`
If you are not building csw from the sources, you can run `csw-services` as follows:

- Install `coursier` using steps described [here](https://tmtsoftware.github.io/csw/apps/csinstallation.html) and add TMT channel.
- Run `cs install csw-services`. This will create an executable file named `csw-services` in the default installation directory.
- Run `csw-services --help` to get more information.
- Run `csw-services start` to start all the csw services, for example, Location, Config, Alarm, AAS service etc

### Running Sequencer App with script

1. Run the `sbt` command at the root level of this repo

1. Within the `sbt` shell, run the following command which will read the _scripts.Subsystem.Observing_Mode.scriptClass_ configuration and start that script.

    ```bash
        run sequencer -s <Subsystem> -m <Observing_Mode>
    ```
    Please note that the configuration for the IRIS (subsystem) and the IRIS_darknight observing mode should be there in
    the config file.

    For example, the following command will start the IRIS IRIS_darknight script i.e. **_Darknight.kts_**.
    The Configuration for this exists in the iris.conf file under the iris scripts folder.
    
    ```bash
        run sequencer -s IRIS -m IRIS_darknight
    ```

1. At this stage, your `Sequencer` will be started with a provided `script` and will be waiting for a `Sequence` to be received for execution.

## Developing Scripts while interacting from ESW Eng UI
### Initial test setup
This setup assumes the `esw`, `csw`, and `sequencer-script` repos are available on the local machine.

1. In the ESW repo, run `sbt publishLocal`
1. In the Sequencer-Scripts repo, run `sbt -Ddev=true publishLocal`            
1. Checkout a compatible SHA of CSW and Start CSW services (including config service and keycloak) with the command : `sbt csw-services/run start -c`. 
   
   If the CSW repo is available on the local machine, use `Coursier` to start csw-services (See [here](#prerequisite)). 
1. Start ESW services with the command : `sbt esw-services/run start-eng-ui-services --scripts-version 0.1.0-SNAPSHOT`
1. Start the UI server in the ESW-OCS-Eng-UI repo with the command : `npm start`
1. Once the browser opens and the UI loads
    - Login  
    - Provision sequence components
    - Configure obs mode (say IRIS_Darknight)

### Script development flow
To develop a script so that the changes are reflected in the Sequencer and UI:
1. Unload the Sequencer being developed, for example ESW.IRIS_Darknight.
1. Since the script under development will be in a specific directory, eg: `scripts/esw`, the changes in other directories should not cause the sequencer to restart.
1. This can be done using the `watchSources` task in sbt, which defines the sources that will be watched while using sbt revolver. Since we need to watch changes only of a specific directory we can set it accordingly.
   For eg: `set watchSources := Seq(WatchSource(baseDirectory.value /"scripts/esw/"))`
1. Go to the Sequencer-scripts repo and start the Sequencer in watch mode by executing `sbt -Ddev=true ~reStart sequencer -s ESW -m IRIS_Darknight`


## Submitting Sequence to Sequencer

Once you have started the `Sequencer` with the appropriate `script`, the next step would be to submit a `Sequence` to the `Sequencer` for execution.

This can be done using the [esw-shell](https://github.com/tmtsoftware/esw/tree/master/esw-shell), which has detailed documentation on how to submit a sequence to the sequencer.
