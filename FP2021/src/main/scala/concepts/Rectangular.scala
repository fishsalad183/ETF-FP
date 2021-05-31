package concepts

import image.Pixel

trait Rectangular {
  def x: Int
  def y: Int
  def width: Int
  def height: Int

  def contains(xCoord: Int, yCoord: Int): Boolean = (x to x + width contains xCoord) && (y to y + height contains yCoord)
  def contains(p: Pixel): Boolean = contains(p.x, p.y)

}
