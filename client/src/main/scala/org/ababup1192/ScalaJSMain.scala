package org.ababup1192

import com.scalawarrior.scalajs.ace._
import org.scalajs.dom

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js

object ScalaJSMain extends js.JSApp {

  val graph = new Graph
  var depth = 0
  var entryNum = 0

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
        depth = 0
        entryNum = 0
        visit(1, ast)
      }
    })
  }

  def visit(id: Int, ast: Map[Int, js.Any]): Unit = {
    if (id == 1) {
      graph.clear()
    }
    ast.get(id).foreach { node =>
      getKind(node) match {
        case "object" =>
          depth += 1
          val objectNode = node.asInstanceOf[ObjectNode]
          objectNode.childrenId.foreach { id =>
            entryNum += 1
            visit(id.toString().toInt, ast)
          }
          entryNum = 0
        case "entry" =>
          val entryNode = node.asInstanceOf[EntryNode]
          graph.addEntry(entryNode.key, depth, entryNum)
          entryNode.childrenId.foreach { id =>
            visit(id.toString().toInt, ast)
          }
        case "array" =>
          val arrayNode = node.asInstanceOf[ArrayNode]
          arrayNode.childrenId.foreach { id =>
            visit(id.toString().toInt, ast)
          }
        case "string" =>
          val stringNode = node.asInstanceOf[StringNode]
          graph.addString(stringNode.value, depth, entryNum)
        case "number" =>
          val numberNode = node.asInstanceOf[NumberNode]
          graph.addNumber(numberNode.value, depth, entryNum)
        case kind =>
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

