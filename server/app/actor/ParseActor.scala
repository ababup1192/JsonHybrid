package actor

import akka.actor.{Actor, ActorRef, Props}
import org.ababup1192.parser.json.JsonParser
import play.api.libs.json._

object ParseActor {
  def props(out: ActorRef) = Props(new ParseActor(out))
}

class ParseActor(out: ActorRef) extends Actor {
  val parser = JsonParser()

  override def receive: Receive = {
    case jsonText: String =>
      if (!jsonText.isEmpty) {
        parser.input(jsonText)
        parser.drawingAst.foreach { ast =>
          out ! upickle.json.write(ast.toJson)
        }
      }
  }
}
