package org.ababup1192.hybrid.json

import fr.iscpif.scaladget.d3._
import org.ababup1192.parser._
import org.scalajs.jquery.jQuery
import rx._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{literal => lit}

case class GraphNode(id: Int)

class Graph {
  val svg = d3.select("#graph")
    .append("svg")
    .attr("id", "workflow")
    .attr("width", "100%")
    .attr("height", "500px")
    .style("border-style", "solid")
  val graph = svg.append("g").classed("graph", true)
  var nodes = List.empty[GraphNode]
  val selected: Var[Option[Int]] = Var(None)

  def clear(): Unit = {
    graph.selectAll("*").remove()
    jQuery("#node_id").value("")
    jQuery("#node_value").value("")
    nodes = Nil
  }

  def addEntry(nodeInfo: EntryNode, depth: Int, num: Int): Unit = {
    val entryHeight = 60
    val entry = graph.append("g")
      .classed("entry", true)
      .attr("transform", (d: js.Any, i: Double) => {
        s"translate(${(depth - 1) * 100}, ${20 + entryHeight + (num - 1) * 100} )"
      })
      .style("cursor", "pointer")

    entry.on("mousedown", (_: js.Any, _: Double) => {
      jQuery("#node_id").value(nodeInfo.id.toString)
      jQuery("#node_value").value(nodeInfo.key)
      jQuery("#node_kind").value("entry")
      selected() = Some(nodeInfo.id)
    })

    entry.append("rect")
      .attr("width", 15 + nodeInfo.key.length * 10)
      .attr("height", entryHeight - 1)
      .attr("fill", "white")
      .attr("stroke", "rgb(0, 0, 0)")
      .attr("stroke-width", 3)

    entry.append("text")
      .attr("x", 10)
      .attr("y", entryHeight / 2)
      .attr("dy", ".35em")
      .text(nodeInfo.key)
  }

  def addString(nodeInfo: StringNode, depth: Int, num: Int): Unit = {
    val entryHeight = 60

    val stringNode = graph.append("text")
      .attr("x", 10)
      .attr("y", entryHeight / 2)
      .attr("dy", ".35em")
      .attr("transform", (d: js.Any, i: Double) => {
        s"translate(${(depth - 1) * 100 + 50}, ${20 + entryHeight + (num - 1) * 100} )"
      })
      .style("cursor", "pointer")
      .text(nodeInfo.value)

    stringNode.on("mousedown", (_: js.Any, _: Double) => {
      jQuery("#node_id").value(nodeInfo.id.toString)
      jQuery("#node_value").value(nodeInfo.value)
      jQuery("#node_kind").value("string")
      selected() = Some(nodeInfo.id)
    })

  }

  def addNumber(nodeInfo: NumberNode, depth: Int, num: Int): Unit = {
    val entryHeight = 60

    val numberNode = graph.append("text")
      .attr("x", 10)
      .attr("y", entryHeight / 2)
      .attr("dy", ".35em")
      .attr("transform", (d: js.Any, i: Double) => {
        s"translate(${(depth - 1) * 100 + 50}, ${20 + entryHeight + (num - 1) * 100} )"
      })
      .style("cursor", "pointer")
      .text(nodeInfo.value)

    numberNode.on("mousedown", (_: js.Any, _: Double) => {
      jQuery("#node_id").value(nodeInfo.id.toString)
      jQuery("#node_value").value(nodeInfo.value.toString)
      jQuery("#node_kind").value("number")
      selected() = Some(nodeInfo.id)
    })

  }

  def addBoolean(nodeInfo: BooleanNode, depth: Int, num: Int): Unit = {
    val entryHeight = 60

    val booleanNode = graph.append("text")
      .attr("x", 10)
      .attr("y", entryHeight / 2)
      .attr("dy", ".35em")
      .attr("transform", (d: js.Any, i: Double) => {
        s"translate(${(depth - 1) * 100 + 50}, ${20 + entryHeight + (num - 1) * 100} )"
      })
      .style("cursor", "pointer")
      .text(nodeInfo.value)

    booleanNode.on("mousedown", (_: js.Any, _: Double) => {
      jQuery("#node_id").value(nodeInfo.id.toString)
      jQuery("#node_value").value(nodeInfo.value.toString)
      jQuery("#node_kind").value("boolean")
      selected() = Some(nodeInfo.id)
    })

  }

  def addNull(nodeInfo: NullNode, depth: Int, num: Int): Unit = {
    val entryHeight = 60

    graph.append("text")
      .attr("x", 10)
      .attr("y", entryHeight / 2)
      .attr("dy", ".35em")
      .attr("transform", (d: js.Any, i: Double) => {
        s"translate(${(depth - 1) * 100 + 50}, ${20 + entryHeight + (num - 1) * 100} )"
      })
      .style("cursor", "pointer")
      .text("null")
  }
}

trait GraphElement <: EventStates {
  def literal: js.Dynamic
}

trait EventStates {
  val selected: Var[Boolean] = Var(false)
}

class Task(val id: String,
           val title: Var[String] = Var(""),
           val location: Var[(Double, Double)] = Var((0.0, 0.0))) extends GraphElement {
  def literal = lit("id" -> id, "title" -> title(), "x" -> location()._1, "y" -> location()._2)
}

