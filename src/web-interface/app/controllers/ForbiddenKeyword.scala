package controllers

import play.api._
import play.api.mvc._
import play.api.Play.current
import scala.io.Source
import java.io
import java.io.PrintWriter

object ForbiddenKeyword extends Controller {

  def index = Action {
    Ok(views.html.twitterKeyword())

  }

  def readFile = Action {
    Ok(views.html.forbiddenKeyword(Global.forbiddenwords.iterator))
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
