package ui

import scala.swing._

object Main extends SimpleSwingApplication {

  override def top: Frame = {
    val ui = new MainWindow()
    ui.visible = true
    ui
  }

}
