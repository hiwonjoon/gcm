import sbt._
import Keys._
import com.typesafe.sbt.SbtAspectj.AspectjKeys._
import com.typesafe.sbt.SbtAspectj.{ Aspectj, aspectjSettings }


object RootBuild extends Build {
  lazy val buildSettings = Defaults.defaultSettings ++ Seq( organization := "com.typesafe.sbt.aspectj")

  lazy val akka_esper = project in file("akka-esper") dependsOn common
  lazy val core = Project(
    "core",
    file("core"),
    dependencies = Seq(common), 
    settings =buildSettings ++ 
              aspectjSettings ++ 
              Seq( 
                compileOnly in Aspectj := true,
                products in Compile <++= products in Aspectj,
                javaOptions in run <++= weaverOptions in Aspectj,
                javaOptions in run ++= Seq(
                  "-javaagent:" + System.getProperty("user.home") + s"/.ivy2/cache/org/aspectj/aspectjweaver/jars/aspectjweaver-1.7.3.jar",
                  "-Xms128m -Xmx256m",
                  "-XX:+ExtendedDTraceProbes"
                )
                fork in run := true
              )
            

  )

  lazy val common = project in file("common")
  lazy val web_interface = project in file("web-interface") dependsOn common
  lazy val root = project in file(".") aggregate(common,core,akka_esper)
}
