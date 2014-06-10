/**
 * Created by wonjoon-g on 2014. 6. 10..
 */
package core;
import akka.actor.{ActorRef, Actor, ActorSystem, Props};
import scala.collection.mutable.HashMap
import akka.event.Logging
import scala.compat.Platform

case class GetVector(id:String,sendTo:ActorRef)
case class Vectors(id:String,vec:Tuple2[Int,Int])

class InformationTree(parent : ActorRef) extends Actor {
  var user_vector_map = new HashMap[String,ActorRef];
  def receive = {
    case GetVector(_,sendTo) => {
      user_vector_map.foreach{case(id,ref) => {ref ! GetVector(id,sendTo)}}
    }
    case a:Job => {
      a.getId().foreach(id => {
        user_vector_map.get(id) match {
          case Some(elem) => elem.forward(a)
          case _ =>
            val new_user = context.system.actorOf(Props(new UserVector(id)))
            user_vector_map.put(id,new_user);
            new_user.forward(a);
        }
      })
    }
    case _ => Logging(context.system, this).info("case _ in Information Tree")
  }

}

class UserVector(id:String) extends Actor {
  val maximum = 120 * 1000; //120seconds.
  var last_inserted : Long = 0;
  var last_vector_calculated : Long = 0;
  var list : scala.collection.immutable.List[Job] = Nil;
  def receive = {
    case GetVector(_,sendTo) => {
      if( last_vector_calculated >= last_inserted )
      {}
      else {
        val userMove = list.count(job => job.getTime() >= last_inserted - 2 * 1000 && job.isInstanceOf[UserMove])
        val userBattleResult = list.count(job => job.getTime() >= last_inserted - 2 * 1000 && job.isInstanceOf[UserBattleResult])
        //println(id + (userMove, userBattleResult).toString())
        sendTo ! Vectors(id, Tuple2(userMove, userBattleResult))

        last_vector_calculated = Platform.currentTime
      }
    }
    case a : Job => {
      last_inserted = Platform.currentTime;
      list.filterNot(job => job.getTime() < a.getTime() - maximum)
      list = a::list;
    }
    case _ => Logging(context.system, this).info("case _ in User Vector")
  }
}

