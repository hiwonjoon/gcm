package core

import akka.actor.{Actor, ActorRef}
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.Map
import scala.math
import common._

class Subscriber extends Actor {
  var esper = context.actorSelection("akka.tcp://akka-esper@127.0.0.1:5150/user/EsperActor")
  var logger_web = context.actorSelection("akka.tcp://web@127.0.0.1:8999/user/LogActor")

  // heap memory test
  var buffer:ArrayBuffer[Array[Double]] = new ArrayBuffer[Array[Double]]()
  var cnt = 0

  // macro detection
  var prevVec:Map[String, Array[Int]] = Map[String, Array[Int]] ()
  var curVec:Map[String, Array[Int]] = Map[String, Array[Int]] ()

  def receive = {
    case EsperEvent(_, ChatAbusing(id, origin)) => {
      if(origin != null)
        origin.asInstanceOf[ActorRef] ! ChatLog(id, " 님이 도배를 하고 있습니다.")
      logger_web ! ChatLog(id, " 님이 도배를 하고 있습니다.")
    }

    case EsperEvent(_, ChatSlang(id, message, origin)) => {
      if(origin != null)
        origin.asInstanceOf[ActorRef] ! ChatLog(id, " 님이 비속어를 사용했습니다.")
    }

    case EsperEvent(_, MacroDetection(id, avg, stddev)) => {
      println(s"Macro Detected($id) : $avg, $stddev")
    }

    case "RequestDetection" => {
      var packet = new RegisterPacket
      packet.packetType = "Chat"
      packet.statement += s"""
                          insert into ChatAbusing
                          select c.id, c.origin
                          from ChatWithAddress.win:time(1 sec) as c
                          group by id
                          having count(*) > 3
                         """ -> self

      packet.statement += s"""
                          insert into ChatSlang
                          select c.id, c.message, c.origin
                          from ChatWithAddress as c
                          where Filtering(c.message)
                          """ -> self

      packet.statement += s"""
                          insert into MacroDetection
                          select c.id, avg(c.cosine), stddev(c.cosine)
                          from Macro.win:time(10 sec) as c
                          group by id
                          """ -> self

//                        having avg(cosine) > 0.4 and avg(cosine) < 0.6 and stddev(cosine) < 0.2

      packet.eventTypes += "ChatWithAddress" -> classOf[ChatWithAddress]
      packet.eventTypes += "ChatAbusing" -> classOf[ChatAbusing]
      packet.eventTypes += "ChatSlang" -> classOf[ChatSlang]
      packet.eventTypes += "Macro" -> classOf[Macro]
      packet.eventTypes += "MacroDetection" -> classOf[MacroDetection]

      esper ! Request(packet)
    }

    case ChatLog(id, msg) => {
      println(s"($id)$msg")
    }

    case ChatWithAddress(id,msg,origin) => {
      esper ! ChatWithAddress(id,msg,self)
    }

    case Macro(id, cosine) => {
      esper ! Macro(id, cosine)
    }

    case EsperError(_) => {
      self ! "RequestDetection";
    }

    case Dummy(data) => {
      buffer += data
      cnt += 1
      println(cnt)

      var newData:Array[Double] = new Array[Double] (10000)
      self ! Dummy(newData)
	}

    case Vectors(id, vec) => {
      println(id + " : " + vec.mkString(" "))
      Push(id, vec)
    }
  }

  def Push(id:String, vec:Array[Int]) = {
    curVec(id) = vec

    val isExist = prevVec.contains(id)
    if(isExist)
    {
      val cosine = GetCosine(prevVec(id), curVec(id))
      self ! Macro(id, cosine)
    }

    prevVec(id) = curVec(id)
  }

  def GetCosine(vec1:Array[Int], vec2:Array[Int]):Double = {
    val size1:Double = Math.sqrt(vec1.map(i => i * i).foldLeft(0) (_ + _))
    val size2:Double = Math.sqrt(vec2.map(i => i * i).foldLeft(0) (_ + _))

    var multipleSum:Double = 0
    for(i<- 0 until vec1.length)
      multipleSum += vec1(i) * vec2(i)

    multipleSum / (size1 * size2)
  }

}