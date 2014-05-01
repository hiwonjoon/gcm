package core

import akka.actor.{Actor,ActorRef}
import akka.event.Logging
import common._
/**
 * Created by wonjoon-g on 2014. 5. 1..
 */
class WebActor extends Actor{

  var esper = context.actorSelection("akka.tcp://akka-esper@127.0.0.1:5150/user/EsperActor")
  val log = Logging(context.system,this)

  override def receive = {
    case common.TweetListRequest() => {
      log.info("Request Tweet from Web...")
      Main.tweet_processor ! ("List",sender)
    }
    case "Hello" => {
      log.info("Hello from Web!")
      sender ! "Hello"
    }
    case "Bye" => {
      log.info("Web subscriber leaving...ㅠㅠ")
    }

    case Chat(name, text) => {
      log.info(name + " : " + text)
      esper ! ChatWithAddress(name, text, sender)
      sender ! Chat(name,text)
    }
  }
}
