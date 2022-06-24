package pytest

import csw.params.commands.{CommandName, Setup}
import csw.params.core.generics.KeyType
import csw.params.core.generics.KeyType.IntKey
import csw.params.core.models.ObsId
import csw.prefix.models.Prefix
import csw.prefix.models.Subsystem.ESW
import esw.ocs.impl.pyscript.PyScriptApi
import org.graalvm.polyglot._

import java.io.File

// XXX TODO FIXME: Make paths absolute!
object PyScriptApiTest extends App {
  println("Hello from Scala!")
  //  val context = Context.create()
  val context =  Context.newBuilder("python")
    .allowAllAccess(true)
    .option("python.PythonPath", "pyScripts")
    .option("python.ForceImportSite", "true")
    .option("python.Executable", "pyScripts/venv/bin/graalpython")
    .build()
  context.eval("python", "print('Hello from Python!')")

//  val baseClass = Source.newBuilder("python", new File("pyScripts/common/ScriptBase.py")).build()
//  context.eval(baseClass)
  val source = Source.newBuilder("python", new File("pyScripts/common/MyPyScript.py")).build()

  val x = context.eval(source)
  println(s"XXX x = $x")
  val clazz = context.getPolyglotBindings.getMember("MyPyScript")
  val instance = clazz.newInstance()
  val res = instance.as(classOf[PyScriptApi])

  val commandKey = KeyType.IntKey.make("command")
  val temperatureFsmKey = IntKey.make("temperatureFsm")
  val setupCommand = Setup(Prefix(ESW, "test"), CommandName("move"), Some(ObsId.apply("2020A-001-123")))
    .madd(commandKey.set(10), temperatureFsmKey.set(11))

  res.executeGoOnline()
  res.execute(setupCommand)
}

