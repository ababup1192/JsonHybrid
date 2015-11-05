package controllers

import org.ababup1192.parser.json.JsonParser
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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

  def sourceCode = Action.async { request =>
    request.body.asText.map { text =>
      Future {
        parser.input(text)
        parser.jsonAst.toString()
      }.map { ast =>
        Ok(ast)
      }
    }.getOrElse {
      Future {
        "ParseError"
      }.map {
        BadRequest(_)
      }
    }
  }


}
