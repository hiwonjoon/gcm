package core;

import scala.collection.immutable.Seq;
/**
 * Created by wonjoon-g on 2014. 6. 4..
 */
case class InGameLocation(x:Int,y:Int);
case class InGameUser(id:String,isNpc:Boolean)
case class InGameReward(exp:Int,gold:Int);
trait Job {
  def getTime() : Long;
  def getId() : Seq[String]
}
case class UserChat(user: String, msg: String, time : Long ) extends Job
{
  def getTime() = { time; }
  def getId() = { user::Nil };
}
case class UserMove(user: String, from : InGameLocation, to: InGameLocation, time : Long ) extends Job
{
  def getDir():Int = {
    var dir = 0
    if(to.x > from.x) dir = 0
    if(to.x < from.x) dir = 1
    if(to.y < from.y) dir = 2
    if(to.y > from.y) dir = 3
    dir
  }

  def getTime() = { time; }
  def getId() = { user::Nil };
}
case class UserBattleStart(user : String, loc : InGameLocation, who : String, time : Long ) extends Job
{
  def getTime() = { time; }
  def getId() = { user::Nil };
}
case class UserSkill(user: String, skill : String, time : Long) extends Job
{
  def getTime() = { time; }
  def getId() = { user::Nil };
}
case class UserBattleResult(isDraw : Boolean, duration : Long, pos : InGameLocation, winner : InGameUser, loser : InGameUser, reward : InGameReward, time : Long) extends Job
{
  def GetZone():Int = {
    val i = pos.x / 400
    val j = pos.y / 400
    val zone = i + 4 * j
    zone
  }

  def getTime() = { time; }
  def getId() = {
    if( winner.isNpc == false && loser.isNpc == false )
      winner.id::loser.id::Nil
    else if( winner.isNpc == false )
      winner.id::Nil
    else if( loser.isNpc == false )
      loser.id::Nil
    else
      Nil
  };
}
case class UserPvpStart(user1 : String, user2 : String, time : Long) extends Job
{
  def getTime() = { time; }
  def getId() = {
    user1::user2::Nil;
  }
}
case class UserPvpEnd(user1 : String, user2 : String, winner : String, duration : Long, time : Long ) extends Job
{
  def getTime() = { time; }
  def getId() = {
    user1::user2::Nil;
  }
}

