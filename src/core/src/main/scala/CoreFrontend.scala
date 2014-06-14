package core;

import akka.actor._
import akka.io.{IO, Tcp}
import akka.util.ByteString
import java.net.InetSocketAddress
import spray.json.{DefaultJsonProtocol, RootJsonFormat, JsBoolean, JsString, JsNumber, JsObject, JsonParser, JsValue, DeserializationException}
import spray.json.pimpAny
import scala.Some
import scala.concurrent.duration._
import scala.collection.immutable.IndexedSeq
import scala.collection.mutable.Seq
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import akka.cluster.Cluster
import akka.routing.{Broadcast, FromConfig}
import akka.cluster.routing.{ClusterRouterSettings, AdaptiveLoadBalancingRouter, ClusterRouterConfig}

/**
 * Created by wonjoon-g on 2014. 6. 4..
 */

object JsonProtocol extends DefaultJsonProtocol{
  implicit val userchatFormat = jsonFormat3(UserChat);
  implicit val inGameLocationFormat = jsonFormat2(InGameLocation);
  implicit val inGameUserFormat = jsonFormat2(InGameUser);
  implicit val inGameRewardFormat = jsonFormat2(InGameReward);

  implicit object userSkillFormat extends RootJsonFormat[UserSkill] {
    def write(c: UserSkill) = JsObject()
    def read(value: JsValue) = {
      value.asJsObject.getFields("id", "skill", "time") match {
        case Seq(JsString(id), JsString(skill), JsNumber(time)) =>
          new UserSkill(id, skill, time.longValue())
        case _ => throw new DeserializationException("UserSkill expected")
      }
    }
  }

  implicit object userMoveFormat extends RootJsonFormat[UserMove] {
    def write(c: UserMove) = JsObject()
    def read(value: JsValue) = {
      value.asJsObject.getFields("id", "src", "dest", "time") match {
        case Seq(JsString(id), from : JsValue, to : JsValue, JsNumber(time)) =>
          new UserMove(id,from.convertTo[InGameLocation], to.convertTo[InGameLocation], time.longValue())
        case _ => throw new DeserializationException("UserMove expected")
      }
    }
  }
  implicit object userBattleResultFormat extends RootJsonFormat[UserBattleResult] {
    def write(c: UserBattleResult) = JsObject()
    def read(value: JsValue) = {
      value.asJsObject.getFields("isDraw", "duration", "pos", "winner", "loser", "reward", "time") match {
        case Seq( JsBoolean(isDraw), JsNumber(duration), pos : JsValue, winner : JsValue, loser : JsValue, reward : JsValue, JsNumber(time)) =>
          new UserBattleResult(isDraw,duration.longValue(),pos.convertTo[InGameLocation], winner.convertTo[InGameUser], loser.convertTo[InGameUser], reward.convertTo[InGameReward], time.longValue())
        case _ => throw new DeserializationException("UserMove expected")
      }
    }
  }
}

class PacketHandler(frontend:ActorRef) extends Actor {
  import Tcp._
  import JsonProtocol._

  var buf = ByteString()
  def receive = {
    case Received(data) => {
      buf ++= data
      buf = processPacket(buf, (jo) => {
        val parsed = for (msgType <- jo.fields.get("msgType"); body <- jo.fields.get("body")) yield (msgType, body)
        if (parsed.isDefined) {
          try {
            parsed match {
              case Some((JsString("chat"), body)) =>
                frontend !(sender, body.convertTo[UserChat])
              case Some((JsString("skill"), body)) =>
                frontend !(sender, body.convertTo[UserSkill])
              case Some((JsString("move"), body)) =>
                frontend !(sender, body.convertTo[UserMove])
              case Some((JsString("bbs"), body)) => ;
              case Some((JsString("battleResult"),body)) =>
                frontend !(sender, body.convertTo[UserBattleResult])
              case _ =>
                println("? in CoreFrontend.scala")
            }
          }
          catch {
            case _ => println("Parse Error. Json Object : " + jo.prettyPrint)
          }
        } else {
          println("??? in CoreFrontEnd.scala")
          // handle error
        }
      })
    }
    case PeerClosed => context stop self
  }

  /*

 {
  "msgType": "chat",
  "body": {
    "from": "John",
    "to": "Jane",
    "msg": "Hello, World wtf"
  }
}
   */

  def parseJson(buf: ByteString, index: Int): Option[(JsObject, Int)] = {
    try {
      val nowBuf = buf.drop(index)
      val str = nowBuf.utf8String
      val (jsonSize, jsonStart) = header findFirstMatchIn str match {
        case Some(m) => (m.group(1).toString.toInt, m.end)
        case None => (0, 0)
      }

      if (jsonStart > 0 && nowBuf.length >= jsonStart + jsonSize) {
        val jsonStr = nowBuf.drop(jsonStart).take(jsonSize).utf8String
        val json = JsonParser(jsonStr).asJsObject
        return Some(json, index + jsonStart + jsonSize)
      } else {
        return None
      }
    } catch {
      case _ => return None
    }
  }

