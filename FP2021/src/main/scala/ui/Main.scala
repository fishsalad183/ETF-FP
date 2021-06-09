package ui

import image.Image
import project.{Layer, Project}

import scala.collection.mutable.ArrayBuffer
import scala.swing._

object Main extends SimpleSwingApplication {

  def createTestProject(): Project = {
    val testImg1: Image = new Image("src/resource/image/wallpapersden.com_british-columbia-foggy-forest_800x600.jpg")
    val testImg2: Image = new Image("src/resource/image/wallpapersden.com_cave-lake_800x600.jpg")
    val testImg3: Image = new Image("src/resource/image/wallpapersden.com_lake-lucerne_800x600.jpg")
    new Project(ArrayBuffer(new Layer(testImg1, 0.3, false), new Layer(testImg2, 0.3), new Layer(testImg3, 0.5)))
  }

  override def top: Frame = {
    val ui = new MainWindow(createTestProject())
    ui.visible = true
    ui
  }

}
