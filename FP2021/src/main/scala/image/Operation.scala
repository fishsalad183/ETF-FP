package image

import java.awt.Color
import java.awt.image.BufferedImage

case class Operation(f: RGB => RGB) {

  def apply(rgb: RGB): RGB = f(rgb)
  def apply(p: Pixel): Pixel = Pixel(p.x, p.y, f(p.rgb))
  def apply(img: BufferedImage, x: Int, y: Int): BufferedImage = {
    img.setRGB(x, y, f(img.getRGB(x, y)))
    img
  }

  def andThen(that: Operation): Operation = this.f andThen that.f

}

object Operation {
  implicit def funcToOperation(f: RGB => RGB): Operation = Operation(f)

  def fill(c: Color): Operation = (rgb: RGB) => RGB.intToRGB(c.getRGB)
  def add(value: Int): Operation = (rgb: RGB) => rgb + value
  def sub(value: Int): Operation = (rgb: RGB) => rgb - value

  val operations: Map[String, (String, Nothing => Operation)] = Map(
    "fill" -> ("Color", fill _),
    "add" -> ("Int", add _),
    "sub" -> ("Int", sub _),
  )
}