package image

import concepts.Rectangular
import project.Selection

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class Image(val path: String, val transparency: Int) {

  val bufferedImage = ImageIO.read(new File(path))
  def x: Int = bufferedImage.getMinX
  def y: Int = bufferedImage.getMinY
  def width: Int = bufferedImage.getWidth
  def height: Int = bufferedImage.getHeight

  def perform(op: Operation, on: Array[Selection]): Unit = {
    for (y <- 0 until height;
         x <- 0 until width) {
      if (on.exists(_.contains(x, y))) bufferedImage setRGB(x, y, op(bufferedImage.getRGB(x, y)))
    }
  }

}