package Handler

import akka.actor.{ Props, ActorRef, Actor }
import akka.io.Tcp._
import akka.io.Tcp.Received
import Server._

trait HandlerProps {
  def props(connection: ActorRef, sessions: Sessions): Props
}

abstract class Handler(val connection: ActorRef, val sessions: Sessions) extends Actor {

  val abort = "(?i)abort".r
  val confirmedClose = "(?i)confirmedclose".r
  val close = "(?i)close".r

  def receive: Receive = {
    case Received(data) =>
      data.utf8String.trim match {
        case abort() => connection ! Abort
        case confirmedClose() => connection ! ConfirmedClose
        case close() => connection ! Close
        case str => {
          received(str)
        }
      }
    case PeerClosed =>
      peerClosed()
      stop()
    case ErrorClosed =>
      errorClosed()
      stop()
    case Closed =>
      closed()
      stop()
    case ConfirmedClosed =>
      confirmedClosed()
      stop()
    case Aborted =>
      aborted()
      stop()
  }

  def received(str: String): Unit

  def peerClosed() {
    println("PeerClosed")
  }

  def errorClosed() {
    println("ErrorClosed")
  }

  def closed() {
    println("Closed")
  }

  def confirmedClosed() {
    println("ConfirmedClosed")
  }

  def aborted() {
    println("Aborted")
  }

  def stop() {
    println("Stopping")
    sessions.RemoveSession(connection)
    context stop self
  }
}
