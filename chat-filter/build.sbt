name := "chat-filter"

version := "1.0"

scalaVersion := "2.11.0"

resolvers += "spray repo" at "http://repo.spray.io"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.2",
  "io.spray" %% "spray-json"  % "1.2.6"
)


