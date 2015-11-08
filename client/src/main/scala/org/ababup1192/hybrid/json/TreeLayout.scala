package org.ababup1192.hybrid.json

import org.ababup1192.parser.Node

import scala.annotation.tailrec

trait DrawTree {
  val id: Int
  val value: Node
  var x: Double
  var y: Double
  val depth: Int
  val number: Int
  var thread: Option[DrawTree]
  var change: Double
  var shift: Double
  var mod: Double
  var ancestor: DrawTree

  def parent: Option[DrawTree]

  def children: List[DrawTree]

  def left(): Option[DrawTree]

  def right(): Option[DrawTree]

  def leftBrother: Option[DrawTree]

  def lMostSibling: Option[DrawTree]
}

class TreeLayout(val ast: Map[Int, Node]) {

  case class DrawTreeImpl(value: Node, parent: Option[DrawTree] = None,
                          depth: Int = 0, number: Int = 1, var ancestor: DrawTree = null) extends DrawTree {
    self: DrawTree =>
    val id = value.id
    var x = -1d
    var y = depth.toDouble
    ancestor = self

    lazy val children = value.childrenId.zipWithIndex.foldLeft(List.empty[DrawTree]) { (list, idWithIndex) =>
      val (id, index) = idWithIndex
      ast.get(id).map { child =>
        DrawTreeImpl(child, Some(this), depth + 1, index + 1) :: list
      }.getOrElse(list)
    }.reverse

    var thread: Option[DrawTree] = None
    var change = 0d
    var shift = 0d
    var mod = 0d

    def left(): Option[DrawTree] = {
      if (thread.isDefined) thread else children.headOption
    }

    def right(): Option[DrawTree] = {
      if (thread.isDefined) thread else children.lastOption
    }

    def leftBrother: Option[DrawTree] = {
      @tailrec
      def loop(target: Option[DrawTree] = None, siblings: List[DrawTree]): Option[DrawTree] = {
        siblings match {
          case head :: _ if head == this => target
          case head :: tail => loop(Some(head), tail)
        }
      }
      parent.map { pt =>
        loop(siblings = pt.children)
      }.getOrElse(None)
    }

    def lMostSibling: Option[DrawTree] = {
      parent.map { pt =>
        pt.children.headOption.flatMap(node => if (node != this) Some(node) else None)
      }.getOrElse(None)
    }

    override def toString: String = {
      s"DrawTree($id, ${value.kind}, $x, $y, $children)"
    }
  }

  val rootTree: Option[DrawTree] = {
    ast.get(1).map { root =>
      Some(DrawTreeImpl(root))
    }.getOrElse(None)
  }


  def buchheim(distance: Double = 1d): Option[DrawTree] = {
    rootTree.map { tree =>
      val dt = firstWalk(tree, distance)
      val min = secondWalk(dt)
      min.foreach { minInst =>
        if (minInst < 0d) {
          thirdWalk(dt, -minInst)
        }
      }
      normalize(tree, distance)
      Some(dt)
    }.getOrElse(None)
  }

  def normalize(v: DrawTree, distance: Double): Unit = {
    v.y *= distance.toDouble
    v.children.foreach { child =>
      normalize(child, distance)
    }
  }

  def firstWalk(v: DrawTree, distance: Double): DrawTree = {
    v.children match {
      case List() =>
        if (v.lMostSibling.isDefined) {
          v.leftBrother foreach { brother =>
            v.x = brother.x + distance
          }
        } else {
          v.x = 0d
        }
      case ell :: t =>
        val arr = v.children.last
        v.children.foldLeft(ell) { (da, w) =>
          firstWalk(w, distance)
          apportion(w, da, distance)
        }
        executeShifts(v)

        val midpoint = (ell.x + arr.x) / 2d
        v.leftBrother match {
          case Some(w) =>
            v.x = w.x + distance
            v.mod = v.x - midpoint
          case None =>
            v.x = midpoint
        }
    }
    v
  }

  def apportion(v: DrawTree, defaultAncestor: DrawTree, distance: Double): DrawTree = {
    v.leftBrother match {
      case Some(w) =>
        var vir = v
        var vor = v
        var vil = w
        var vol = v.lMostSibling
        var sir = v.mod
        var sor = v.mod
        var sil = vil.mod
        var sol = vol.map(_.mod).getOrElse(0d)

        @tailrec
        def loop(): Unit = {
          (vil.right(), vir.left()) match {
            case (Some(vilRight), Some(virLeft)) =>
              vil = vilRight
              vir = virLeft
              vol = vol.map(_.left()).getOrElse(None)
              vor = vor.right().getOrElse(vor)
              vor.ancestor = v
              val shift = (vil.x + sil) - (vir.x + sir) + distance
              if (shift > 0) {
                moveSubtree(ancestor(vil, v, defaultAncestor), v, shift)
                sir += shift
                sor += shift
              }
              sil += vil.mod
              sir += vir.mod
              sol += vol.map(_.mod).getOrElse(0d)
              sor += vor.mod
              loop()
            case (_, _) =>
          }
        }
        loop()

        if (vil.right().isDefined && vor.right().isEmpty) {
          vor.thread = vir.right()
          vor.mod += sil - sor
        } else {
          (vir.left(), vol, vol.map(_.left()).getOrElse(None)) match {
            case (Some(virLeft), Some(volInst), None) =>
              volInst.thread = vir.left()
              volInst.mod = sir - sol
            case _ =>
          }
        }
        v
      case None =>
        defaultAncestor
    }
  }

  def moveSubtree(wl: DrawTree, wr: DrawTree, shift: Double): Unit = {
    val subtrees = wr.number - wl.number
    wr.change -= shift / subtrees
    wr.shift += shift
    wl.change += shift / subtrees
    wr.x += shift
    wr.mod = shift
  }

  def executeShifts(v: DrawTree): Unit = {
    var shift = 0d
    var change = 0d
    v.children.reverse.foreach {
      w =>
        w.x += shift
        w.mod += shift
        change += w.change
        shift += w.shift + change
    }
  }

  def ancestor(vil: DrawTree, v: DrawTree, defaultAncestor: DrawTree): DrawTree = {
    v.parent.map {
      parent =>
        if (parent.children.contains(vil.ancestor)) {
          vil.ancestor
        } else {
          defaultAncestor
        }
    }.getOrElse(defaultAncestor)
  }

  def secondWalk(v: DrawTree, m: Double = 0d, depth: Int = 0, min: Option[Double] = None): Option[Double] = {
    v.x += m
    v.y = depth.toDouble

    val newMin = min match {
      case Some(minInst) =>
        if (v.x < minInst) Some(v.x) else min
      case None =>
        Some(v.x)
    }

    v.children.foldLeft(newMin) { (nextNewMin, child) =>
      secondWalk(child, m + v.mod, depth + 1, nextNewMin)
    }
  }

  def thirdWalk(tree: DrawTree, n: Double) {
    tree.x += n
    tree.children.foreach { child =>
      thirdWalk(child, n)
    }
  }

}


