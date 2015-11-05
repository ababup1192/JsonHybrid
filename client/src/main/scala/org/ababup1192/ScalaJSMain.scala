package org.ababup1192

import com.scalawarrior.scalajs.ace._
import org.scalajs.dom

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js

object ScalaJSMain extends js.JSApp {

  def main(): Unit = {
    val editor = ace.edit("editor")
    editor.setTheme("ace/theme/idle_fingers")
    editor.getSession().setMode("ace/mode/javascript")

    editor.getSession().on("change", (_: js.Any) => {
      val text = editor.getValue()
      dom.ext.Ajax.post(
        url = "http://localhost:9000/sourcecode",
        data = text
      ).map { response =>
        println(response.responseText)
        js.JSON.parse(response.responseText)
      }.foreach { json =>
        val ast = toAST(json)

        // Visit from Root
        visit(1, ast)
      }
    })
  }

  def visit(id: Int, ast: Map[Int, js.Any]): Unit = {
    ast.get(id).foreach { node =>
      getKind(node) match {
        case "object" =>
          val objectNode = node.asInstanceOf[ObjectNode]
          objectNode.childrenId.foreach { id =>
            visit(id.toString().toInt, ast)
          }
        case "entry" =>
          val entryNode = node.asInstanceOf[EntryNode]
          entryNode.childrenId.foreach { id =>
            visit(id.toString().toInt, ast)
          }
        case "array" =>
          val arrayNode = node.asInstanceOf[ArrayNode]
          arrayNode.childrenId.foreach { id =>
            visit(id.toString().toInt, ast)
          }
        case "number" =>
          val numberNode = node.asInstanceOf[NumberNode]
          println(numberNode.value)
        case kind =>
          println(kind)
      }
    }
  }

  private def getKind(json: js.Any): String = {
    val node = json.asInstanceOf[js.Dictionary[js.Any]]
    node match {
      case node: js.Dictionary[js.Any] =>
        node.toMap.get("kind").map {
          case str: js.Any => str.toString
          case _ => "no"
        }.getOrElse("match error")
      case _ =>
        "match error"
    }
  }

  def toAST(json: Dynamic): Map[Int, js.Any] = {
    val obj = json.asInstanceOf[js.Dictionary[js.Any]]
    obj match {
      case obj: js.Dictionary[js.Any] =>
        obj.toMap.map {
          case (id: String, node: js.Any) =>
            (id.toInt, node)
        }
    }
  }

}

