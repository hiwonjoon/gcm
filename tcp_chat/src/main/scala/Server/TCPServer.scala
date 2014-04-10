package Server

import akka.io.{ IO, Tcp }
import java.net.InetSocketAddress
import Util._
import Handler._
import akka.actor.{ActorRef, Props}
import scala.collection.mutable.ArrayBuffer


object TcpServer {
  def props(handlerProps: HandlerProps): Props =
    Props(classOf[TcpServer], handlerProps)
}

class TcpServer(handlerProps: HandlerProps) extends Server with Sessions {

  import context.system

  IO(Tcp) ! Tcp.Bind(self, new InetSocketAddress(Conf.appHostName, Conf.appPort))

  override def receive = {
    case Tcp.CommandFailed(_: Tcp.Bind) => context stop self

    case Tcp.Connected(remote, local) =>
      val handler = context.actorOf(handlerProps.props(sender, this.asInstanceOf[Sessions]))
      sender ! Tcp.Register(handler)

      AddSession(sender)
  }
}