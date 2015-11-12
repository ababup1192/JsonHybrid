package org.ababup1192.hybrid.json

import japgolly.scalajs.react.vdom.svg.prefix_<^._
import japgolly.scalajs.react.{ReactComponentB, _}
import org.ababup1192.parser.drawing._
import paths.high.Tree

import scala.scalajs.js

object JsonTree {

  private def move(p: js.Array[Double]) = s"translate(${p(0)},${p(1)})"

  private def isLeaf(node: Node) = node.children.isEmpty

  val jsonTree = ReactComponentB[Node]("Json Tree")
    .render_P { rootNode =>

      val tree = Tree[Node](
        data = rootNode,
        children = _.children,
        width = 400,
        height = 400
      )

      val branches = tree.curves map { curve =>
        <.path(^.d := curve.connector.path.print)
      }
      val nodes = tree.nodes map { node =>
        node.item match {
          case _: ObjectNode =>
            <.g(^.transform := move(node.point),
              <.circle(^.r := 5, ^.cx := 0, ^.cy := 0)
            )
          case entryNode: EntryNode =>
            <.g(^.transform := move(node.point),
              <.circle(^.r := 5, ^.cx := 0, ^.cy := 0),
              <.text(
                ^.transform := (if (isLeaf(node.item)) "translate(10,0)" else "translate(-10,0)"),
                ^.textAnchor := (if (isLeaf(node.item)) "start" else "end"),
                entryNode.key
              )
            )
          case arrayNode: ArrayNode =>
            <.g(^.transform := move(node.point),
              <.circle(^.r := 5, ^.cx := 0, ^.cy := 0),
              <.text(
                ^.transform := (if (isLeaf(node.item)) "translate(10,0)" else "translate(-10,0)"),
                ^.textAnchor := (if (isLeaf(node.item)) "start" else "end"),
                arrayNode.kind
              )
            )
          case stringNode: StringNode =>
            <.g(^.transform := move(node.point),
              <.circle(^.r := 5, ^.cx := 0, ^.cy := 0),
              <.text(
                ^.transform := (if (isLeaf(node.item)) "translate(10,0)" else "translate(-10,0)"),
                ^.textAnchor := (if (isLeaf(node.item)) "start" else "end"),
                stringNode.value
              )
            )
          case numberNode: NumberNode =>
            <.g(^.transform := move(node.point),
              <.circle(^.r := 5, ^.cx := 0, ^.cy := 0),
              <.text(
                ^.transform := (if (isLeaf(node.item)) "translate(10,0)" else "translate(-10,0)"),
                ^.textAnchor := (if (isLeaf(node.item)) "start" else "end"),
                numberNode.value
              )
            )
          case booleanNode: BooleanNode =>
            <.g(^.transform := move(node.point),
              <.circle(^.r := 5, ^.cx := 0, ^.cy := 0),
              <.text(
                ^.transform := (if (isLeaf(node.item)) "translate(10,0)" else "translate(-10,0)"),
                ^.textAnchor := (if (isLeaf(node.item)) "start" else "end"),
                booleanNode.value.toString
              )
            )
          case nullNode: NullNode =>
            <.g(^.transform := move(node.point),
              <.circle(^.r := 5, ^.cx := 0, ^.cy := 0),
              <.text(
                ^.transform := (if (isLeaf(node.item)) "translate(10,0)" else "translate(-10,0)"),
                ^.textAnchor := (if (isLeaf(node.item)) "start" else "end"),
                "null"
              )
            )
        }

      }

      <.svg(^.width := 550, ^.height := 550,
        <.g(^.transform := "translate(10,20)",
          branches,
          nodes
        )
      )
    }.build


}
