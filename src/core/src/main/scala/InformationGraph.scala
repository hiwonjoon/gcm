package core;
import akka.actor._
import akka.event.Logging
import scala.collection.mutable
import scala.collection.Seq
import scala.collection.mutable.HashSet
import scala.compat.Platform
import akka.cluster.Cluster
import scala.Some

/**
 * Created by Nine on 2014-06-14.
 */
case class MakeNode(id:String,frontend:ActorRef)
case class MadeNode(id:String,node:ActorRef)
case class AddWeight(actor:ActorRef,weight:Double)
case class Suspicious(node:ActorRef)

case class GetId()
case class GetFriends(baseline:Double,sendTo:ActorRef)
case class Friends(friends:Seq[ActorRef])
case class Clustered(users:HashSet[String])

class UserNode(id:String,frontend:ActorRef) extends Actor {
  val adjecentNode = mutable.HashMap.empty[ActorRef,(Double,Long)]

  def receive = {
    case AddWeight(actor,weight) => {
      adjecentNode.get(actor) match {
        case Some((original_weight,old_time)) => {
          adjecentNode.remove(actor)
          val new_weight = decaying(original_weight,old_time,Platform.currentTime)+weight;
          adjecentNode.put(actor,(new_weight,Platform.currentTime))

          if(new_weight >= 1) {
            //println("suspicious")
            frontend ! Suspicious(self)
          }
          //println(id + " : " + adjecentNode )
        }
        case _ => {
          adjecentNode.put(actor,(weight,Platform.currentTime))
          //println(id + " : " + adjecentNode )
        }
      }
    }
    case GetFriends(baseline,sendTo) => {
      val friends = scala.collection.mutable.ArrayBuffer.empty[ActorRef]
      adjecentNode.foreach{ case (friend,(weight,time)) =>
        val new_weight = decaying(weight,time,Platform.currentTime)
        //println(id + " : " + new_weight)
        if( new_weight >= baseline )
          friends += friend
      }
      //println(id + " : " + friends)
      sendTo ! Friends(friends)
    }
    case GetId() => {
      sender ! id
    }
    case _ => Logging(context.system, this).info("case_ in UserNode")
  }

  def decaying(original_weight:Double,old_time:Long,current_time:Long) : Double = {
    val new_weight = original_weight / math.pow(2,((current_time - old_time).toDouble / 1000))
    new_weight
  }
}

class Clustering(baseline:Double,sendTo:ActorRef) extends Actor {
  val not_sent = new HashSet[ActorRef]
  val nodes = new HashSet[ActorRef]
  val result = new HashSet[String]

  def receive = {
    case Friends(friends) => {
      not_sent -= sender

      friends.foreach(friend => {
        if( friend != null && nodes.contains(friend) == false)
          not_sent += friend
        if( friend != null )
          nodes += friend
      })
      //println(not_sent)
      if( not_sent.size == 0 ) {
        nodes.foreach{ actor => actor ! GetId() }
      }
      else
      {
        not_sent.foreach{ actor => actor ! GetFriends(baseline,self)}
      }
    }
    case id:String => {
      result += id;
      if( result.size == nodes.size )
        sendTo ! Clustered(result)
    }
    case _ => Logging(context.system, this).info("Case _ in ClusterGropuing")
  }
}

class ClusteringResultGatherer(gather_count:Int,sendTo:ActorSelection) extends Actor {
  var gathered = 0
  val clusters = mutable.HashSet.empty[HashSet[String]]
  def receive = {
    case Clustered(result) => {
      gathered += 1
      clusters += result
      //println("Custered : " + gathered)
      if( gathered == gather_count ) {
        clusters.foreach(cluster => {
          println("Custered : " + cluster);
          if( cluster.size >= 2 ) {
            cluster.foreach(id =>
              sendTo ! common.MacroDetection(id, 3, 0, 0)
            )
          }
        })
      }
    }
    case _ => Logging(context.system,this).info("Case _ in ClusteringResultGatherer")
  }
}