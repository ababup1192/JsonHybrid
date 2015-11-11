package controllers

import actor.ParseActor
import org.ababup1192.parser.json.JsonParser
import play.api.Play.current
import play.api.mvc._

object Application extends Controller {
  val parser = JsonParser()

  def index = Action {
    Ok(views.html.index())
  }

  def wsParseJson = WebSocket.acceptWithActor[String, String] { request => out =>
    ParseActor.props(out)
  }
}
