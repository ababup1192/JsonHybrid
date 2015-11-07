package org.ababup1192.hybrid.json

import org.ababup1192.parser.Node

import scala.annotation.tailrec

class TreeLayout(val ast: Map[Int, Node]) {

  case class DrawTree(value: Node, tree: Option[DrawTree] = None, parent: Option[DrawTree] = None, var depth: Int = 0, var number: Int = 1) {
    self: DrawTree =>

    var x = -1
    var y = depth

    val children = value.childrenId.zipWithIndex.foldLeft(List.empty[DrawTree]) { (list, idWithIndex) =>
      val (id, index) = idWithIndex
      ast.get(id).map { child =>
        DrawTree(child, Some(this), Some(this), depth + 1, index + 1) :: list
      }.getOrElse(list)
    }.reverse


    var thread = None
    var ancestor = this
    var change = 0
    var shift = 0

    def leftBrother: Option[Node] = {
      @tailrec
      def loop(target: Option[Node] = None, siblings: Vector[Node]): Option[Node] = {
        siblings match {
          case head +: tail if head == this.value => target
          case head +: tail => loop(Some(head), tail)
        }
      }
      loop(siblings = value.siblingsWithSelf(ast))
    }

    def lMostSibling: Option[Node] = {
      value.siblingsWithSelf(ast).headOption.flatMap(node => if (node != this.value) Some(node) else None)
    }

    override def toString: String = {
      s"DrawTree(${value.id}, ${value.kind}, $depth, $number, $children)"
    }
  }

  val rootTree: Option[DrawTree] = {
    ast.get(1).map { root =>
      Some(DrawTree(root))
    }.getOrElse(None)
  }


}


