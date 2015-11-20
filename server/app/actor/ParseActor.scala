package actor

import akka.actor.{Actor, ActorRef, Props}
import org.ababup1192.parser.json.JsonParser
import play.libs.Json

object ParseActor {
  def props(out: ActorRef) = Props(new ParseActor(out))
}

class ParseActor(out: ActorRef) extends Actor {
  val parser = JsonParser()

  override def receive: Receive = {
    case jsonText: String =>
      val json = Json.parse(jsonText)

      json.findPath("operation").asText() match {
        case "input" =>
          val inputText = json.findPath("text").asText()
          if (!inputText.isEmpty) {
            parser.input(inputText)
            parser.drawingAst.foreach { ast =>
              out ! upickle.json.write(ast.toJson)
            }
          }
        case "delete" =>
          val id = json.findPath("id").asInt()
          parser.controller.delete(id)
          parser.drawingAst.foreach { ast =>
            out ! upickle.json.write(ast.toJson)
          }
      }
  }
}
