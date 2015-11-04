package controllers

import org.ababup1192.parser.json.JsonParser
import play.api.mvc._

/*
object AutowireServer extends autowire.Server[String, upickle.default.Reader, upickle.default.Writer] {
  def read[Result: upickle.default.Reader](p: String) = upickle.default.read[Result](p)

  def write[Result: upickle.default.Writer](r: Result) = upickle.default.write(r)
}
*/

object Application extends Controller {
  val parser = JsonParser()

  /*
  object ApiImpl extends shared.Api {
    // override def textToAst(text: String): Map[Int, Node] = {
    override def textToAst(text: String): Int = {
      parser.input(text)
      parser.ast
      1
    }
  }*/


  def index = Action {
    Ok(views.html.index())
  }

  def sourceCode = Action { request =>
    request.body.asText.map { text =>
      parser.input(text)
      Ok(parser.jsonAst.toString())
      // Ok(parser.jsonAst.toString)
    }.getOrElse {
      BadRequest("ParserError")
    }
  }


}
