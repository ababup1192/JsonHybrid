package org.ababup1192.hybrid.json

import org.ababup1192.parser._

import scala.collection.mutable.ArrayBuffer

object Json {
  /**
    * Parse Json AST to AST (Scala case class Map)
    * @param jsonAST Json object AST
    * @return AST Map
    */
  def parse(jsonAST: Dynamic): Option[Map[Int, Node]] = {
    val upcJson = upickle.json.readJs(jsonAST)
    upcJson.value match {
      case nodeArray: ArrayBuffer[(String, upickle.Js.Value)] =>
        // Classify Node's type by `kind` value. And add Node to ast.
        Some(nodeArray.foldLeft(Map.empty[Int, Node]) {
          case (ast, (id, node)) =>
            node("kind").value match {
              case "object" =>
                ast + (id.toInt -> upickle.default.readJs[ObjectNode](upcJson(id)))
              case "entry" =>
                ast + (id.toInt -> upickle.default.readJs[EntryNode](upcJson(id)))
              case "array" =>
                ast + (id.toInt -> upickle.default.readJs[ArrayNode](upcJson(id)))
              case "string" =>
                ast + (id.toInt -> upickle.default.readJs[StringNode](upcJson(id)))
              case "number" =>
                ast + (id.toInt -> upickle.default.readJs[NumberNode](upcJson(id)))
              case "boolean" =>
                ast + (id.toInt -> upickle.default.readJs[BooleanNode](upcJson(id)))
              case "null" =>
                ast + (id.toInt -> upickle.default.readJs[NullNode](upcJson(id)))
              case _ =>
                ast
            }
          case (ast, _) => ast
        })
      case _ => None
    }
  }
}
