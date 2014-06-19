package controllers

import play.api._
import play.api.mvc._
import play.api.Play.current
import scala.io.Source
import java.io
import java.io.PrintWriter
import play.api.libs.json.Json

case class ForBiddenData(str : String)
object ForBiddenData{
  implicit val jsonFormat2 = Json.format[ForBiddenData]
}
case class ForBiddenKeywordTable(draw : Int, recordsTotal:Long , recordsFiltered:Long, data:Seq[ForBiddenData])
object ForBiddenKeywordTable extends ((Int, Long, Long, Seq[ForBiddenData]) => ForBiddenKeywordTable) {
  implicit val jsonFormat = Json.format[ForBiddenKeywordTable]
}

object ForbiddenKeyword extends Controller {

  def readFile = Action {
    Ok(views.html.forbiddenKeyword(Global.forbiddenwords.iterator))
  }
  def getAjax = Action { implicit request =>

      val draw = request.getQueryString("draw") match { case Some(s) => s.toInt case None => 0 }
      val start = request.getQueryString("start") match {case Some(s) => s.toInt case None => 0 }
      val length = request.getQueryString("length") match {case Some(s) => s.toInt case None => 0}
      val keyword = request.getQueryString("search[value]") match {case Some(s) => s case None => ""}

      Logger.info(draw + " " + start + " " + length + " " + keyword)
    Logger.info(request.getQueryString("draw") + " " + request.getQueryString("start") + " " + request.getQueryString("length") + " " + request.getQueryString("keyword"))

      val count = Global.forbiddenwords.size
      val lengthCount = if(start + length > count ) count - start else length
      val filtered = if(keyword != "") Global.forbiddenwords.filter(str => str.contains(keyword)) else Global.forbiddenwords
      Ok(Json.toJson(ForBiddenKeywordTable(draw, count, filtered.size, filtered.toSeq.slice(start, lengthCount).map( str => ForBiddenData(str)))))

  }

  def add(keyword : String) = Action
  {
    if(keyword == "")
    {
      Ok("추가될 단어를 입력하세요.")
    }
    else
    {
      if(Global.forbiddenwords.contains(keyword))
      {
        Ok("이미 추가된 단어입니다.")
      }
      else
      {
        Global.forbiddenwords.add(keyword)
        // Core 로 전송이 필요할듯..?
        Global.sendForbiddenWordsToCore()
        Ok("Success")
      }
    }

  }

  def delete(keyword : String) = Action
  {
    if(keyword == "")
    {
      Ok("삭제할 단어가 입력되지 않았습니다")
      // Core 로 전송이 필요할듯..?
    }
    else
    {
      if(Global.forbiddenwords.contains(keyword))
      {
        Global.forbiddenwords.remove(keyword)
        Global.sendForbiddenWordsToCore()
        Ok("Success")
      }
      else
      {
        Ok("이미 삭제되거나 없는 키워드 입니다.")
      }
    }
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
}
