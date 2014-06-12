package core

import akka.actor.{Props, ActorSystem, ActorRef, Actor}
import spray.httpx.unmarshalling.{MalformedContent, Unmarshaller, Deserialized}
import scala.annotation.tailrec
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._

case class Dummy(dummy:Array[Double])
object Main extends App {
  val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=5151")
    .withFallback(ConfigFactory.load())

  val system = ActorSystem("core", config)

  val tweet_processor = system.actorOf(Props(new TweetProcessor),"tweetprocessor")
  val raw_processor = system.actorOf(Props(new RawViewProcessor),"rawprocessor")
  val web_processor = system.actorOf(Props(new WebActor),"WebActor")
  val esper_subscriber = system.actorOf(Props(classOf[Subscriber]),"espersubscriber");
  var forbiddenWords = scala.collection.mutable.LinkedHashSet[String]()

  var cosine_flag = false
  CoreFrontend.main(Seq("1338","5152").toArray) //FrontEnd는 Clustering 안함.
  CoreBackend.main(Seq("5153").toArray)
  //CoreBackend.main(Seq("0").toArray)


  //println(perf.getCpuInfo().toString());
  //println(perf.getMemoryInfo().toString());

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
      case "test" =>
        tweet_processor ! "test"
      case "memory_test" =>
        var data:Array[Double] = new Array[Double] (10000)
        esper_subscriber ! Dummy(data)
      case "cosine_test" =>
        cosine_flag = true
      case "battle" =>
        esper_subscriber ! common.Battle("a", "b", 5)
      case "battle2" =>
        esper_subscriber ! common.Battle("a", "b", 15)
      case "battle3" =>
        esper_subscriber ! common.Battle("b", "a", 5)
      case "battle4" =>
        esper_subscriber ! common.Battle("b", "a", 15)
      case str:String =>
        if(cosine_flag && str.length > 0)
        {
          val value = str.toDouble
          esper_subscriber ! common.Macro("overload", 0, value)
        }
        else
          esper_subscriber ! common.ChatWithAddress("overload",str, null)
    }
    commandLoop()
  }
  commandLoop()
}
