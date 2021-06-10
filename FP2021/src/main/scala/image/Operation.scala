package image

import project.Selection

import java.awt.Color
import java.awt.image.BufferedImage
import scala.swing.Orientation

@SerialVersionUID(104L)
sealed abstract class Operation() extends Serializable {

  def sampleAndEvaluate(img: BufferedImage, x: Int, y: Int): RGB

  def apply(image: Image, on: Array[Selection]): Image = apply(image.img, on)

  def apply(img: BufferedImage, on: Array[Selection]): Image = {
    val modified = Image.copy(img)
    for (y <- 0 until modified.getHeight; x <- 0 until modified.getWidth if on.exists(_.contains(x, y))) {
      modified.setRGB(x, y, sampleAndEvaluate(img, x, y))
    }
    new Image(modified)
  }

}

sealed case class SimpleOperation(f: RGB => RGB) extends Operation {

  def andThen(that: SimpleOperation): SimpleOperation = SimpleOperation(this.f andThen that.f)

  override def sampleAndEvaluate(img: BufferedImage, x: Int, y: Int): RGB = f(img.getRGB(x, y))

}

sealed class FilterOperation(f: List[RGB] => RGB, n: Int, o: Orientation.Value) extends Operation {

  override def sampleAndEvaluate(img: BufferedImage, x: Int, y: Int): RGB = f(getNeighborRGBs(img, x, y))

  private def getNeighborRGBs(img: BufferedImage, ofX: Int, ofY: Int): List[RGB] = {
    if (o == Orientation.Horizontal) (for (x <- ofX - n until ofX + n if x >= 0 && x >= ofX - n && x < img.getWidth && x <= ofX + n) yield RGB.intToRGB(img.getRGB(x, ofY))).toList
    else (for (y <- ofY - n until ofY + n if y >= 0 && y >= ofY - n && y < img.getHeight && y <= ofY + n) yield RGB.intToRGB(img.getRGB(ofX, y))).toList
  }

}

sealed class OperationSequence(operations: List[Operation]) extends Operation {

  override def sampleAndEvaluate(img: BufferedImage, x: Int, y: Int): RGB = ???

  override def apply(image: Image, on: Array[Selection]): Image = operations.foldLeft(image)((img, op) => op.apply(img, on))

}

object Operation {
  implicit def funcToSimpleOperation(f: RGB => RGB): SimpleOperation = SimpleOperation(f)

  val id: SimpleOperation = (rgb: RGB) => rgb
  def fill(c: Color): SimpleOperation = (_: RGB) => RGB.intToRGB(c.getRGB)
  def add(value: Double): SimpleOperation = (rgb: RGB) => rgb + value
  def sub(value: Double): SimpleOperation = (rgb: RGB) => rgb - value
  def revsub(value: Double): SimpleOperation = (rgb: RGB) => RGB(value - rgb.r, value - rgb.g, value - rgb.b)
  def mul(value: Double): SimpleOperation = (rgb: RGB) => rgb * value
  def div(value: Double): SimpleOperation = (rgb: RGB) => RGB(rgb.r / value, rgb.g / value, rgb.b / value)
  def revdiv(value: Double): SimpleOperation = (rgb: RGB) => RGB(value / rgb.r, value / rgb.g, value / rgb.b)
  def pow(value: Double): SimpleOperation = (rgb: RGB) => RGB(math.pow(rgb.r, value), math.pow(rgb.g, value), math.pow(rgb.b, value))
  def log(): SimpleOperation = (rgb: RGB) => RGB(math.log(rgb.r), math.log(rgb.g), math.log(rgb.b))
  def abs(): SimpleOperation = (rgb: RGB) => RGB(math.abs(rgb.r), math.abs(rgb.g), math.abs(rgb.b))
  def min(value: Double): SimpleOperation =  (rgb: RGB) => RGB(math.min(value, rgb.r), math.min(value, rgb.g), math.min(value, rgb.b))
  def max(value: Double): SimpleOperation =  (rgb: RGB) => RGB(math.max(value, rgb.r), math.max(value, rgb.g), math.max(value, rgb.b))
  def inv(): SimpleOperation = (rgb: RGB) => RGB(1.0 - rgb.r, 1.0 - rgb.g, 1.0 -rgb.b)
  def grayscale(): SimpleOperation = (rgb: RGB) => {
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