package core

import akka.actor.{Actor, ActorRef}
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.Map
import scala.collection.immutable.Seq
import scala.math
import common._

class Subscriber extends Actor {
  var esper = context.actorSelection("akka.tcp://akka-esper@127.0.0.1:5150/user/EsperActor")
  var logger_web = context.actorSelection("akka.tcp://web@127.0.0.1:8999/user/LogActor")

  // heap memory test
  var buffer:ArrayBuffer[Array[Double]] = new ArrayBuffer[Array[Double]]()
  var cnt = 0

  // macro detection
  var prevVec:Map[Pair[String,Int], Seq[Int]] = Map[Pair[String,Int], Seq[Int]] ()
  var curVec:Map[Pair[String,Int], Seq[Int]] = Map[Pair[String,Int], Seq[Int]] ()

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

    case EsperEvent(_, MacroDetection(id, sort, avg, stddev)) => {
      val typeName = C.MacroType(sort)
      println(s"$typeName Detection($id) : avg = $avg, stddev = $stddev")
      val typeName = C.MacroType(sort)
	  logger_web ! MacroDetection(id, sort, avg, stddev)
    }

    case EsperEvent(_, AbusingDetection(user1, user2, count)) => {
      println(s"Abusing Detection : $user1 , $user2 $count 개")
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
                          select c.id, c.sort, avg(c.cosine), stddev(c.cosine)
                          from Macro.std:groupwin(id, sort).win:length(10) as c
                          group by id, sort
                          having count(*) = 10 and avg(cosine) > 0.85 and stddev(cosine) < 0.15
                          """ -> self

      packet.statement += s"""
                          insert into AbusingDetection
                          select c.user1, c.user2, count(*)
                          from Battle.std:groupwin(user1, user2).win:length(5) as c
                          group by user1, user2
                          having avg(duration) < 10 and stddev(duration) < 1
                          """ -> self

//      having avg(cosine) > 0.8 and stddev(cosine) < 0.2

      packet.eventTypes += "ChatWithAddress" -> classOf[ChatWithAddress]
      packet.eventTypes += "ChatAbusing" -> classOf[ChatAbusing]
      packet.eventTypes += "ChatSlang" -> classOf[ChatSlang]
      packet.eventTypes += "Macro" -> classOf[Macro]
      packet.eventTypes += "MacroDetection" -> classOf[MacroDetection]
      packet.eventTypes += "Battle" -> classOf[Battle]
      packet.eventTypes += "AbusingDetection" -> classOf[AbusingDetection]

      esper ! Request(packet)
    }

    case ChatLog(id, msg) => {
      println(s"($id)$msg")
    }

    case ChatWithAddress(id,msg,origin) => {
      esper ! ChatWithAddress(id,msg,self)
    }

    case Macro(id, sort, cosine) => {
      esper ! Macro(id, sort, cosine)
    }

    case Battle(user1, user2, winner, duration) => {
      esper ! Battle(user1, user2, winner, duration)
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
      Push(id, vec(0), vec.tail)
    }
  }

  def Push(id:String, sort:Int, vec:Seq[Int]) {
    curVec(Pair(id,sort)) = vec

    val isExist = prevVec.contains(id,sort)
    if(isExist)
    {
      val cosine = GetCosine(prevVec(id,sort), curVec(id,sort))
      println(id + " (" + C.MacroType(sort) + ") : prev(" + prevVec(id,sort).mkString(",") + "), cur(" + vec.mkString(",") + ") : cosine = " + cosine)
      self ! Macro(id, sort, cosine)
    }

    prevVec(Pair(id,sort)) = curVec(Pair(id,sort))
  }

  def GetCosine(vec1:Seq[Int], vec2:Seq[Int]):Double = {
    val size1:Double = Math.sqrt(vec1.map(i => i * i).foldLeft(0) (_ + _))
    val size2:Double = Math.sqrt(vec2.map(i => i * i).foldLeft(0) (_ + _))

    if(size1 * size2== 0)
      return 0

    var multipleSum:Double = 0
    for(i<- 0 until vec1.length)
      multipleSum += vec1(i) * vec2(i)

    multipleSum / (size1 * size2)
  }

}