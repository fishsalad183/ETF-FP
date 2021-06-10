package image

case class RGB(private var red: Double, private var green: Double, private var blue: Double, private val checkingComponentValidity: Boolean = true) {
  def this(rgb: (Int, Int, Int)) = this(rgb._1, rgb._2, rgb._3)

  if (checkingComponentValidity) {
    if (red < 0) red = 0 else if (red > 1.0) red = 1.0
    if (green < 0) green = 0 else if (green > 1.0) green = 1.0
    if (blue < 0) blue = 0 else if (blue > 1.0) blue = 1.0
  }

  def r: Double = red
  def g: Double = green
  def b: Double = blue

  def +(that: RGB) = RGB(this.red + that.red, this.green + that.green, this.blue + that.blue)
  def +(value: Double) = RGB(this.red + value, this.green + value, this.blue + value)
  def -(that: RGB) = RGB(this.red - that.red, this.green - that.green, this.blue - that.blue)
  def -(value: Double) = RGB(this.red - value, this.green - value, this.blue - value)
  def *(that: RGB) = RGB(this.red * that.red, this.green * that.green, this.blue * that.blue)
  def *(value: Double) = RGB(this.red * value, this.green * value, this.blue * value)
}

object RGB {
  implicit def intToRGB(rgb: Int): RGB = RGB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, (rgb >> 0) & 0xFF)
  implicit def RGBToInt(rgb: RGB): Int = ((rgb.red * 255.0).toInt << 16) | ((rgb.green * 255.0).toInt << 8) | ((rgb.blue * 255.0).toInt << 0)

  def apply(red: Int, green: Int, blue: Int) = new RGB(red / 255.0, green / 255.0, blue / 255.0)
}
