package ui

import image.{Image, Operation}
import project.{Layer, Project, Selection}

import java.awt.Color
import scala.collection.mutable.ArrayBuffer
import scala.swing._

object Main extends SimpleSwingApplication {

//  val img1: Image = new Image
  val img2: Image = {
    val img = new Image(800, 600, Color.WHITE)
    img.perform(Operation.fill(Color.BLACK), Array(new Selection(0, 0, 1280, 800)))
    img
  }
  val img3: Image = new Image("src/resource/image/wallpapersden.com_british-columbia-foggy-forest_800x600.jpg")
  val img4: Image = new Image("src/resource/image/wallpapersden.com_cave-lake_800x600.jpg")
  val img5: Image = new Image("src/resource/image/wallpapersden.com_lake-lucerne_800x600.jpg")
  var project = new Project(ArrayBuffer(new Layer(img3, 0.3, false), new Layer(img4, 0.3, true), new Layer(img5, 0.5, true)))

  override def top: Frame = {
    val ui = new MainWindow(project)
    ui.visible = true
    ui
  }

}
