/**
 * Created by wonjoon-g on 2014. 6. 4..
 */
package core

import common.MachinePerformance
import scala.concurrent.duration._
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.RootActorPath
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.ClusterEvent.MemberUp
import akka.cluster.Member
import akka.cluster.MemberStatus
import com.typesafe.config.ConfigFactory

class CoreBackend extends Actor {
  val cluster = Cluster(context.system)

  val info_tree = context.system.actorOf(Props(new InformationTree(self)))
  val perf = new java_core.PerformanceMonitor;

  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive = {
    case job : Job =>
      info_tree ! job;
    case a:GetVector => info_tree ! a;
    case GetPerformance(sendTo) =>
      if(sendTo != null)
        sendTo ! MachinePerformance(perf.getCpuInfo(),perf.getMemoryInfo())
      else
        println(perf.getCpuInfo() + " " + perf.getMemoryInfo())
    case state : CurrentClusterState =>
      state.members.filter(_.status == MemberStatus.up) foreach register
    case MemberUp(m) => register(m)
  }

  def register(member:Member): Unit =
    if( member.hasRole("frontend"))
      context.actorSelection(RootActorPath(member.address) / "user" / "frontend") ! BackendRegistration

}

object CoreBackend {
  def main(args: Array[String]) : Unit = {
    val port = if(args.isEmpty) "0" else args(0)
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [backend]")).
      withFallback(ConfigFactory.load())

    val system = ActorSystem("core", config)
    system.actorOf(Props[CoreBackend], name = "backend");
  }
}
