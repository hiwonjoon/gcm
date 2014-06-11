import sbt._
import Keys._

object RootBuild extends Build {
  lazy val akka_esper = project in file("akka-esper") dependsOn common
  lazy val core = project in file("core") dependsOn common
  lazy val common = project in file("common")
  lazy val web_interface = project in file("web-interface") dependsOn common
  lazy val root = project in file(".") aggregate(common,core,akka_esper)
}
