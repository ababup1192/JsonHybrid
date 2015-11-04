package org.ababup1192

import com.scalawarrior.scalajs.ace._
import org.scalajs.dom
import org.scalajs.jquery.jQuery

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js


object ScalaJSMain extends js.JSApp {

  def main(): Unit = {
    val editor = ace.edit("editor")
    editor.setTheme("ace/theme/idle_fingers")
    editor.getSession().setMode("ace/mode/javascript")

    editor.getSession().on("change", (_: js.Any) => {
      val text = editor.getValue()
      dom.ext.Ajax.post(
        url = "http://localhost:9000/sourcecode",
        data = text
      ).map {
        _.responseText
      }.foreach { result =>
        jQuery("#graph").text(result)
      }
    })

  }
}