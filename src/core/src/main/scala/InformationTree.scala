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
case class DeleteUser()
case class GetVector(kind:Int,id:String,sendTo:ActorRef)
case class Vectors(id:String,vec:(Seq[Int]))
case class VectorsEmpty(id:String)
case class AllVectorSent(backend:ActorRef)
case class VectorList(backend:ActorRef, map : HashMap[String,Seq[Int]])

class InformationTree(parent : ActorRef) extends Actor {
  var user_vector_map = new HashMap[String,ActorRef];
  def receive = {
    case GetVector(kind,_,sendTo) => {
      val oneTreeGatherer = context.system.actorOf(Props(new OneTreeGatherer(user_vector_map.size,parent,sendTo)));
      user_vector_map.foreach{
        case(id,ref) => {
          ref ! GetVector(kind,id,oneTreeGatherer)
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
    case DeleteUser => {
      sender ! akka.actor.PoisonPill
    }
    case _ => Logging(context.system, this).info("case _ in Information Tree")
  }

}

class UserVector(id:String) extends Actor {
  val maximum = 120 * 1000; //120seconds.
  //val maximum = 10 * 60 * 1000; //10 MINUTES
  val check_interval = 10 * 1000;

  var last_inserted : Long = 0;
  var last_vector_calculated : Long = 0;
  var list : scala.collection.immutable.List[Job] = Nil;
  def receive = {
    case GetVector(kind,_,sendTo) => {
        if( last_inserted <= Platform.currentTime - maximum )
        {
          sender ! DeleteUser()
        }
        else if( last_vector_calculated >= last_inserted )
        {
          sendTo ! VectorsEmpty(id)
        }
        else {
          val moveList = list.filter(job => job.getTime() >= last_inserted - check_interval && job.isInstanceOf[UserMove])

          val moveEast = moveList.count(job => job.asInstanceOf[UserMove].getDir() == 0)
          val moveWest = moveList.count(job => job.asInstanceOf[UserMove].getDir() == 1)
          val moveSouth = moveList.count(job => job.asInstanceOf[UserMove].getDir() == 2)
          val moveNorth = moveList.count(job => job.asInstanceOf[UserMove].getDir() == 3)

          val battleList = list.filter(job => job.getTime() >= last_inserted - check_interval && job.isInstanceOf[UserBattleResult])

          val pveResult = battleList.count(job => job.asInstanceOf[UserBattleResult].winner.id == id && job.asInstanceOf[UserBattleResult].winner.isNpc == false && job.asInstanceOf[UserBattleResult].loser.isNpc == true)

          val pvpList = battleList.filter(job => job.asInstanceOf[UserBattleResult].winner.id == id && job.asInstanceOf[UserBattleResult].winner.isNpc == false && job.asInstanceOf[UserBattleResult].loser.isNpc == false)
          var pvpResult:Seq[Int] = Nil
          for(i <- 0 to 15)
          {
            pvpResult :+= pvpList.count(job => job.asInstanceOf[UserBattleResult].GetZone() == i)
          }


          var skillList = list.filter(job => job.getTime() >= last_inserted - check_interval && job.isInstanceOf[UserSkill])

          var skillA = skillList.count(job => job.asInstanceOf[UserSkill].skill.equals("A"))
          var skillB = skillList.count(job => job.asInstanceOf[UserSkill].skill.equals("B"))

          kind match {
            case 0 =>
              // 반복적인 움직임 패턴 감지
              sendTo ! Vectors(id, 0::moveEast::moveWest::moveSouth::moveNorth::Nil )
            case 1 =>
              // 반복적인 사냥 감지
              sendTo ! Vectors(id, 1::pveResult::skillA::skillB::Nil )
            case 2 =>
              //특정 지역에서 유저를 계속 죽이고 다니는 놈 감지
              sendTo ! Vectors(id, 2+:pvpResult)

         }
        last_vector_calculated = Platform.currentTime
      }
    }
    case a : Job => {
      last_inserted = Platform.currentTime;
      list = list.filterNot(job => job.getTime() < a.getTime() - maximum)
      list = a::list;
    }
    case _ => Logging(context.system, this).info("case _ in User Vector")
  }
}

