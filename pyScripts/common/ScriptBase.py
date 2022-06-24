import java
# noinspection PyUnresolvedReferences
import polyglot

print('Hello from PyScriptApiTest.py')
SequenceCommand = java.type('csw.params.commands.SequenceCommand')
ScalaSetup = java.type('csw.params.commands.Setup')
ScalaObserve = java.type('csw.params.commands.Observe')
JavaKeyType = java.type('csw.params.javadsl.JKeyType')
ScalaKeyType = java.type('csw.params.core.generics.KeyType')
ScalaParameter = java.type('csw.params.core.generics.Parameter')


class Parameter:
    param: ScalaParameter

    def __init__(self, param: ScalaParameter):
        self.param = param

    def keyName(self) -> str:
        return self.param.keyName()

class Setup:
    setup: ScalaSetup

    def __init__(self, setup: ScalaSetup):
        self.setup = setup

    def __call__(self, key: ScalaKeyType):
        return self.setup.get(key).get()

    def get(self, key: ScalaKeyType):
        return self.setup.get(key)

    def commandName(self) -> str:
        return self.setup.commandName().name()

    def obsId(self) -> str:
        return self.setup.maybeObsId().get()

    def source(self) -> str:
        return self.setup.source()

    def size(self) -> int:
        return self.setup.size()

    def paramset(self) -> list:
        return self.setup.jParamSet()


class Observe:
    observe: ScalaObserve

    def __init__(self, observe: ScalaObserve):
        self.observe = observe

    def commandName(self) -> str:
        return self.observe.commandName().name()

    def obsId(self) -> str:
        return self.observe.maybeObsId().get()

    def source(self) -> str:
        return self.observe.source()


class ScriptBase:

    def execute(self, command: SequenceCommand):
        className = command.getClass().getName()
        if className == 'csw.params.commands.Setup':
            self.onSetup(Setup(command))
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


IntKey = JavaKeyType.IntKey()
