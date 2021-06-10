package image

import project.Selection

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import scala.swing.{Component, Graphics2D}

@SerialVersionUID(102L)
class Image (val path: String = "", bufferedImage: BufferedImage) extends Component with Serializable {
//  def this(image: Image) = this(image.img, null)
  def this(path: String) = this(path, null)
  def this(bufferedImage: BufferedImage) = this("", bufferedImage)
  def this(width: Int, height: Int, color: Color) = this(Operation.fill(color)(new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR), Array(new Selection(0, 0, width, height))).img)

  @transient lazy val img: BufferedImage = {
    if (bufferedImage == null) ImageIO.read(new File(path))
    else bufferedImage
  }

  def x: Int = img.getMinX
  def y: Int = img.getMinY
  def width: Int = img.getWidth
  def height: Int = img.getHeight

  override def paintComponent(g: Graphics2D): Unit = {
    super.paintComponent(g)
    g.drawImage(img, 0, 0, null)
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

  def export(outputFile: File, fileFormat: String): Unit = ImageIO.write(img, fileFormat, outputFile)

  override def toString: String = if (path != "") path.split("[\\\\/]").last else "Empty layer"
}

object Image {
  def load(path: String): BufferedImage = ImageIO.read(new File(path))

  def copy(bi: BufferedImage): BufferedImage = new BufferedImage(bi.getColorModel, bi.copyData(null), bi.getColorModel.isAlphaPremultiplied, null)
}