package image

import concepts.Rectangular
import project.Selection

import java.awt
import java.awt.Color
import java.awt.image.{BufferedImage, ImageObserver}
import java.io.File
import javax.imageio.ImageIO
import scala.swing.{Component, Graphics2D}

class Image(val path: String, val transparency: Int) extends Component with ImageObserver {
  def this() = this("", 100)

  val img = if (path != "") ImageIO.read(new File(path)) else new BufferedImage(1280, 800, BufferedImage.TYPE_INT_RGB)
  if (path == "") {
    perform(Operation.Fill(Color.WHITE), Array(new Selection(0, 0, 1280, 800)))
  }

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
      if (on.exists(_.contains(x, y))) op(img, x, y)
    }
  }

  def blend(that: Image, opacity: Double): Image = {
    val result = new Image(this.path, 0)
    for (y <- 0 until height;
         x <- 0 until width) {
      val rgb1: RGB = result.img.getRGB(x, y)
      val rgb2: RGB = {
        val rgb: RGB = that.img.getRGB(x, y)
        rgb * opacity
      }
      result.img.setRGB(x, y, rgb1 + rgb2)
    }
    result
  }

}