from common.ScriptBase import *

class MyPyScript(ScriptBase):

    def onSetup(self, setup: Setup):
        print(f"Called onSetup {setup})")
        print(f"Command name = {setup.commandName()}")
        print(f"obsId = {setup.obsId()}")
        print(f"source = {setup.source()}")
        temperatureFsmKey = IntKey.make("temperatureFsm")
        print(f"temperatureFsm units = {setup(temperatureFsmKey).units()}")
        print(f"temperatureFsm head = {setup(temperatureFsmKey).head()}")
        print(f"temperatureFsm source = {setup.source()}")
        print(f"temperatureFsm size = {setup.size()}")
        print(f"temperatureFsm paramset = {setup.paramset()}")



polyglot.export_value("MyPyScript", MyPyScript)
