package ui

import image.Image

import java.awt.Color
import scala.swing.{BorderPanel, BoxPanel, FlowPanel, Orientation}
import scala.swing.BorderPanel.Position._
import scala.swing.Swing.LineBorder

class ImagePanel(val mainWindow: MainWindow) extends BoxPanel(Orientation.NoOrientation) {
//  add(mainWindow.project.images(0).image, Center)
  def img = mainWindow.project.layers(0).image
  contents += img

  border = LineBorder(Color.BLACK, 1)
}
