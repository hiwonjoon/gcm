import sbt._
import Keys._

object RootBuild extends Build {
  lazy val common = RootProject(file("../common"))
  lazy val root = Project(id = "web-interface", base = file(".")) dependsOn (common)
}
