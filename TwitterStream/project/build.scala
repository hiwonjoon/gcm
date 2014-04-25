import sbt._

object RootBuild extends Build {
  lazy val root = Project(id = "TwitterStream",
                          base = file("."))
}