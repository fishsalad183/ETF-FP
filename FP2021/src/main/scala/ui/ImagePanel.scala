package ui

import image.Image

import scala.swing.{BorderPanel, FlowPanel}
import scala.swing.BorderPanel.Position._

class ImagePanel(val image: Image) extends BorderPanel {
  add(image, BorderPanel.Position.Center)
}
