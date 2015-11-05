package org.ababup1192

import com.scalawarrior.scalajs.ace._
import fr.iscpif.scaladget.d3._
import org.scalajs.dom
import org.scalajs.jquery.{JQueryAjaxSettings, JQueryXHR, jQuery}
import rx._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js

object ScalaJSMain extends js.JSApp {

  val graph = new Graph
  var depth = 0
  var entryNum = 0
  val editor = ace.edit("editor")
  val isGraphChange = Var(false)

  def main(): Unit = {
    graph.clear()
    editor.setTheme("ace/theme/idle_fingers")
    editor.getSession().setMode("ace/mode/javascript")

    editor.getSession().on("change", (_: js.Any) => {
      val text = editor.getValue()
      if (!isGraphChange()) {
        dom.ext.Ajax.post(
          url = "http://localhost:9000/sourcecode",
          data = text
        ).map { response =>
          js.JSON.parse(response.responseText)
        }.foreach { json =>
          val ast = toAST(json)

          // Visit from Root
          depth = 0
          entryNum = 0
          visit(1, ast)
        }
      }
    })

    d3.select(dom.window)
      .on("keydown", (_: js.Any, _: Double) => {
        val DELETE_KEY = 8d
        d3.event.keyCode match {
          case DELETE_KEY =>
            graph.selected().foreach { id =>
              dom.ext.Ajax.delete(
                url = s"http://localhost:9000/sourcecode/$id"
              ).map { response =>
                js.JSON.parse(response.responseText)
              }.foreach { json =>
                val ast = toAST(json)
                isGraphChange() = true
                // Visit from Root
                depth = 0
                entryNum = 0
                visit(1, ast)
              }
              graph.selected() = None
            }
          case _ =>
        }
      })

    d3.select("#node_value")
      .on("keydown", (_: js.Any, _: Double) ⇒ {
        val RETURN_KEY = 13
        d3.event.keyCode match {
          case RETURN_KEY =>
            val id = jQuery("#node_id").value().toString
            val value = jQuery("#node_value").value().toString
            val kind = jQuery("#node_kind").value().toString

            val json = if (kind == "boolean" || kind == "number") {
              s"""{"id": $id, "value": $value, "kind": "$kind"}"""
            } else {
              s"""{"id": $id, "value": "$value", "kind": "$kind"}"""
            }

            val url = if (kind == "entry" || kind == "string") {
              "http://localhost:9000/sourcecode/operation/string"
            } else if (kind == "number") {
              "http://localhost:9000/sourcecode/operation/number"
            } else {
              "http://localhost:9000/sourcecode/operation/bool"
            }

            jQuery.ajax(js.Dynamic.literal(
              url = url,
              data = json,
              success = { (data: js.Dynamic, textStatus: js.JSStringOps, jqXHR: JQueryXHR) =>
                val ast = toAST(data)
                isGraphChange() = true
                // Visit from Root
                depth = 0
                entryNum = 0
                visit(1, ast)
              },
              error = { (jqXHR: JQueryXHR, textStatus: js.JSStringOps, errorThrow: js.JSStringOps) =>
                println("no")
              },
              contentType = "application/json",
              dataType = "json",
              `type` = "POST"
            ).asInstanceOf[JQueryAjaxSettings])


          /*
          val ast = toAST(json)

          // Visit from Root
          depth = 0
          entryNum = 0
          visit(1, ast)
          */

          case _ =>
        }
      })
  }

  def visit(id: Int, ast: Map[Int, js.Any]): Unit = {
    if (id == 1) {
      graph.clear()
    }
    ast.get(id).foreach {
      node =>
        getKind(node) match {
          case "object" =>
            depth += 1
            val objectNode = node.asInstanceOf[ObjectNode]
            if (id == 1 && isGraphChange()) {
              editor.setValue(objectNode.code)
              isGraphChange() = false
            }
            objectNode.childrenId.foreach {
              id =>
                entryNum += 1
                visit(id.toString().toInt, ast)
            }
            entryNum = 0
          case "entry" =>
            val entryNode = node.asInstanceOf[EntryNode]
            graph.addEntry(entryNode.id, entryNode.key, depth, entryNum)
            entryNode.childrenId.foreach {
              id =>
                visit(id.toString().toInt, ast)
            }
          case "array" =>
            val arrayNode = node.asInstanceOf[ArrayNode]
            arrayNode.childrenId.foreach {
              id =>
                visit(id.toString().toInt, ast)
            }
          case "string" =>
            val stringNode = node.asInstanceOf[StringNode]
            graph.addString(stringNode.id, stringNode.value, depth, entryNum)
          case "number" =>
            val numberNode = node.asInstanceOf[NumberNode]
            graph.addNumber(numberNode.id, numberNode.value, depth, entryNum)
          case "boolean" =>
            val booleanNode = node.asInstanceOf[BooleanNode]
            graph.addBoolean(booleanNode.id, booleanNode.value, depth, entryNum)
          case "null" =>
            val nullNode = node.asInstanceOf[NullNode]
            graph.addNull(nullNode.id, depth, entryNum)
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

