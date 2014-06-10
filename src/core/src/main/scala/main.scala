package core


import akka.actor.{Props, ActorSystem, ActorRef, Actor}
import spray.httpx.unmarshalling.{MalformedContent, Unmarshaller, Deserialized}
import scala.annotation.tailrec
import scala.concurrent.duration._

import java.net.InetSocketAddress

case class Dummy(dummy:Array[Double])

object Main extends App {
  val system = ActorSystem("core")
  val tweet_processor = system.actorOf(Props(new TweetProcessor),"tweetprocessor")
  val raw_processor = system.actorOf(Props(new RawViewProcessor),"rawprocessor")
  val web_processor = system.actorOf(Props(new WebActor),"WebActor")
  val esper_subscriber = system.actorOf(Props(classOf[Subscriber]));
  val core_frontend = system.actorOf(Props(new Listener(1338)))
  var forbiddenWords = scala.collection.mutable.LinkedHashSet[String]()

  var cosine_flag = false

  esper_subscriber ! "RequestDetection"

  @tailrec private def commandLoop() : Unit = {
    Console.readLine() match {
      case "quit" => tweet_processor ! "finish"; system.shutdown(); return
      case "stat" => tweet_processor ! "stat";
      case "normal" =>
        val stream = system.actorOf(Props(new TweetStreamActor(TweetStreamerActor.twitterUri,tweet_processor) with OAuthTwitterAuthorization),"streamgenerator")
        stream ! "start"
      case "raw" =>
        val stream = system.actorOf(Props(new TweetStreamActor(TweetStreamerActor.twitterUri,raw_processor) with OAuthTwitterAuthorization),"streamgenerator")
        stream ! "start"
      case "memory_test" =>
        var data:Array[Double] = new Array[Double] (10000)
        esper_subscriber ! Dummy(data)
      case "cosine_test" =>
        cosine_flag = true
      case str:String =>
        if(cosine_flag && str.length > 0)
        {
          val value = str.toDouble
          esper_subscriber ! common.Macro("overload", value)
        }
        else
          esper_subscriber ! common.ChatWithAddress("overload",str, null)
    }
    commandLoop()
  }
  commandLoop()
}
