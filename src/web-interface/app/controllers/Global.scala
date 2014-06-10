package controllers

import play.api._
import akka.actor.{Props,ActorSystem}
import scala.io.Source
import java.io
import java.io.PrintWriter
import play.api.Play.current

object Global extends GlobalSettings {

  val system = ActorSystem("web")
  val core_subscriber = system.actorOf(Props(classOf[CoreSubscriber]))
  val log_processor = system.actorOf(Props(new LogSubscriber),"LogActor")
  var forbiddenwords = scala.collection.mutable.LinkedHashSet[String]()

  override def onStart(app: Application) {
    Logger.info("Hello Started!")

    core_subscriber ! "Start"

    /* 키워드 파일 읽어오기 */
    var forbidden = Play.getFile("db/forbiddenWords.txt")

    if(!forbidden.exists())
    {
      val writer = new PrintWriter(forbidden)
      writer.close()
    }
    forbidden = Play.getFile("db/forbiddenWords.txt")
    Source.fromFile(forbidden).getLines().foreach { list => forbiddenwords.add(list) }

    /* 금지 키워드 코어로 전송 */
    core_subscriber ! common.ForbiddenWords(forbiddenwords.toArray)

  }
  override def onStop(app: Application) {
    Logger.info("Hello Ended!")
    core_subscriber ! "End"

    /* 키워드 파일 입력하기 */
    var words = "";
    forbiddenwords.map { list => words = words + list + "\n" ; Logger.info(list) }
    withFileWriter("db/forbiddenWords.txt", false) { fileWriter => fileWriter.write(words) }
  }

  def withFileWriter(name:String, append:Boolean)(f: (java.io.FileWriter) => Any) {
    val file = Play.getFile(name)
    val writer = new java.io.FileWriter(file,append)
    try {
      f(writer)
    }
    finally {
      writer.close()
    }
  }

  def sendForbiddenWordsToCore()
  {
    core_subscriber ! common.ForbiddenWords(forbiddenwords.toArray)
  }


}