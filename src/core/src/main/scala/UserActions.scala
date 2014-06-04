package core;
/**
 * Created by wonjoon-g on 2014. 6. 4..
 */
case class UserChat(user: String, msg: String, time : Long )
case class UserMove(user: String, from : (Int,Int), to: (Int,Int), time : Long )
case class UserBattleStart(user : String, loc : (Int,Int), who : String, time : Long )
case class UserSkill(user: String, skill : String, time : Long)
case class UserBattleEnd(user: String, time : Long)
case class UserPvpStart(user1 : String, user2 : String, time : Long)
case class UserPvpEnd(user1 : String, user2 : String, winner : String, duration : Long, time : Long )
