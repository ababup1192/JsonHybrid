package org.ababup1192.hybrid.json

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.vdom.prefix_<^.{^ => ^^}
import japgolly.scalajs.react.vdom.svg.prefix_<^._
import org.ababup1192.parser.drawing._
import org.scalajs.dom.raw.SVGCircleElement
import paths.high.{Tree, TreeCurve, TreeNode}

import scala.scalajs.js

object JsonTree {

  case class State(selected: Option[Node])

  case class TreeProps(rootNode: Node, model: JsonTreeModel, state: State)

  case class NodesProps(treeProps: TreeProps, tree: Tree[Node])

  case class NodeProps(treeProps: TreeProps, node: TreeNode[Node]) {
    def getNode = node.item
  }

  private def move(p: js.Array[Double]) = s"translate(${p(0)},${p(1)})"

  private val Branch = ReactComponentB[TreeCurve[Node]]("Branch")
    .render_P(curve =>
      <.path(^.d := curve.connector.path.print)
    ).build

  private class NodeBackend($: BackendScope[NodeProps, Unit]) extends OnUnmount {

    case class Callbacks(P: NodeProps) {
      val handleNodeMouseDown: ReactMouseEvent => Callback =
        e => P.treeProps.model.selected(P.node.item)
    }

    def render(props: NodeProps) = {
      def isLeaf(node: Node) = node.children.isEmpty

      implicit val r = Reusability.fn[NodeProps]((p1, p2) => p1 == p2)
      val cbs = Px.cbM($.props).map(Callbacks)
      val callbacks = cbs.value()

      def getContent(node: Node): TagMod = {
        node match {
          case objectNode: ObjectNode =>
            EmptyTag
          case entryNode: EntryNode =>
            entryNode.key
          case arrayNode: ArrayNode =>
            arrayNode.kind
          case stringNode: StringNode =>
            stringNode.value
          case numberNode: NumberNode =>
            numberNode.value
          case booleanNode: BooleanNode =>
            booleanNode.value.toString
          case nullNode: NullNode =>
            "null"
        }
      }

      val state = props.treeProps.state

      val circle = state.selected.filter(_.id == props.getNode.id)
        .map(_ => <.circle(^^.className := "selected", ^.r := 5, ^.cx := 0, ^.cy := 0))
        .getOrElse(<.circle(^.r := 5, ^.cx := 0, ^.cy := 0))

      props match {
        case NodeProps(treeProps, treeNode) =>
          <.g(
            ^.transform := move(treeNode.point),
            ^^.onMouseDown ==> callbacks.handleNodeMouseDown,
            circle,
            <.text(
              ^.transform := (if (isLeaf(props.getNode)) "translate(10,0)" else "translate(-10,0)"),
              ^.textAnchor := (if (isLeaf(props.getNode)) "start" else "end"),
              getContent(treeNode.item)
            )
          )
      }
    }
  }

  private val Node = ReactComponentB[NodeProps]("Node")
    .renderBackend[NodeBackend]
    .build

  private val Nodes = ReactComponentB[NodesProps]("Tree Nodes")
    .render_P {
      case NodesProps(treeProps, tree) =>
        val nodes = tree.nodes.map(node =>
          Node.withKey(node.item.id)(NodeProps(treeProps, node)))
        <.g(vdom.prefix_<^.^.className := "nodes", nodes)
    }.build


  class JsonTreeBackEnd($: BackendScope[TreeProps, State]) extends OnUnmount {

    case class Callbacks(P: TreeProps) {
      val handleCanvasMouseDown: ReactMouseEvent => Option[Callback] =
        e =>
          e.target match {
            case _: SVGCircleElement =>
              None
            case _ =>
              Some(P.model.unselected())
          }
    }

    def render(props: TreeProps, state: State) = {
      val tree = Tree[Node](
        data = props.rootNode,
        children = {
          /*case node: ArrayNode =>
            List.empty[Node]*/
          case node =>
            node.children
        },
        width = 400,
        height = 400
      )

      implicit val r = Reusability.fn[TreeProps]((p1, p2) => p1 == p2)
      val cbs = Px.cbM($.props).map(Callbacks)
      val callbacks = cbs.value()

      val Branches =
        <.g(vdom.prefix_<^.^.className := "paths",
          tree.curves map { curve =>
            Branch.withKey(curve.index)(curve)
          }
        )

      <.svg(^.width := 550, ^.height := 550,
        ^^.onMouseDown ==>? callbacks.handleCanvasMouseDown,
        <.g(vdom.prefix_<^.^.className := "jsonTree", ^.transform := "translate(10,20)",
          Branches,
          Nodes(NodesProps(props.copy(state = state), tree))
        )
      )
    }
  }

  val jsonTree = ReactComponentB[TreeProps]("Json Tree")
    .initialState(State(None))
    .renderBackend[JsonTreeBackEnd]
    .configure(
      Listenable.install(
        (p: TreeProps) => p.model,
        $ => (selected: Option[Node]) => $.modState(_.copy(selected = selected)))
    )
    .build


  def apply(rootNode: Node, treeModel: JsonTreeModel) = {
    JsonTree.jsonTree(TreeProps(rootNode, treeModel, State(None)))
  }
}
