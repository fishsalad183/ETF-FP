package image

case class RGB(var r: Int, var g: Int, var b: Int) {
  def this(rgb: Int) = this((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF)
  def this() = this(0, 0, 0)

  if (r < 0) r = 0 else if (r > 255) r = 255
  if (g < 0) g = 0 else if (g > 255) g = 255
  if (b < 0) b = 0 else if (b > 255) b = 255

  def +(that: RGB) = RGB(this.r + that.r, this.g + that.g, this.b + that.b)
  def -(that: RGB) = RGB(this.r - that.r, this.g - that.g, this.b - that.b)
  def *(value: Double) = RGB((this.r * value).toInt, (this.g * value).toInt, (this.b * value).toInt)
}

object RGB {
  implicit def intToRGB(rgb: Int): RGB = RGB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF)
  implicit def RGBToInt(rgb: RGB): Int = 0 | rgb.r << 16 | rgb.g << 8 | rgb.b
}
