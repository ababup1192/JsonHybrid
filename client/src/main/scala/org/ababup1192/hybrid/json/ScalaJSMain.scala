package org.ababup1192.hybrid.json

import com.scalawarrior.scalajs.ace._
import japgolly.scalajs.react.ReactDOM
import org.ababup1192.parser.drawing.JsonVisitor
import org.scalajs.dom
import org.scalajs.dom.raw._
import org.scalajs.dom.{WebSocket, document}
import rx.core.Var

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal

object ScalaJSMain extends js.JSApp {
  val editor = ace.edit("editor")
  val isGraphChange = Var(true)

  def main(): Unit = {

    val treeModel = new JsonTreeModel
    // editor.setTheme("ace/theme/idle_fingers")
    editor.setTheme("ace/theme/clouds")
    editor.getSession().setMode("ace/mode/javascript")

    val wsParser = new WebSocket(getWebSocketUri)

    wsParser.onopen = (event: Event) => {
      val text = editor.getValue()
      val json = literal(operation = "input", text = text)
      wsParser.send(js.JSON.stringify(json))
    }

    wsParser.onmessage = (event: MessageEvent) => {
      println(isGraphChange())
      val rootNodeJson = upickle.json.readJs(js.JSON.parse(event.data.toString))
      JsonVisitor.parse(rootNodeJson).foreach { node =>
        if(isGraphChange()){
          editor.setValue(node.code)
          ReactDOM.render(JsonTree(node, treeModel), document.getElementById("canvas"))
          isGraphChange() = false
        }
      }
      // Delete function
      /*
      isGraphChange() = true
      val del = literal(operation = "delete", id = 10)
      wsParser.send(js.JSON.stringify(del))
      */
    }

    wsParser.onerror = (event: ErrorEvent) => {
      System.err.println(event.message)
    }

    editor.addEventListener("change", (_: js.Any) => {
      val text = editor.getValue()
      if (!text.isEmpty && !isGraphChange()) {
        isGraphChange() = true
        val json = literal(operation = "input", text = text)
        wsParser.send(js.JSON.stringify(json))
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

