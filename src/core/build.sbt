name := "core"

version := "1.0"

resolvers += "spray repo" at "http://repo.spray.io"

resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  "com.typesafe.akka"      %% "akka-actor"            % "2.2.3",
  "com.typesafe.akka"      %% "akka-remote"           % "2.2.3",
  "com.typesafe.akka"      %% "akka-cluster"          % "2.2.3",
  "com.typesafe.akka"      %% "akka-slf4j"            % "2.2.3",
  "io.spray"                % "spray-can"             % "1.2.0",
  "io.spray"                % "spray-client"          % "1.2.0",
  "io.spray"                % "spray-routing"         % "1.2.0",
  "io.spray"               %% "spray-json"            % "1.2.5",
  "org.eigengo.monitor"     % "agent-akka"            % "0.2-SNAPSHOT",
  "org.eigengo.monitor"     % "output-statsd"         % "0.2-SNAPSHOT",
  "org.specs2"             %% "specs2"                % "2.2.2"        % "test",
  "io.spray"                % "spray-testkit"         % "1.2.0"        % "test",
  "com.typesafe.akka"      %% "akka-testkit"          % "2.2.3"        % "test"
)

