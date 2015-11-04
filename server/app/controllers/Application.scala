package controllers

import org.ababup1192.parser.json.JsonParser
import play.api.mvc._

object Application extends Controller {
  val parser = JsonParser()

  def index = Action {
    Ok(views.html.index())
  }

  def sourceCode = Action { request =>
    request.body.asText.map { text =>
      // JsonParser
      parser.input(text)
      Ok(parser.ast.toString())
    }.getOrElse {
      BadRequest("Barser Error")
    }
  }


}
