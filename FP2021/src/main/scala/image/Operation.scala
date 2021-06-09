package image

import project.Selection

import java.awt.Color
import java.awt.image.BufferedImage
import scala.swing.Orientation

@SerialVersionUID(104L)
class Operation(val f: RGB => RGB) extends Serializable {

  def andThen(that: Operation): Operation = {
    if (this ne Operation.id) new Operation(this.f andThen that.f)
    else that
  }

  def apply(img: BufferedImage, on: Array[Selection]): Unit = {
    val copy = Image.copy(img)
    for (y <- 0 until img.getHeight; x <- 0 until img.getWidth if on.exists(_.contains(x, y))) {
      copy.setRGB(x, y, f(img.getRGB(x, y)))
    }
    img.setData(copy.getData)
  }

}

class FilterOperation(g: List[RGB] => RGB, n: Int = 0, o: Orientation.Value = Orientation.Horizontal) extends Operation(null) {

  override def apply(img: BufferedImage, on: Array[Selection]): Unit = {
    val copy = Image.copy(img)
    for (y <- 0 until img.getHeight; x <- 0 until img.getWidth if on.exists(_.contains(x, y))) {
      val newRGB = g(getNeighborRGBs(img, x, y))
      copy.setRGB(x, y, newRGB)
    }
    img.setData(copy.getData)
  }

  private def getNeighborRGBs(img: BufferedImage, ofX: Int, ofY: Int): List[RGB] = {
    if (o == Orientation.Horizontal) (for (x <- ofX - n until ofX + n if x >= 0 && x >= ofX - n && x < img.getWidth && x <= ofX + n) yield RGB.intToRGB(img.getRGB(x, ofY))).toList
    else (for (y <- ofY - n until ofY + n if y >= 0 && y >= ofY - n && y < img.getHeight && y <= ofY + n) yield RGB.intToRGB(img.getRGB(ofX, y))).toList
  }

}

object Operation {
  implicit def funcToOperation(f: RGB => RGB): Operation = new Operation(f)

  val id: Operation = (rgb: RGB) => rgb
  def fill(c: Color): Operation = (_: RGB) => RGB.intToRGB(c.getRGB)
  def add(value: Double): Operation = (rgb: RGB) => rgb + value
  def sub(value: Double): Operation = (rgb: RGB) => rgb - value
  def revsub(value: Double): Operation = (rgb: RGB) => RGB(value - rgb.r, value - rgb.g, value - rgb.b)
  def mul(value: Double): Operation = (rgb: RGB) => rgb * value
  def div(value: Double): Operation = (rgb: RGB) => RGB(rgb.r / value, rgb.g / value, rgb.b / value)
  def revdiv(value: Double): Operation = (rgb: RGB) => RGB(value / rgb.r, value / rgb.g, value / rgb.b)
  def pow(value: Double): Operation = (rgb: RGB) => RGB(math.pow(rgb.r, value), math.pow(rgb.g, value), math.pow(rgb.b, value))
  def log(): Operation = (rgb: RGB) => RGB(math.log(rgb.r), math.log(rgb.g), math.log(rgb.b))
  def abs(): Operation = (rgb: RGB) => RGB(math.abs(rgb.r), math.abs(rgb.g), math.abs(rgb.b))
  def min(value: Double): Operation =  (rgb: RGB) => RGB(math.min(value, rgb.r), math.min(value, rgb.g), math.min(value, rgb.b))
  def max(value: Double): Operation =  (rgb: RGB) => RGB(math.max(value, rgb.r), math.max(value, rgb.g), math.max(value, rgb.b))
  def inv(): Operation = (rgb: RGB) => RGB(1.0 - rgb.r, 1.0 - rgb.g, 1.0 -rgb.b)
  def grayscale(): Operation = (rgb: RGB) => {
    val avg: Double = (rgb.r + rgb.g + rgb.b) / 3.0
    RGB(avg, avg, avg)
  }

  def median(n: Int, o: Orientation.Value): FilterOperation = {
    val calculateMedian = (rgbs: List[RGB]) => {
      def medianForColor(rgbToComponent: RGB => Double): Double = {
        val values = rgbs map rgbToComponent
        val (lower, upper) = values.sortWith(_<_).splitAt(values.size / 2)
        if (values.size % 2 == 0) (lower.last + upper.head) / 2.0 else upper.head
      }
      RGB(medianForColor((rgb: RGB) => rgb.r), medianForColor((rgb: RGB) => rgb.g), medianForColor((rgb: RGB) => rgb.b))
    }
    new FilterOperation(calculateMedian, n, o)
  }
}