package common

import scala.beans.BeanProperty
import scala.collection.immutable.Map
import scala.collection.mutable.ArrayBuffer

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
case class Macro(@BeanProperty id: String, @BeanProperty cosine: Double)
case class MacroDetection(@BeanProperty id: String, @BeanProperty avg: Double, @BeanProperty stddev: Double)

case class TweetListRequest()
case class TweetListResponsePacket(packet:TweetList)
class TweetList(tweets : Seq[(String,Int)]) extends Serializable {
  val Tweets = tweets
}
object C {
  val filterStr:Array[String] = Array("시발", "좆", "ㅅㅂ", "지랄", "새끼", "개새끼", "샹놈")
}
case class ForbiddenWords(arrayOfWords : Array[String])
