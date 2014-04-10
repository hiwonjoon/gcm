package Handler

import akka.actor._
import akka.util.ByteString
import akka.io.Tcp.Write

import Server._

object ChatHandler extends HandlerProps {
  def props(connection: ActorRef, sessions: Sessions) = Props(classOf[ChatHandler], connection, sessions)
}

class ChatHandler(connection: ActorRef, sessions: Sessions) extends Handler(connection, sessions) {

  /**
   * Broadcast incoming message.
   */
  def received(data: String) = {
    for(session <- sessions.GetSessions() if connection != session ) {
      session ! Write(ByteString(data + "\n"))
    }
  }
}