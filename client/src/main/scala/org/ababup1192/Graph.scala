package org.ababup1192

import fr.iscpif.scaladget.d3._
import rx._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{literal => lit}

class Graph {
  val svg = d3.select("#graph")
    .append("svg")
    .attr("id", "workflow")
    .attr("width", "100%")
    .attr("height", "500px")
    .style("border-style", "solid")
  val graph = svg.append("g").classed("graph", true)

  def clear(): Unit = {
    graph.selectAll("*").remove()
  }

  def addEntry(key: String, depth: Int, num: Int): Unit = {
    println(key, depth, num)
    val entryHeight = 60
    val entry = graph.append("g")
      .classed("entry", true)
      .attr("transform", (d: js.Any, i: Double) => {
        s"translate(${depth * 10}, ${20 + entryHeight + (num - 1) * 100} )"
      })

    entry.append("rect")
      .attr("width", 15 + key.length * 10)
      .attr("height", entryHeight - 1)
      .attr("fill", "white")
      .attr("stroke", "rgb(0, 0, 0)")
      .attr("stroke-width", 3)

    entry.append("text")
      .attr("x", 10)
      .attr("y", entryHeight / 2)
      .attr("dy", ".35em")
      .text(key)
  }

  def addString(value: String, depth: Int, num: Int): Unit = {
    val entryHeight = 60

    graph.append("text")
      .attr("x", 10)
      .attr("y", entryHeight / 2)
      .attr("dy", ".35em")
      .attr("transform", (d: js.Any, i: Double) => {
        s"translate(${depth * 10 + 50}, ${20 + entryHeight + (num - 1) * 100} )"
      })
      .text(value)
  }


  def addNumber(value: Double, depth: Int, num: Int): Unit = {
    val entryHeight = 60

    graph.append("text")
      .attr("x", 10)
      .attr("y", entryHeight / 2)
      .attr("dy", ".35em")
      .attr("transform", (d: js.Any, i: Double) => {
        s"translate(${depth * 10 + 50}, ${20 + entryHeight + (num - 1) * 100} )"
      })
      .text(value)
  }

  /*val circleRoot = graph.append("g").classed("circleRoot", true)
  val tasks: Var[Array[Var[Task]]] = Var(Array())
  val dragging = Var(false)
  val mouseDownTask: Var[Option[Task]] = Var(None)
  val keyCodeV = Var(-1d)

  def hoge(): Unit = {

    val svgElement = js.Dynamic.global.document.getElementById("workflow")

    def mouseXY = d3.mouse(svgElement)


    d3.select(dom.window)
      .on("keydown", (_: js.Any, _: Double) => {
        keyCodeV() = d3.event.keyCode
      })

    d3.select(dom.window)
      .on("keyup", (_: js.Any, _: Double) => {
        keyCodeV() = -1d
      })


    def mouseMove(): Unit = {
      Seq(mouseDownTask()).flatten.foreach { t ⇒
        val xy = mouseXY
        val x = xy(0)
        val y = xy(1)
        dragging() = true
        t.location() = (x, y)
      }
    }

    def mouseUp(): Unit = {
      // Hide the drag line
      val xy = mouseXY
      if (!dragging() && keyCodeV() == 16) {
        val (x, y) = (xy(0), xy(1))
        val id = UUID.randomUUID()
        addTask(id.toString, id.toString, x, y)
      }
      mouseDownTask() = None
      dragging() = false
    }

    svg
      .on("mousemove", (_: js.Any, _: Double) => mouseMove())
      .on("mouseup.scene", (_: js.Any, _: Double) ⇒ mouseUp())
  }

  def addTask(id: String, title: String, x: Double, y: Double): Unit =
    addTask(new Task(id, Var(title), Var((x, y))))

  def addTask(task: Task): Unit = {
    tasks() = tasks() :+ Var(task)

    Obs(tasks) {
      val mysel = circleRoot.selectAll("g").data(tasks().toJSArray, (task: Var[Task], n: Double) => {
        task().id.toString
      })

      val newNode = mysel.enter().append("g")
      newNode.append("circle").attr("r", 25)

      Rx {
        newNode.classed("circle", true)
          .attr("transform", (task: Var[Task]) ⇒ {
            val loc = task().location()
            "translate(" + loc._1 + "," + loc._2 + ")"
          })
      }

      newNode.on("mousedown", (t: Var[Task], n: Double) ⇒ {
        mouseDownTask() = Some(t())
        d3.event.stopPropagation

      })
      mysel.exit().remove()
    }
  }*/
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

