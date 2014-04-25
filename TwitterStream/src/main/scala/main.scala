package my_util
import scala.util.Try
import scala.io.Source
import scala.Some
import akka.actor.{Props, ActorSystem, ActorRef, Actor}
import akka.io.IO
import spray.http._
import spray.json._
import spray.can.Http
import spray.httpx.unmarshalling.{MalformedContent, Unmarshaller, Deserialized}
import spray.client.pipelining._
import spray.http.HttpRequest
import scala.annotation.tailrec
import akka.event.Logging

trait TwitterAuthorization {
  def authorize: HttpRequest => HttpRequest
}

trait OAuthTwitterAuthorization extends TwitterAuthorization {
  import OAuth._
  val AccessToken = "208999107-FmFqwMku3QNzBp6gOn4RDV3z4ItAQKexohnpKTIS"
  val AccessSecret = "sHIhpJQ0BaG8tgyKDxLucVEutRpWg26ZMJd7165vUXVhU"
  val ConsumerKey = "KMsgTQjhswaVVW9HgpRIHmKIi"
  val ConsumerSecret = "Se4BcvlqjjMa8CwDD1TNOBxTk3r3LtmX22H1lNQvJJlC9NfEHf"
  val consumer = Consumer(ConsumerKey,ConsumerSecret)
  val token = Token(AccessToken,AccessSecret)
  
  val authorize : (HttpRequest) => HttpRequest = oAuthAuthorizer(consumer,token)

}

case class User(id:String, lang:String, follwersCount: Int)
case class Place(country:String, name:String) { 
  override lazy val toString = s"$name,$country"; 
}
case class Tweet(id:String,user:User,text:String,place:Option[Place])

trait TweetMarshaller {
  implicit object TweetUnmarshaller extends Unmarshaller[Tweet] {
    def mkUser(user:JsObject): Deserialized[User] = {
      (user.fields("id_str"), user.fields("lang"), user.fields("followers_count")) match {
        case (JsString(id), JsString(lang), JsNumber(followers)) => Right(User(id,lang,followers.toInt))
        case (JsString(id),_,_) => Right(User(id, "", 0))
        case _ => Left(MalformedContent("bad user"))
      }
    }
    def mkPlace(place:JsValue) : Deserialized[Option[Place]] = { place match { 
        case JsObject(fields) => 
          (fields.get("country"), fields.get("name")) match {
            case (Some(JsString(country)), Some(JsString(name))) => Right(Some(Place(country,name)))
            case _ => Left(MalformedContent("bad place"))
          }
        case JsNull => Right(None)
        case _ => Left(MalformedContent("bad tweet"))
      }
    }

    def apply(entity: HttpEntity): Deserialized[Tweet] = {
      Try {
        val json = JsonParser(entity.asString).asJsObject
        (json.fields.get("id_str"), json.fields.get("text"), json.fields.get("place"), json.fields.get("user")) match {
          case (Some(JsString(id)), Some(JsString(text)), Some(place), Some(user:JsObject)) => 
            val x = mkUser(user).fold(x => Left(x), { user => 
              mkPlace(place).fold(x => Left(x), { place => Right(Tweet(id,user,text,place))
              })
            })
            x
          case _ => Left(MalformedContent("bad tweet"))
        }
      }
    }.getOrElse(Left(MalformedContent("bad json")))
  }
}

object TweetStreamerActor {
  val twitterUri = Uri("https://stream.twitter.com/1.1/statuses/filter.json")
}
class TweetStreamActor(uri: Uri, processor: ActorRef) extends Actor with TweetMarshaller{
  this: TwitterAuthorization => 
    val io = IO(Http)(context.system)

    def receive: Receive = {
      case _ : String  => 
//        val body = HttpEntity(ContentType(MediaTypes.`application/x-www-form-urlencoded`), s"locations=126.014513,34.301586,129.266466,38.499926&language=ko-KR")
        val body = HttpEntity(ContentType(MediaTypes.`application/x-www-form-urlencoded`), s"track=twitter&language=ko")
        val rq = HttpRequest(HttpMethods.POST, uri = uri, entity = body) ~> authorize
        val log = Logging(context.system,this)
        log.info( rq.uri.toString )
        rq.headers map { header => log.info( header.toString ) } 
        log.info( rq.entity.asString )
        sendTo(io).withResponsesReceivedBy(self)(rq)
      case ChunkedResponseStart(_)=>
        //val log = Logging(context.system,this)
        //log.info( s"ChunkedResponseStart")
      case MessageChunk(entity,_) => 
        //val log = Logging(context.system,this)
        //log.info( s"MessageChunk")
        TweetUnmarshaller(entity).fold(_ => (), processor !)
      /*case HttpResponse(status,entity,headers,_) =>
        val log = Logging(context.system,this)
        log.info( s"http response $status")
        headers map { header => log.info( header.toString ) }
        log.info( entity.asString)*/
      case _ =>
        val log = Logging(context.system, this)
        log.info( "case with _")
    }
}

object Main extends App {
  val system = ActorSystem("tweetstreamprocessor")
  val processor = system.actorOf(Props(new TweetProcessor),"processor")
  val rawprocessor = system.actorOf(Props(new RawViewProcessor),"rawprocessor")
  @tailrec private def commandLoop() : Unit = {
    Console.readLine() match {
      case "quit" => processor ! "finish"; system.shutdown(); return
      case "stat" => processor ! "stat";
      case "normal" =>
        val stream = system.actorOf(Props(new TweetStreamActor(TweetStreamerActor.twitterUri,processor) with OAuthTwitterAuthorization),"streamgenerator")
        stream ! "start"
      case "raw" =>
        val stream = system.actorOf(Props(new TweetStreamActor(TweetStreamerActor.twitterUri,rawprocessor) with OAuthTwitterAuthorization),"streamgenerator")
        stream ! "start"
    }
    commandLoop()
  }
  commandLoop()
}
