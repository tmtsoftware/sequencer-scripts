import java

print('Hello from PyScriptApiTest.py')
SequenceCommand = java.type('csw.params.commands.SequenceCommand')
Setup = java.type('csw.params.commands.Setup')
Observe = java.type('csw.params.commands.Observe')
JKeyType = java.type('csw.params.javadsl.JKeyType')


class ScriptBase:

    def execute(self, command: SequenceCommand):
        className = command.getClass().getName()
        if className == 'csw.params.commands.Setup':
            self.onSetup(command)
        elif className == 'csw.params.commands.Observe':
            self.onObserve(command)

    def onSetup(self, command: Setup):
        pass

    def onObserve(self, command: Observe):
        pass

    # def executeGoOnline(self):
    #     print('XXX python version of executeGoOnline()')

    def executeGoOffline(self):
        pass

    def executeShutdown(self):
        pass

    def executeAbort(self):
        pass

    def executeNewSequenceHandler(self):
        pass

    def executeStop(self):
        pass

    def executeDiagnosticMode(self, startTime: str, hint: str):
        pass

    def executeOperationsMode(self):
        pass

    def executeExceptionHandlers(self, ex: str):
        pass

    def shutdownScript(self):
        pass


