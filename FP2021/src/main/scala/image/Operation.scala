package image

import javafx.geometry.Orientation
import project.Selection

import java.awt.Color
import java.awt.image.BufferedImage

case class Operation(f: RGB => RGB) {

  def apply(rgb: RGB): RGB = f(rgb)
  def apply(p: Pixel): Pixel = Pixel(p.x, p.y, f(p.rgb))
  def apply(img: BufferedImage, x: Int, y: Int): BufferedImage = {
    img.setRGB(x, y, f(img.getRGB(x, y)))
    img
  }
//  def apply(img: BufferedImage, on: Array[Selection]): BufferedImage = {
//    val copy = Image.copy(img)
//    for (y <- 0 until img.getHeight;
//         x <- 0 until img.getWidth
//         if on.exists(_.contains(x, y))) copy.setRGB(x, y) = f(img.getRGB(x, y))
//    copy
//  }

  def andThen(that: Operation): Operation = this.f andThen that.f

}

object Operation {
  implicit def funcToOperation(f: RGB => RGB): Operation = Operation(f)

  def id(): Operation = (rgb: RGB) => rgb
  def fill(c: Color): Operation = (rgb: RGB) => RGB.intToRGB(c.getRGB)
  def add(value: Double): Operation = (rgb: RGB) => rgb + value
  def sub(value: Double): Operation = (rgb: RGB) => rgb - value
  def pow(value: Double): Operation = (rgb: RGB) => RGB(math.pow(rgb.r, value), math.pow(rgb.g, value), math.pow(rgb.b, value))
  def inv(): Operation = (rgb: RGB) => RGB(1.0 - rgb.r, 1.0 - rgb.g, 1.0 -rgb.b)
  def grayscale(): Operation = (rgb: RGB) => {
    val avg: Double = (rgb.r + rgb.g + rgb.b) / 3.0
    RGB(avg, avg, avg)
  }
//  def median(orientation: Orientation, n: Int): Operation = (rgb: RGB) => {
//
//  }
}