package org.ababup1192

import scala.scalajs.js

@js.native
class ObjectNode(val id: Int = js.native,
                 val kind: String = js.native,
                 val code: String = js.native,
                 val parentId: Int = js.native,
                 val childrenId: js.Array[js.JSNumberOps] = js.native
                ) extends js.Object

@js.native
class EntryNode(val id: Int = js.native,
                val kind: String = js.native,
                val code: String = js.native,
                val key: String = js.native,
                val parentId: Int = js.native,
                val childrenId: js.Array[js.JSNumberOps] = js.native
               ) extends js.Object

@js.native
class ArrayNode(val id: Int = js.native,
                val kind: String = js.native,
                val code: String = js.native,
                val parentId: Int = js.native,
                val childrenId: js.Array[js.JSNumberOps] = js.native
               ) extends js.Object

@js.native
class StringNode(val id: Int = js.native,
                 val kind: String = js.native,
                 val code: String = js.native,
                 val value: String = js.native,
                 val parentId: Int = js.native,
                 val childrenId: js.Array[js.JSNumberOps] = js.native
                ) extends js.Object


@js.native
class NumberNode(val id: Int = js.native,
                 val kind: String = js.native,
                 val code: String = js.native,
                 val value: Double = js.native,
                 val parentId: Int = js.native,
                 val childrenId: js.Array[js.JSNumberOps] = js.native
                ) extends js.Object

@js.native
class BooleanNode(val id: Int = js.native,
                  val kind: String = js.native,
                  val code: String = js.native,
                  val value: Boolean = js.native,
                  val parentId: Int = js.native,
                  val childrenId: js.Array[js.JSNumberOps] = js.native
                 ) extends js.Object

@js.native
class NullNode(val id: Int = js.native,
               val kind: String = js.native,
               val code: String = js.native,
               val parentId: Int = js.native,
               val childrenId: js.Array[js.JSNumberOps] = js.native
              ) extends js.Object

    