package image

import concepts.Rectangular

case class Pixel(x: Int, y: Int, rgb: RGB) {
  def this(x: Int, y: Int, r: Int, g: Int, b: Int) = this(x, y, RGB(r, g, b))
//  def this(x: Int, y: Int, rgb: Int) = this(x, y, RGB(rgb))

  def in(rect: Rectangular): Boolean = rect contains this

  def coords: (Int, Int) = (x, y)

}
