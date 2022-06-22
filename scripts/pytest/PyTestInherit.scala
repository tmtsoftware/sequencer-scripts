package pytest

import org.graalvm.polyglot._

import java.io.File

trait MyTrait {
  def foo(i: Int): String
}

object PyTestInherit extends App {
  println("Hello from Scala!")
  //  val context = Context.create()
  val context =  Context.newBuilder("python").
    allowAllAccess(true).
    option("python.ForceImportSite", "true").
    option("python.Executable", "pyScripts/venv/bin/graalpython").
    build();
  context.eval("python", "print('Hello from Python!')")

  val source = Source.newBuilder("python", new File("pyScripts/common/InheritTest.py")).build()

  val x = context.eval(source)
  println(s"XXX x = $x")
  val clazz = context.getPolyglotBindings.getMember("MyClass")
  val instance = clazz.newInstance()
  val res = instance.as(classOf[MyTrait])

  println(s"XXX res = ${res.foo(22)}")
}

