package org.ababup1192.hybrid.json

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.{EventListener, OnUnmount}
import japgolly.scalajs.react.vdom.svg.prefix_<^._
import org.ababup1192.parser.drawing._
import org.scalajs.dom.MouseEvent
import paths.high.{Tree, TreeCurve, TreeNode}

import scala.scalajs.js

object JsonTree {

  case class Props(content: TagMod, treeNode: TreeNode[Node]){
    def getNode = treeNode.item
  }

  private def move(p: js.Array[Double]) = s"translate(${p(0)},${p(1)})"

  private val Branch = ReactComponentB[TreeCurve[Node]]("Branch")
    .render_P(curve =>
      <.path(^.d := curve.connector.path.print)
    ).build

  private class NodeBackend($: BackendScope[Props, Unit]) extends OnUnmount {

    def nodeEnter(e: MouseEvent): Callback = Callback(println("Node enter"))

    def nodeClick(e: MouseEvent): Callback = Callback(println("Node Click", $.props.runNow().getNode))

    def render(P: Props) = {
      def isLeaf(node: Node) = node.children.isEmpty

      P match {
        case props@Props(content, treeNode) =>

          <.g(^.transform := move(treeNode.point),
            <.circle(^.r := 5, ^.cx := 0, ^.cy := 0),
            <.text(
              ^.transform := (if (isLeaf(props.getNode)) "translate(10,0)" else "translate(-10,0)"),
              ^.textAnchor := (if (isLeaf(props.getNode)) "start" else "end"),
              content
            )
          )
      }
    }
  }

  private val Node = ReactComponentB[Props]("Node")
    .renderBackend[NodeBackend]
    .configure(
      EventListener[MouseEvent].install("click", _.backend.nodeClick),
      EventListener[MouseEvent].install("mouseenter", _.backend.nodeEnter)
    )
    .build

  private val Nodes = ReactComponentB[Tree[Node]]("Tree Nodes")
    .render_P { treeNode =>

      val nodes = treeNode.nodes.map(node =>
        node.item match {
          case objectNode: ObjectNode =>
            Node.withKey(objectNode.id)(Props(EmptyTag, node))
          case entryNode: EntryNode =>
            Node.withKey(entryNode.id)(Props(entryNode.key, node))
          case arrayNode: ArrayNode =>
            Node.withKey(arrayNode.id)(Props(arrayNode.kind, node))
          case stringNode: StringNode =>
            Node.withKey(stringNode.id)(Props(stringNode.value, node))
          case numberNode: NumberNode =>
            Node.withKey(numberNode.id)(Props(numberNode.value, node))
          case booleanNode: BooleanNode =>
            Node.withKey(booleanNode.id)(Props(booleanNode.value.toString, node))
          case nullNode: NullNode =>
            Node.withKey(nullNode.id)(Props("null", node))
        }
      )
      <.g(vdom.prefix_<^.^.className := "nodes", nodes)
    }.build


  class JsonTreeBackEnd($: BackendScope[Node, Unit]) extends OnUnmount {

    def canvasClick(e: MouseEvent): Callback = Callback(println("Canvas Click"))

    def render(P: Node) = {
      val tree = Tree[Node](
        data = P,
        children = {
          case node: ArrayNode =>
            List.empty[Node]
          case node =>
            node.children
        },
        width = 400,
        height = 400
      )

      val Branches =
        <.g(vdom.prefix_<^.^.className := "paths",
          tree.curves map { curve =>
            Branch.withKey(curve.index)(curve)
          }
        )

      <.svg(^.width := 550, ^.height := 550,
        <.g(vdom.prefix_<^.^.className := "jsonTree", ^.transform := "translate(10,20)",
          Branches,
          Nodes(tree)
        )
      )
    }
  }

  val jsonTree = ReactComponentB[Node]("Json Tree")
    .renderBackend[JsonTreeBackEnd]
    .configure(
      EventListener[MouseEvent].install("click", _.backend.canvasClick)
    )
    .build


}
