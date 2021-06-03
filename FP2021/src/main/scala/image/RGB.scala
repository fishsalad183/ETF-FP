package image

case class RGB(var r: Double, var g: Double, var b: Double) {
//  def this(red: Int, green: Int, blue: Int) = this(red / 255.0, green / 255.0, blue / 255.0)
//  def this(rgb: Int) = this((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF)

  if (r < 0) r = 0 else if (r > 1.0) r = 1.0
  if (g < 0) g = 0 else if (g > 1.0) g = 1.0
  if (b < 0) b = 0 else if (b > 1.0) b = 1.0

  def +(that: RGB) = RGB(this.r + that.r, this.g + that.g, this.b + that.b)
  def -(that: RGB) = RGB(this.r - that.r, this.g - that.g, this.b - that.b)
  def *(that: RGB) = RGB(this.r * that.r, this.g * that.g, this.b * that.g)
  def *(value: Double) = RGB(this.r * value, this.g * value, this.b * value)
}

object RGB {
  // BGR in BufferedImage implementation?
  implicit def intToRGB(rgb: Int): RGB = RGB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, (rgb >> 0) & 0xFF)
  implicit def RGBToInt(rgb: RGB): Int = ((rgb.r * 255.0).toInt << 16) | ((rgb.g * 255.0).toInt << 8) | ((rgb.b * 255.0).toInt << 0)

  def apply(red: Int, green: Int, blue: Int) = new RGB(red / 255.0, green / 255.0, blue / 255.0)
}
