package controllers

import play.api.mvc.{Action, Controller}
import play.api.libs.json.Json
import play.api.{Logger, Routes}
import akka.actor.{Actor, ActorRef}
import akka.pattern.ask
import scala.concurrent.Future
import akka.util.Timeout
import scala.concurrent.duration._


case class TwitterWords(text: String, weight: Int)

object TwitterWordsImplicits {
  implicit def convert(as : (String,Int)) : TwitterWords = TwitterWords(as._1,as._2)
  implicit def convert[B, A <% B](l: Seq[(String,Int)]): Seq[TwitterWords] = l map { a => a: TwitterWords }
}

class GetResponse extends Actor {
  val remote = context.actorSelection("akka.tcp://core@127.0.0.1:5151/user/WebActor")

  var respondTo : ActorRef = self;
  override def receive = {
    case common.TweetListRequest() => {
      remote ! common.TweetListRequest()
      respondTo = sender;
    }
    case common.TweetListResponsePacket(packet) => {
      Logger.info("Tweet List Response Packet");
      //Logger.info(packet.Tweets.toString())
      respondTo ! packet.Tweets
    }
  }
}
object GetTwitterWords extends Controller {
  implicit val fooWrites = Json.writes[TwitterWords]
  val tweet_list = new String();
  val last_refreshed = 0;
  val getTweetsFromCoreActor = Global.system.actorOf(akka.actor.Props(classOf[GetResponse]))

  def getMessage = Action.async {

    implicit val timeout = Timeout(10 seconds);
    import play.api.libs.concurrent.Execution.Implicits.defaultContext

    val future : Future[Seq[(String,Int)]] = (getTweetsFromCoreActor ? common.TweetListRequest()).mapTo[Seq[(String,Int)]]

    future.map(seq => {
      Ok(Json.toJson(TwitterWordsImplicits.convert(seq)))
    });

    /* Message 전송 받으면 됩니다 */
    /* 예시 */
//    Ok(Json.toJson(
//      List(TwitterWords("purus",1), TwitterWords("dui",1), TwitterWords("vestibulum",2),
//      TwitterWords("Aenean", 2), TwitterWords("hello wordl", 3), TwitterWords("Habitant",3),
//      TwitterWords("Nam et", 4), TwitterWords("Leo", 4), TwitterWords("Sit", 1),
//      TwitterWords("Dolor", 9), TwitterWords("Ipsum", 10), TwitterWords("Lorem", 14))))
  }

  def javascriptRoutes = Action { implicit request =>
    Ok(Routes.javascriptRouter("jsRoutes")(routes.javascript.GetTwitterWords.getMessage)).as(JAVASCRIPT)
  }

}