/**
 * Created by wonjoon-g on 2014. 6. 11..
 */
package core

import akka.actor._;
import scala.collection.immutable.Seq
import scala.collection.mutable.HashMap

class OneTreeGatherer(user_size:Int,backend:ActorRef,sendTo:ActorRef) extends Actor {
  var user_remained = user_size;
  //println("user_size : " + user_size)

  if( user_size == 0 )
    sendTo ! AllVectorSent(backend)

  def receive = {
    case a:Vectors => {
      sendTo ! a
      user_remained -= 1;
      if( user_remained <= 0 ) {
        sendTo ! AllVectorSent(backend)
        context.system.stop(self)
      }
    }
  }
}
class Gatherer(backend_list:scala.collection.mutable.Seq[ActorRef],sendTo:ActorRef) extends Actor{
  var backend_remain : scala.collection.mutable.ArrayBuffer[(ActorRef,Int)] = (backend_list.map { actor => (actor,10) }).to[scala.collection.mutable.ArrayBuffer]
  val aggregation = new HashMap[String,Seq[Int]]

  def receive = {
    case Vectors(id,seq) => {
      aggregation.get(id) match {
        case Some(elem) => {
          val new_elem = (elem zip seq) map {
            p: (Int, Int) => p._1 + p._2
          }
          aggregation += (id -> new_elem)
        }
        case _ => {
          aggregation += (id -> seq)
        }
      }
    }
    case AllVectorSent(backend) => {
      backend_remain.find{case (actor,_) => actor == backend } match {
        case Some((actor,count)) => {
          backend_remain -= ((actor,count))
          backend_remain += ((actor,count-1))
        }
        case _ => println("? in Gatherer")
      }

      backend_remain = backend_remain.filterNot{case(actor,count) => count == 0};

      if( backend_remain.size == 0 )
      {
        aggregation.foreach {
          case (elem, vec) =>
            sendTo ! Vectors(elem, vec)
            context.system.stop(self)
        }
      }
    }
    case _ => println("?? in Gatherer")
  }
}
