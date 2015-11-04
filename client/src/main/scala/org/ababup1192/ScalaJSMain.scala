package org.ababup1192

import com.scalawarrior.scalajs.ace._

import scala.scalajs.js

object ScalaJSMain extends js.JSApp {

  def main(): Unit = {
    val editor = ace.edit("editor")
    editor.setTheme("ace/theme/idle_fingers")
    editor.getSession().setMode("ace/mode/javascript")

    editor.getSession().on("change", (_: js.Any) => {
      val text = editor.getValue()
      println(text)
    })

  }

}