package core

import akka.actor.Actor
import akka.event.Logging


import kr.co.shineware.nlp.komoran.core.MorphologyAnalyzer
import scala.collection.JavaConversions._

/**
 * Created by wonjoon-g on 2014. 4. 10..
 */
class TweetProcessor extends Actor {
  val analyzer = new MorphologyAnalyzer("./lib/datas/")
  val word_count = new scala.collection.mutable.HashMap[String,scala.collection.mutable.HashMap[String,Int]];
  var tweets_count = 0;

  def receive: Receive = {
    case tweet: Tweet =>
      val result = for(eojeol <- analyzer.analyze(tweet.text)) yield {
        val changed = for(word <- eojeol) yield {
          (word.getFirst(), word.getSecond())
        }
        changed
      }

      val flatten = result.flatten
      for((content,pos) <- flatten)
      {
        word_count.get(pos) match {
          case Some(value) => value.get(content) match {
            case Some(count) => value(content) = count + 1;
            case None => value(content) = 1;
          }
          case None => word_count(pos) = scala.collection.mutable.HashMap[String,Int](content -> 1);
        }
//        Logging(context.system,this).info( "%s/%s" format (content,pos) )
      }

      tweets_count += 1;
      //Logging(context.system,this).info(flatten.toString())
      //val text = tweet.text.toLowerCase
      //val log = Logging(context.system, this)
      //log.info( s"$text: tweet received. in TweetProcessor")
    case "stat" =>
      Logging(context.system,this).info("current tweets : %d" format tweets_count)
    case "finish" =>
      Logging(context.system,this).info("%s" format word_count("NNP").toSeq.sortBy(_._2).toString() )
    case _ =>
      val log = Logging(context.system, this)
      log.info(" case with _ in TweetProcessor")
  }
}
class RawViewProcessor extends Actor {
  var tweets_count = 0;

  def receive: Receive = {
    case tweet: Tweet =>
      val text = tweet.text.toLowerCase
      val log = Logging(context.system, this)
      log.info( s"$text")
    case "stat" =>
      Logging(context.system,this).info("current tweets : %d" format tweets_count)
    case _ =>
      val log = Logging(context.system, this)
      log.info(" case with _ in TweetProcessor")
  }
}