  val header = "^(\\d+)\\r?\\n\\r?\\n".r
  def processPacket(buf: ByteString, handler: (JsObject) => Unit): ByteString = {
    var index = 0
    while (true)
    {
      val res = parseJson(buf, index)
      if (!res.isDefined)
        return buf.drop(index)

      handler(res.get._1)
      index = res.get._2
    }

    buf.drop(index)
  }
}

class Listener(port: Int, frontend: ActorRef) extends Actor {
  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress("0.0.0.0",port))

  def receive = {
    case Bound(localAddress) =>
      println("Bound : " + localAddress)
    case Connected(remote, _) =>
      println("Connected: " + remote)
      val handler = context.actorOf(Props(new PacketHandler(frontend)))
      sender ! Register(handler)
    case CommandFailed(_: Bind) =>
      println("Bind Error")
      context stop self
  }
}


class CoreFrontend(port : Int) extends Actor {
  import JsonProtocol._
  var backends = IndexedSeq.empty[ActorRef];
  val backendRouter = context.actorOf(Props.empty.withRouter(ClusterRouterConfig(
      AdaptiveLoadBalancingRouter(metricsSelector = akka.cluster.routing.HeapMetricsSelector),ClusterRouterSettings(totalInstances = 100, routeesPath="/user/backend",allowLocalRoutees = true, useRole=Some("backend")))),name="backend_router")

  var jobCounter = 0;
  var esper = context.actorSelection("akka.tcp://akka-esper@127.0.0.1:5150/user/EsperActor")

  val listener = context.system.actorOf(Props(new Listener(port,self)))
  var logger_web = context.actorSelection("akka.tcp://web@127.0.0.1:8999/user/LogActor");

  val system = context.system
  import system.dispatcher
  context.system.scheduler.schedule(10.seconds, 10.seconds) {
    (self ! GetVector(0,"",system.actorOf(Props(new Gatherer(scala.collection.mutable.ArraySeq(backends:_*),0,Main.esper_subscriber)))))
    (self ! GetVector(1,"",system.actorOf(Props(new Gatherer(scala.collection.mutable.ArraySeq(backends:_*),1,Main.esper_subscriber)))))
    (self ! GetVector(2,"",system.actorOf(Props(new Gatherer(scala.collection.mutable.ArraySeq(backends:_*),2,Main.esper_subscriber)))))
  }

  ///성열아 여기 수정하면되. 이거 없애고, 이 액터에서 receive 받아서, 아래 내용 실행한 다음에, null을 웹쪽으로 뿌리도록 해버리면 됨.
  //http://java.dzone.com/articles/real-time-charts-play 이건 차트 만드는거.

  context.system.scheduler.schedule(10.seconds, 10.seconds) {
    logger_web.resolveOne(5.seconds).foreach{
      ref => backends.foreach(actor => actor ! GetPerformance(ref))
    }
  }

  def receive = {
//    case (handler:ActorRef,job:Job) if backends.isEmpty =>
//      sender ! JobFailed("Service unavailable, try again later",job)
    case (handler:ActorRef,job:Job) =>
      job match {
        case chat:UserChat =>


      	var message = chat.msg
        Main.forbiddenWords.foreach{ words => message = message.replaceAll(words, getQuestionString(words.length()))}
        var newUserChat = UserChat(chat.user, message, chat.time)
        if(chat.msg != message) // 욕설 필터링 감지
          logger_web ! common.ChatLog(0,chat.user, chat.msg)
      	//에스퍼로 도배 감지
	      esper ! common.ChatWithAddress(chat.user, chat.msg, sender)

    	  import JsonProtocol._

      	  val json = JsObject(
          	"msgType" -> JsString("chat"),
        	"body" -> newUserChat.toJson
      	  )
          val body = ByteString(json.prettyPrint)
          val header = ByteString(s"${body.length}\r\n\r\n")
          handler ! Tcp.Write(header ++ body)
        case _ =>
          jobCounter += 1;
          backendRouter ! job
      }
    case a:GetVector => {
      //클러스터에 잇는 모든 친구들에게 데이터를 요청 후 받아서 처리.
      backendRouter ! Broadcast(a)
      //backends.foreach(backend => backend ! a)
    }
    case BackendRegistration if !backends.contains(sender) =>
      context watch sender
      backends = backends :+ sender
    case Terminated(a) =>
      backends = backends.filterNot(_ == a )
    
    case _ =>
      println("?? in Core Frontend")
  }

  def getQuestionString(n : Int) = {
    var questionMark = ""
    var i = 1
    for(i <- 1 to n)
    { questionMark = questionMark + "?" }
    questionMark
  }
}

object CoreFrontend {
  def main(args: Array[String]) : Unit = {
    val listener_port = if (args.isEmpty) "1338" else args(0);
    val frontend_port = if (args.isEmpty || args.length < 1 ) "0" else args(1);

    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$frontend_port").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [frontend]")).
      withFallback(ConfigFactory.load())
    val system = ActorSystem("core",config)
//    Cluster(system) registerOnMemberUp {
//      system.actorOf(Props(new CoreFrontend(listener_port.toInt)), name="frontend")
//    }
    val frontend = system.actorOf(Props(new CoreFrontend(listener_port.toInt)), name="frontend")

    ///println(frontend.toString())
  }

}