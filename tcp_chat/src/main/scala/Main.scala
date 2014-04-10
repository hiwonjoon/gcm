import akka.actor.{ Props, ActorSystem }
import Handler._
import Server.TcpServer

object Main extends App {
  val system = ActorSystem("server")
  val service = system.actorOf(TcpServer.props(ChatHandler), "ServerActor")
}