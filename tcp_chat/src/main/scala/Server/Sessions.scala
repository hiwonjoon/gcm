package Server

import scala.collection.mutable.ArrayBuffer
import akka.actor.{Actor, ActorRef}

trait Sessions { this: Actor =>
  private var sessions = new ArrayBuffer[ActorRef]

  def GetSessions():ArrayBuffer[ActorRef] = sessions

  def AddSession(session:ActorRef) {
    sessions += session
  }

  def RemoveSession(session:ActorRef) {
    sessions -= session
  }
}