package image

import project.Selection

import java.awt
import java.awt.Color
import java.awt.image.{BufferedImage, ImageObserver}
import java.io.File
import javax.imageio.ImageIO
import scala.swing.{Component, Graphics2D}

@SerialVersionUID(102L)
class Image private(val path: String = "", private val w: Int = 0, private val h: Int = 0, private val color: Color = null) extends Component with ImageObserver with Serializable {
  @transient lazy val img: BufferedImage =
    if (path != "") ImageIO.read(new File(path))
    else Image.perform(Operation.Fill(color), new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR))

  def this(path: String) = this(path, 0, 0, null)

  def this(width: Int, height: Int, color: Color) = this("", width, height, color)

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

object Image {
  private def copy(bi: BufferedImage): BufferedImage = new BufferedImage(bi.getColorModel, bi.copyData(null), bi.getColorModel.isAlphaPremultiplied, null)

  private def perform(op: Operation, bi: BufferedImage): BufferedImage = {
    val modifiedImage = Image.copy(bi)
    for (y <- 0 until bi.getHeight; x <- 0 until bi.getWidth) op(modifiedImage, x, y)
    modifiedImage
  }

}