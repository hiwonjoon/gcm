package common

import scala.beans.BeanProperty
import scala.collection.immutable.Map
import scala.collection.mutable.ArrayBuffer
import java.util

case class Request(packet:RegisterPacket)
class RegisterPacket extends Serializable {
  var packetType = ""
  var statement = Map[String, AnyRef] ()
  var eventTypes = Map[String, Class[_ <: Any]]()
}

case class EsperEvent(eventType: String, underlying: AnyRef)

case class EsperError(error : String)
case class Chat(@BeanProperty id: String, @BeanProperty message: String)
case class ChatWithAddress(@BeanProperty id: String, @BeanProperty message: String, @BeanProperty origin:AnyRef)
case class ChatAbusing(@BeanProperty id: String, @BeanProperty origin:AnyRef)
case class ChatSlang(@BeanProperty id: String, @BeanProperty messsage: String, @BeanProperty origin:AnyRef)
case class ChatLog(@BeanProperty id: String, @BeanProperty log: String)

case class Macro(@BeanProperty id: String, @BeanProperty sort: Int, @BeanProperty cosine: Double)
case class Macro2(@BeanProperty id: String, @BeanProperty sort: Int, @BeanProperty cosine1: Double, @BeanProperty cosine2: Double)
case class MacroDetection(@BeanProperty id: String, @BeanProperty sort: Int, @BeanProperty avg: Double, @BeanProperty stddev: Double)

case class Battle(@BeanProperty winner: String, @BeanProperty loser: String, @BeanProperty duration: Long)
case class AbusingDetection(@BeanProperty user1: String, @BeanProperty user2: String, @BeanProperty count: Long)

case class TweetListRequest()
case class TweetListResponsePacket(packet:TweetList)
class TweetList(tweets : Seq[(String,Int)]) extends Serializable {
  val Tweets = tweets
}
object C {
  val filterStr:Array[String] = Array("시발", "좆", "ㅅㅂ", "지랄", "새끼", "개새끼", "샹놈")
  val MacroType = Array("Auto Bot(Move)", "Auto Bot(NPC Hunting)", "PvP Fraud")
}
case class ForbiddenWords(arrayOfWords : Array[String])
case class MachinePerformance(cpuperf:util.ArrayList[java.util.HashMap[String, String]],memperf:java.util.HashMap[String,java.lang.Long])