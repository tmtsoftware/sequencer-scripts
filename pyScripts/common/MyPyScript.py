# import site
import java
import polyglot

from common.ScriptBase import ScriptBase

Setup = java.type('csw.params.commands.Setup')
Observe = java.type('csw.params.commands.Observe')
JKeyType = java.type('csw.params.javadsl.JKeyType')


class MyPyScript(ScriptBase):

    def onSetup(self, command: Setup):
        print(f"Called onSetup {command})")
        print(f"Command name = {command.commandName().name()}")
        print(f"obsId = {command.maybeObsId().get()}")
        print(f"source = {command.source()}")
        temperatureFsmKey = JKeyType.IntKey().make("temperatureFsm")
        print(f"temperatureFsm = {command.get(temperatureFsmKey).get()}")
        print(f"temperatureFsm units = {command.get(temperatureFsmKey).get().units()}")
        print(f"temperatureFsm value = {command.get(temperatureFsmKey).get().head()}")
        print(f"temperatureFsm value (using apply) = {command.apply(temperatureFsmKey).head()}")


polyglot.export_value("MyPyScript", MyPyScript)
