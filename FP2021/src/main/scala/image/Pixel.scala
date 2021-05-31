package image

import concepts.Rectangular
import project.Selection

case class RGB(r: Int, g: Int, b: Int)

object RGB {
  implicit def intToRGB(rgb: Int): RGB = RGB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF)
  implicit def RGBToInt(rgb: RGB): Int = 0 | rgb.r << 16 | rgb.g << 8 | rgb.b
}

case class Pixel(x: Int, y: Int, rgb: RGB) {
  def this(x: Int, y: Int, r: Int, g: Int, b: Int) = this(x, y, RGB(r, g, b))
//  def this(x: Int, y: Int, rgb: Int) = this(x, y, RGB(rgb))

  def in(rect: Rectangular): Boolean = rect contains this

  def coords: (Int, Int) = (x, y)

}
