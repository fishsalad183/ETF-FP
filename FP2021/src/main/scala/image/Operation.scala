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

}

object Operation {
  def Fill(c: Color) = Operation(RGB => c.getRGB)
  def Add(rgb: RGB) = Operation((curr: RGB) => curr + rgb)
}