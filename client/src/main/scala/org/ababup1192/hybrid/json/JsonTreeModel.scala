package org.ababup1192.hybrid.json

import japgolly.scalajs.react.Callback
import japgolly.scalajs.react.extra.Broadcaster
import org.ababup1192.parser.drawing.Node

class JsonTreeModel extends Broadcaster[Option[Node]] {

  private object State {
    var node: Option[Node] = None

    def mod(newNode: Option[Node]): Callback = Callback(node = newNode) >> broadcast(newNode)
  }

  def selected(newNode: Node): Callback = State.mod(Some(newNode))

  def unselected(): Callback = State.mod(None)

}
