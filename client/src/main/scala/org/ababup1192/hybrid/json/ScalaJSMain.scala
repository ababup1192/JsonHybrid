package org.ababup1192.hybrid.json

import com.scalawarrior.scalajs.ace._
import japgolly.scalajs.react.ReactDOM
import org.ababup1192.parser.drawing.JsonVisitor
import org.scalajs.dom
import org.scalajs.dom.raw._
import org.scalajs.dom.{WebSocket, document}
import rx._

import scala.scalajs.js

object ScalaJSMain extends js.JSApp {
  val editor = ace.edit("editor")
  val isGraphChange = Var(false)

  def main(): Unit = {

    // editor.setTheme("ace/theme/idle_fingers")
    editor.setTheme("ace/theme/clouds")
    editor.getSession().setMode("ace/mode/javascript")

    val wsParser = new WebSocket(getWebSocketUri)

    wsParser.onopen = (event: Event) => {
      wsParser.send(editor.getValue())
    }

    wsParser.onmessage = (event: MessageEvent) => {
      val rootNodeJson = upickle.json.readJs(js.JSON.parse(event.data.toString))
      JsonVisitor.parse(rootNodeJson).foreach { node =>
        ReactDOM.render(JsonTree(node), document.getElementById("canvas"))
      }
    }

    wsParser.onerror = (event: ErrorEvent) => {
      System.err.println(event.message)
    }

    editor.addEventListener("change", (_: js.Any) => {
      val text = editor.getValue()
      if (!isGraphChange() && !text.isEmpty) {
        wsParser.send(text)
      }
    })
  }

  def getWebSocketUri: String = {
    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"

    s"$wsProtocol://${
      dom.document.location.host
    }/parse/json"
  }
}

