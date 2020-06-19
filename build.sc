import $ivy.`com.lihaoyi::mill-contrib-bloop:$MILL_VERSION`
import $ivy.`com.goyeau::mill-scalafix:c71a533`
import $ivy.`io.github.davidgregory084::mill-tpolecat:0.1.3`

import mill._
import mill.scalalib._
import mill.scalajslib._
import publish._
import mill.scalalib.scalafmt._
import com.goyeau.mill.scalafix.ScalafixModule
import io.github.davidgregory084.TpolecatModule
object Deps {
  val scala211 = "2.11.12"
  val scala212 = "2.12.10"
  val scala213 = "2.13.2"
  val scalaJS06 = "0.6.32"
  val scalaJS1 = "1.0.0"

  val scalaVersion = scala213

  val decline = ivy"com.monovore::decline:1.2.0"
  val declineEnumeratum = ivy"com.monovore::decline-enumeratum:1.2.0"

  object fastparse extends Dep("com.lihaoyi::fastparse", "2.3.0")

  object zio extends Dep("dev.zio::zio", "1.0.0-RC20") {

    object test extends Dep(sub("test"), version) {
      object sbt extends Dep(sub("sbt"), version)
    }

    object config extends Dep(sub("config"), "1.0.0-RC20") {
      object derivation extends Dep(sub("derivation"), version)
    }

    object logging extends Dep(sub("logging"), "0.3.1") {
      object sl4j extends Dep(sub("-slf4j"), version)
    }

  }

  abstract class Dep(val notation: String, val version: String) {
    def apply() = ivy"$notation:$version"

    def sub(subArtifact: String): String = notation + s"-$subArtifact"
  }
}

trait PlaygroundScalaModule extends ScalaModule with ScalafmtModule
with ScalafixModule with TpolecatModule {

  def scalaVersion = T { Deps.scalaVersion }

  def ivyDeps = Agg(
    Deps.fastparse()
  )

  trait Tests extends super.Tests with PlaygroundScalaTestModule
}

trait PlaygroundScalaTestModule extends ScalaModule with TestModule
with ScalafmtModule with ScalafixModule with TpolecatModule {

  def ivyDeps = Agg(
    Deps.zio.test(),
    Deps.zio.test.sbt()
  )

  def testFrameworks =
    Seq("zio.test.sbt.ZTestFramework")
}

object playground extends Module {
  object jsonnet extends PlaygroundScalaModule {

    object test extends Tests
  }
}
