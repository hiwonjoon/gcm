package core

import javax.crypto
import java.nio.charset.Charset
import spray.http.{HttpEntity, MediaTypes, ContentType, HttpRequest}
import spray.http.HttpHeaders.RawHeader
import org.parboiled.common.Base64
import scala.collection.immutable.TreeMap
import java.net.URLEncoder

//https://github.com/eigengo/activator-spray-twitter/blob/master/src/main/scala/core/OAuth.scala
object OAuth {
  case class Consumer(key: String, secret:String)
  case class Token(value: String, secret:String)

  def oAuthAuthorizer(consumer: Consumer, token: Token) : HttpRequest => HttpRequest = {
    val SHA1 = "HmacSHA1"
    val keyString = percentEncode(consumer.secret :: token.secret :: Nil)
    val key = new crypto.spec.SecretKeySpec(bytes(keyString), SHA1)
    val mac = crypto.Mac.getInstance(SHA1)

    { httpRequest : HttpRequest => 
      val timestamp = (System.currentTimeMillis / 1000).toString
      val nonce = System.nanoTime.toString
      val (requestParams, newEntity) = httpRequest.entity match {
        case HttpEntity.NonEmpty(ContentType(MediaTypes.`application/x-www-form-urlencoded`, _), data) =>
          val params = data.asString.split("&")
          val pairs = params.map { param => val p = param.split("="); p(0) -> percentEncode(p(1)) }
          //(pairs.toMap, HttpEntity(ContentType(MediaTypes.`application/x-www-form-urlencoded`), "%s=%s" format (pairs(0)._1,pairs(0)._2)))
          (pairs.toMap, HttpEntity(ContentType(MediaTypes.`application/x-www-form-urlencoded`), params mkString "&"))
        case e => (Map(), e)
      }
      val oauthParams = Map(
        "oauth_consumer_key" -> consumer.key,
        "oauth_signature_method" -> "HMAC-SHA1",
        "oauth_timestamp" -> timestamp,
        "oauth_nonce" -> nonce,
        "oauth_token" -> token.value,
        "oauth_version" -> "1.0"
      )
      
      val encodedOrderedParams = (TreeMap[String,String]() ++ oauthParams ++ requestParams) map { case(k,v) => k + "=" + v} mkString "&"
      val url = httpRequest.uri.toString()
      val signatureBaseString = percentEncode(httpRequest.method.toString()::url::encodedOrderedParams::Nil)
     println(signatureBaseString) 
      mac.init(key)
      val sig = Base64.rfc2045().encodeToString(mac.doFinal(bytes(signatureBaseString)), false)
      mac.reset()
      
      val oauth = TreeMap[String,String]() ++ (oauthParams + ("oauth_signature" -> percentEncode(sig))) map { case(k,v) => "%s=\"%s\"" format (k,v) } mkString ", "

      httpRequest.withHeaders(List(RawHeader("Authorization", "OAuth " + oauth))).withEntity(newEntity)
    }
  }

  private def percentEncode(str:String):String = URLEncoder.encode(str,"UTF-8") replace("+","%20") replace("%7E", "~")
  private def percentEncode(s: Seq[String]) : String = s map percentEncode mkString "&"
  private def bytes(str:String) = str.getBytes(Charset.forName("UTF-8"))
}
