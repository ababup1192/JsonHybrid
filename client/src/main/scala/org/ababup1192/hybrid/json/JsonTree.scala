package org.ababup1192.hybrid.json

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.svg.prefix_<^._
import org.ababup1192.parser.drawing._
import paths.high.{Tree, TreeCurve, TreeNode}

import scala.scalajs.js

object JsonTree {

  private def move(p: js.Array[Double]) = s"translate(${p(0)},${p(1)})"

  private val Branch = ReactComponentB[TreeCurve[Node]]("Branch")
    .render_P(curve =>
      <.path(^.d := curve.connector.path.print)
    ).build

  private val Node = ReactComponentB[(TagMod, TreeNode[Node])]("Node")
    .render_P {
      case (content: TagMod, node: TreeNode[Node]) =>

        def isLeaf(node: Node) = node.children.isEmpty

        <.g(^.transform := move(node.point),
          <.circle(^.r := 5, ^.cx := 0, ^.cy := 0),
          <.text(
            ^.transform := (if (isLeaf(node.item)) "translate(10,0)" else "translate(-10,0)"),
            ^.textAnchor := (if (isLeaf(node.item)) "start" else "end"),
            content
          )
        )
    }.build

  private val Nodes = ReactComponentB[Tree[Node]]("Tree Nodes")
    .render_P { treeNode =>

      val nodes = treeNode.nodes.map(node =>
        node.item match {
          case objectNode: ObjectNode =>
            Node.withKey(objectNode.id)((EmptyTag, node))
          case entryNode: EntryNode =>
            Node.withKey(entryNode.id)((entryNode.key, node))
          case arrayNode: ArrayNode =>
            Node.withKey(arrayNode.id)((arrayNode.kind, node))
          case stringNode: StringNode =>
            Node.withKey(stringNode.id)((stringNode.value, node))
          case numberNode: NumberNode =>
            Node.withKey(numberNode.id)((numberNode.value, node))
          case booleanNode: BooleanNode =>
            Node.withKey(booleanNode.id)((booleanNode.value.toString, node))
          case nullNode: NullNode =>
            Node.withKey(nullNode.id)(("null", node))
        }
      )
      <.g(vdom.prefix_<^.^.className := "nodes", nodes)
    }.build

  val jsonTree = ReactComponentB[Node]("Json Tree")
    .render_P { rootNode =>

      val tree = Tree[Node](
        data = rootNode,
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
    }.build


}
