package controllers

import org.ababup1192.parser.json.JsonParser
import play.api.libs.json._
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

  case class StringValueNode(id: Int, value: String, kind: String)

  case class NumberNode(id: Int, value: Double, kind: String)

  case class BooleanNode(id: Int, value: Boolean, kind: String)

  def operationString = Action { request =>
    request.body.asJson.map { json =>


      implicit val stringNodeReads: Format[StringValueNode] = Json.format[StringValueNode]

      Json.fromJson(json).map {
        case StringValueNode(id, key, "entry") =>
          parser.controller.setKey(id, key)
        case StringValueNode(id, value, "string") =>
          parser.controller.setValue(id, value)
      }

      Ok(parser.jsonAst.toString())
    }.getOrElse {
      BadRequest("Not Json")
    }
  }

  def operationNumber = Action { request =>
    request.body.asJson.map { json =>
      implicit val numberNodeReads: Format[NumberNode] = Json.format[NumberNode]

      Json.fromJson(json).map {
        case NumberNode(id, value, _) =>
          parser.controller.setValue(id, value)
      }

      Ok(parser.jsonAst.toString())
    }.getOrElse {
      BadRequest("Not Json")
    }
  }

  def operationBool = Action { request =>
    request.body.asJson.map { json =>
      implicit val booleanNodeReads: Format[BooleanNode] = Json.format[BooleanNode]

      Json.fromJson(json).map {
        case BooleanNode(id, value, _) =>
          parser.controller.setValue(id, value)
      }

      Ok(parser.jsonAst.toString())
    }.getOrElse {
      BadRequest("Not Json")
    }
  }


}
