name := "web-interface"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache
)     

play.Project.playScalaSettings

libraryDependencies ++= Seq(
  "com.typesafe.akka"      %% "akka-actor"            % "2.2.3",
  "com.typesafe.akka"      %% "akka-remote"           % "2.2.3",
  "com.typesafe.akka"      %% "akka-slf4j"            % "2.2.3",
  "org.xerial" % "sqlite-jdbc" % "3.7.15-M1"
)
