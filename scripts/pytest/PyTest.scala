package pytest

import csw.params.core.models.ObsId
import org.graalvm.polyglot._
import org.graalvm.polyglot.proxy._

import java.io.File
import java.nio.file.Paths

object PyTest extends App {
  println("Hello from Scala!")
  val context =  Context.newBuilder("python").
    allowAllAccess(true).
    option("python.ForceImportSite", "true").
    option("python.Executable", "pyScripts/venv/bin/graalpython").
    build();
  context.eval("python", "print('Hello from Python!')")

  val source = Source.newBuilder("python", new File("pyScripts/common/Utils.py")).build();
  val res = context.eval(source).asHostObject[ObsId]()
  println(s"XXX res = $res (${res.getClass})")
}

