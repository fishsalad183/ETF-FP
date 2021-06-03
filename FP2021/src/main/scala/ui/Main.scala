package ui

import image.{Image, Operation}
import project.{Layer, Project, Selection}

import java.awt.Color
import scala.swing._

object Main extends SimpleSwingApplication {

//  val img1: Image = new Image
  val img2: Image = {
    val img = new Image
    img.perform(Operation.Fill(Color.BLACK), Array(new Selection(0, 0, 1280, 800)))
    img
  }
  val img3: Image = new Image("src/resource/image/wallhaven-4o7x1p.jpg", 100)
  val img4: Image = new Image("src/resource/image/wallhaven-43kz59.jpg", 100)
  val img5: Image = new Image("src/resource/image/59300cb43919fe0ee3614ddc.png", 100)
  var project = new Project(Array(new Layer(img3, 0.3, true), new Layer(img4, 1.0, true)))

  override def top: Frame = {
    val ui = new MainWindow(project)
    ui.visible = true
    ui
  }

}
