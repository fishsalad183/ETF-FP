package project

import image.Image

class Layer(val image: Image, var opacity: Double, var active: Boolean) {

  def blend(that: Layer): Layer = {
    new Layer(this.image blend(that.image, opacity), 1.0, true)
  }

}
