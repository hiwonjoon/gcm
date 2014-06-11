/**
 * Created by wonjoon-g on 2014. 6. 10..
 */
package core;
import akka.actor.{ActorRef, Actor, ActorSystem, Props};
import scala.collection.mutable.HashMap
import scala.collection.immutable.Seq
import akka.event.Logging
import scala.compat.Platform

case class GetPerformance(sendTo:ActorRef)

case class GetVector(id:String,sendTo:ActorRef)
case class Vectors(id:String,vec:Seq[Int])
case class VectorList(backend:ActorRef, map : HashMap[String,Seq[Int]])

class InformationTree(parent : ActorRef) extends Actor {
  //TODO : 유저 벡터 멥에서 데이터 없는 애들은 스스로 삭제 되는 코드 넣어야지.
  var user_vector_map = new HashMap[String,ActorRef];
  def receive = {
    case GetVector(_,sendTo) => {
      val oneTreeGatherer = context.system.actorOf(Props(new OneTreeGatherer(user_vector_map.size,parent,sendTo)));
      user_vector_map.foreach{
        case(id,ref) => {
          ref ! GetVector(id,oneTreeGatherer)
        }
      }
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
//      if( last_vector_calculated >= last_inserted )
//      {
//        sendTo ! Vectors(id, Nil)
//      }
//      else {
        val moveList = list.filter(job => job.getTime() >= last_inserted - 10 * 1000 && job.isInstanceOf[UserMove])

        val moveEast = moveList.count(job => job.asInstanceOf[UserMove].getDir() == 0)
        val moveWest = moveList.count(job => job.asInstanceOf[UserMove].getDir() == 1)
        val moveSouth = moveList.count(job => job.asInstanceOf[UserMove].getDir() == 2)
        val moveNorth = moveList.count(job => job.asInstanceOf[UserMove].getDir() == 3)

        val userBattleResult = list.count(job => job.getTime() >= last_inserted - 10 * 1000 && job.isInstanceOf[UserBattleResult])
        //println(id + (userMove, userBattleResult).toString())

        sendTo ! Vectors(id, moveEast::moveWest::moveSouth::moveNorth::userBattleResult::Nil )

        last_vector_calculated = Platform.currentTime
      }
//    }
    case a : Job => {
      last_inserted = Platform.currentTime;
      list = list.filterNot(job => job.getTime() < a.getTime() - maximum)
      list = a::list;
    }
    case _ => Logging(context.system, this).info("case _ in User Vector")
  }
}

