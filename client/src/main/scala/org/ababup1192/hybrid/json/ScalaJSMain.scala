package org.ababup1192.hybrid.json

import com.scalawarrior.scalajs.ace._
import fr.iscpif.scaladget.d3._
import org.ababup1192._
import org.scalajs.dom
import org.scalajs.dom.WebSocket
import org.scalajs.dom.raw._
import org.scalajs.jquery.{JQueryAjaxSettings, JQueryXHR, jQuery}
import rx._

import scala.scalajs.js

object ScalaJSMain extends js.JSApp {

  // val graph = new Graph
  var depth = 0
  var entryNum = 0
  val editor = ace.edit("editor")
  val isGraphChange = Var(false)

  def main(): Unit = {
    // graph.clear()
    editor.setTheme("ace/theme/idle_fingers")
    editor.getSession().setMode("ace/mode/javascript")

    val wsParser = new WebSocket(s"ws://${dom.document.location.host}/parse/json")

    wsParser.onopen = (event: Event) => {
      wsParser.send(editor.getValue())
    }

    wsParser.onmessage = (event: MessageEvent) => {
      val json = js.JSON.parse(event.data.toString)
      for {
        ast <- hybrid.json.Json.parse(json)
        rootNode <- hybrid.json.Json.convertLayoutTree(1, ast)
      } {
        /*
        val svg = d3.select("#graph")
          .append("svg")
          .attr("id", "workflow")
          .attr("width", "100%")
          .attr("height", "500px")
          .style("border-style", "solid")
        val treeLayout = d3.layout.tree()
          .size(js.Array(400, 400))
          .children((d: js.Any) =>
            js.JSON.parse(upickle.json.write(upickle.json.readJs(d).apply("children")))
          )
          */

        val treeLayout = new TreeLayout(ast)
        println(treeLayout.rootTree.getOrElse("No Tree"))

        // val rootNodeJson = js.JSON.parse(upickle.json.write(rootNode))
        // println(js.JSON.stringify(rootNodeJson))


        /*
        val nodes = treeLayout.nodes(
          literal(name = "aaaa", children = js.Array(literal(name = "bbbb"))).asInstanceOf[GraphNode])
        // , children = js.Array(literal(name = "2"))).asInstanceOf[GraphNode])
        val links = treeLayout.links(nodes)

        val node = svg.selectAll(".node")
          .data(nodes)
          .enter()
          .append("g")
          .attr("class", "node")
          .attr("transform", (d: GraphNode) => {
            s"translate(${d.y + 50}, ${d.x})"
          }
          )
        */
        // depth = 0
        // entryNum = 0
        // visit(1, ast)
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

    d3.select(dom.window)
      .on("keydown", (_: js.Any, _: Double) => {
        val DELETE_KEY = 68d
        d3.event.keyCode match {
          case DELETE_KEY =>
          /* graph.selected().foreach { id =>
             dom.ext.Ajax.delete(
               url = s"http://localhost:9000/sourcecode/$id"
             ).map { response =>
               js.JSON.parse(response.responseText)
             }.foreach { json =>
               isGraphChange() = true
               // Visit from Root
               depth = 0
               entryNum = 0
               org.ababup1192.hybrid.json.Json.parse(json).foreach { ast =>
                 visit(1, ast)
               }
             }
             graph.selected() = None
           }
           */
          case _ =>
        }
      })


    d3.select("#node_value")
      .on("mousedown", (_: js.Any, _: Double) ⇒ {
        // graph.selected() = None
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
              success = { (json: js.Dynamic, textStatus: js.JSStringOps, jqXHR: JQueryXHR) =>
                isGraphChange() = true
                // Visit from Root
                depth = 0
                entryNum = 0
                org.ababup1192.hybrid.json.Json.parse(json).foreach { ast =>
                  // visit(1, ast)
                }
              },
              error = { (jqXHR: JQueryXHR, textStatus: js.JSStringOps, errorThrow: js.JSStringOps) =>
                println("no")
              },
              contentType = "application/json",
              dataType = "json",
              `type` = "POST"
            ).asInstanceOf[JQueryAjaxSettings])
          case _ =>
        }
      })
  }

  /*
  def visit(id: Int, ast: Map[Int, org.ababup1192.parser.Node]): Unit = {
    /* if (id == 1) {
      graph.clear()
    }*/
    ast.get(id).foreach {
      case objectNode: ObjectNode =>
        depth += 1
        if (id == 1 && isGraphChange()) {
          editor.setValue(objectNode.code)
          isGraphChange() = false
        }
        objectNode.childrenId.foreach {
          id =>
            entryNum += 1
            visit(id, ast)
        }
        entryNum = 0
      case entryNode: EntryNode =>
        graph.addEntry(entryNode, depth, entryNum)
        entryNode.childrenId.foreach { id =>
          visit(id, ast)
        }
      case arrayNode: ArrayNode =>
        arrayNode.childrenId.foreach { id =>
          visit(id, ast)
        }
      case stringNode: StringNode =>
        graph.addString(stringNode, depth, entryNum)
      case numberNode: NumberNode =>
        graph.addNumber(numberNode, depth, entryNum)
      case booleanNode: BooleanNode =>
        graph.addBoolean(booleanNode, depth, entryNum)
      case nullNode: NullNode =>
        graph.addNull(nullNode, depth, entryNum)
      case _ =>
    }
  }
  */
}

