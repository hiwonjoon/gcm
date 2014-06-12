package core

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.{ActorSystem,Props}
import akka.event.Logging
import scala.collection.mutable.HashSet;
import scala.collection.mutable.HashMap;
import scala.collection.mutable.LinkedList;

import kr.co.shineware.nlp.komoran.core.MorphologyAnalyzer
import scala.collection.JavaConversions._

/**
 * Created by wonjoon-g on 2014. 4. 10..
 */
class TweetGatherer(wannabe : ActorRef) extends Actor
{
  val log = Logging(context.system,this);

  def receive: Receive = {
    case data : collection.mutable.Map[_,_] => {
      log.info("data got with TweetGatherer")
      if( wannabe == null )
      {
        log.info("aa");
        log.info(data.toString());
      }
      else {
        val typed = data.asInstanceOf[collection.mutable.Map[String,Int]];
        val tweet_list = new common.TweetList(typed.toSeq.sortBy(_._2));
        wannabe ! common.TweetListResponsePacket(tweet_list);
      }
    }
    case _ => {
      log.info("case _ with TweetGatherer")
    }
  }
}
class Gather(wannabe : ActorRef,gather_count : Int,result_size : Int) extends Actor {
  val gathered = new scala.collection.mutable.HashMap[Int,String]
  var recv_count = 0;
  def receive : Receive = {
    case data:scala.collection.immutable.IntMap[_] => {
      //context.system.log.info( data.toString() )
      val typed = data.asInstanceOf[collection.immutable.IntMap[String]]
      gathered ++= typed
      gathered.dropRight(result_size);
      recv_count += 1;
      context.system.log.info( recv_count.toString() )
      if( gather_count == recv_count )
        wannabe ! gathered;
    }
    case _ => {}
  }
}

case class AddWord(str : String,time : Long);
case class SplitTree();
case class GetTrends(from_time : Long, reciever : ActorRef);

class TweetIndexTree(father : ActorRef) extends Actor {
  val parent = father;
  val children : HashSet[ActorRef] = null;
  val words = new HashMap[String,ActorRef];

  def receive = {
    case AddWord(str,time) => {
      if( IsTerminal == false ) {
        children.slice(str.hashCode() % children.size,str.hashCode() % children.size).foreach(_ ! AddWord(str,time));
      }
      else
      {
        words.get(str) match {
          case Some(elem) => elem ! InsertTime(time)
          case _ => {
            val new_word = context.system.actorOf(Props(new WordElement(str)));
            new_word ! InsertTime(time);
            words += (str -> new_word);
          }
        }
      }
    }
    case GetTrends(from_time,receiver) => {
      if( IsTerminal == false )
      {
        val gatherer = context.system.actorOf(Props(new Gather(receiver,children.size,100)));
        children.foreach(_ ! GetTrends(from_time,gatherer));
      }
      else
      {
        val gatherer = context.system.actorOf(Props(new Gather(receiver,words.size,100)));
        words.foreach( x => x match { case (a,b) => b ! GetCount(from_time,gatherer)} )
      }
    }
    case SplitTree() => {
      context.system.log.info("TODO");
    }
  }
  private def IsRoot = {
    parent == null;
  }
  private def IsTerminal = {
    children == null;
  }
}
case class InsertTime(time : Long)
case class GetCount(from_time : Long, gatherer : ActorRef)

class WordElement(text_ : String) extends Actor {
  val text = text_;
  var time_occured:scala.collection.immutable.List[Long] = Nil;

  def receive = {
    case InsertTime(time) => {
      time_occured = time_occured:::List(time);
    }
    case GetCount(from_time,gatherer) => {
      //context.system.log.info( gatherer.toString() )
      val pair = (time_occured.count(l => (from_time <= l))->text);
      gatherer ! scala.collection.immutable.IntMap[String](pair);
    }
    case _ => {
      Logging(context.system,this).info("case with _ at {}",self.toString());
    }
  }
}

class TweetProcessor extends Actor {
  val analyzer = new MorphologyAnalyzer("./core/lib/datas/")
  val word_count = new scala.collection.mutable.HashMap[String,scala.collection.mutable.HashMap[String,Int]];
  var tweets_count = 0;
  val index_tree = context.system.actorOf(Props(new TweetIndexTree(null)));

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
        index_tree ! AddWord(content,System.nanoTime());

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
//      val text = tweet.text.toLowerCase
//      val log = Logging(context.system, this)
//      log.info( s"$text: tweet received. in TweetProcessor")
    case "stat" =>
      Logging(context.system,this).info("current tweets : %d" format tweets_count)
    case "finish" =>
      Logging(context.system,this).info("%s" format word_count("NNP").toSeq.sortBy(_._2).toString() )
    case ("List",receiver:ActorRef) => {
      val gatherer = context.system.actorOf(Props(new TweetGatherer(receiver)));
      gatherer ! word_count("NNP")
    }
    case "test" => {
      val actor = context.system.actorOf(Props(new TweetGatherer(null)));
      index_tree ! GetTrends(0, actor);
    }
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
