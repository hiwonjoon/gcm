/**
 * Created by wonjoon-g on 2014. 6. 11..
 */
package core

import akka.actor._;
import scala.collection.immutable.Seq
import scala.collection.mutable.HashMap

class OneTreeGatherer(user_size:Int,backend:ActorRef,sendTo:ActorRef) extends Actor {
  val aggregation = new HashMap[String,Seq[Int]];

  def receive = {
    case Vectors(id,seq) => {
      aggregation += (id -> seq)
      if( aggregation.size >= user_size )
      {
        sendTo ! VectorList(backend,aggregation)
      }
    }
  }
}
class Gatherer(backend_list:scala.collection.immutable.Seq[ActorRef],sendTo:ActorRef) extends Actor{
  var backend_remain = backend_list;
  val aggregation = new HashMap[String,Seq[Int]]

  def receive = {
    case VectorList(backend,map) => {
      val count = backend_remain.find(actor => actor == backend);
      if( count == 0 )
        println("? in Gatherer")

      backend_remain = backend_remain.filterNot(actor => actor == backend);
      map.foreach {
        case (id, seq) =>
          aggregation.get(id) match {
            case Some(elem) => {
              val new_elem = (elem zip seq) map {
                p:(Int,Int) => p._1 + p._2
              }
              aggregation += (id -> new_elem)
            }
            case _ => {
              aggregation += (id -> seq)
            }
          }
      }

      if( backend_remain.size == 0 )
      {
        aggregation.foreach {
          case (elem, vec) =>
            sendTo ! Vectors(elem, vec)
        }
      }
    }
    case _ => println("?? in Gatherer")
  }
}
