import sbt._
import Keys._

object RootBuild extends Build {
  lazy val common = RootProject(file("../common"))
  lazy val root = Project(id = "akka-esper", base = file(".")) dependsOn (common)
}
