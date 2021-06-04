package project

import image.Image

@SerialVersionUID(101L)
class Layer(val image: Image, var opacity: Double, var active: Boolean) extends Serializable {

  def blend(that: Layer): Layer = {
    new Layer(this.image blend(that.image, opacity), 1.0, true)
  }

}
