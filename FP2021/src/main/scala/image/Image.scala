package image

import concepts.Rectangular
import project.Selection

import java.awt
import java.awt.Color
import java.awt.image.{BufferedImage, ImageObserver}
import java.io.File
import javax.imageio.ImageIO
import scala.swing.{Component, Graphics2D}

class Image private (val img: BufferedImage, val path: String = "") extends Component with ImageObserver {
  def this(path: String) = this(ImageIO.read(new File(path)), path)
  def this(width: Int, height: Int, color: Color) = {
    this(new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR), "Color layer")
    perform(Operation.Fill(color), Array(new Selection(0, 0, width, height)))
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
    for (y <- 0 until height;
         x <- 0 until width
         if y < that.height && x < that.width) {
      val rgb1: RGB = { // this/background
        val rgb: RGB = this.img.getRGB(x, y)
        rgb * (1 - opacity)
      }
      val rgb2: RGB = { // that/foreground
        val rgb: RGB = that.img.getRGB(x, y)
        rgb * opacity
      }
      this.img.setRGB(x, y, rgb1 + rgb2)
    }
    this
  }

}