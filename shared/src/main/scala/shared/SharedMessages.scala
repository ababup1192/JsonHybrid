package shared

import org.ababup1192.parser.Node


trait Api {
  // textToAst(text: String): Map[Int, Node] = {
  def textToAst(text: String): Int
}
