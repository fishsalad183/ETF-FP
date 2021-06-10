package project

import image.Image

import java.awt.Color

@SerialVersionUID(101L)
class Layer(val image: Image, var opacity: Double, var active: Boolean = true) extends Serializable {
  def this(imageWidth: Int, imageHeight: Int, color: Color) = this(new Image(imageWidth, imageHeight, color), 1.0)

  def blend(that: Layer): Layer = {
    new Layer(this.image blend(that.image, opacity), 1.0, true)
  }

}
