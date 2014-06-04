package core


import akka.actor.{Props, ActorSystem, ActorRef, Actor}
import spray.httpx.unmarshalling.{MalformedContent, Unmarshaller, Deserialized}
import scala.annotation.tailrec


object Main extends App {
  val system = ActorSystem("core")
  val tweet_processor = system.actorOf(Props(new TweetProcessor),"tweetprocessor")
  val raw_processor = system.actorOf(Props(new RawViewProcessor),"rawprocessor")
  val web_processor = system.actorOf(Props(new WebActor),"WebActor")
  val esper_subscriber = system.actorOf(Props(classOf[Subscriber]))
  val chat_processor = system.actorOf(Props(new ChatFilter(1338)),"ChatFilter")
  var forbiddenWords = scala.collection.mutable.LinkedHashSet[String]()

  esper_subscriber ! "RequestDetection"

  //case str => subsccriber ! Chat("overload", str)

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
      case str:String =>
        if(str.contains("cos"))
        {
          val value:Double = str.replaceAll("cos", "").toDouble
          esper_subscriber ! common.Macro("overload", value)
        }
        else
          esper_subscriber ! common.ChatWithAddress("overload",str, null)
    }
    commandLoop()
  }
  commandLoop()
}
