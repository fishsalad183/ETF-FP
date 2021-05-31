package image

import concepts.Rectangular
import project.Selection

import java.awt
import java.awt.image.{BufferedImage, ImageObserver}
import java.io.File
import javax.imageio.ImageIO
import scala.swing.{Component, Graphics2D}

class Image(val path: String, val transparency: Int) extends Component with ImageObserver {

  val img = ImageIO.read(new File(path))
  def x: Int = img.getMinX
  def y: Int = img.getMinY
  def width: Int = img.getWidth
  def height: Int = img.getHeight

  override def paintComponent(g: Graphics2D): Unit = {
    super.paintComponent(g)
    g.drawImage(img, 0, 0, this)
  }

  override def imageUpdate(img: awt.Image, infoflags: Int, x: Int, y: Int, width: Int, height: Int): Boolean = {
    false
  }

  def perform(op: Operation, on: Array[Selection]): Unit = {
    for (y <- 0 until height;
         x <- 0 until width) {
      if (on.exists(_.contains(x, y))) img setRGB(x, y, op(img.getRGB(x, y)))
    }
  }

}