package project

import image.Image

import java.awt.Color

@SerialVersionUID(100L)
class Project private (val layers: Array[Layer], val imageWidth: Int, val imageHeight: Int) extends Serializable {
  def this(layers: Array[Layer]) = this(layers, layers(0).image.width, layers(0).image.height)
  def this(imageWidth: Int, imageHeight: Int) = this(Array(new Layer(new Image(imageWidth, imageHeight, Color.WHITE), 1.0, true)), imageWidth, imageHeight)

  def save(): Unit = ???

}

object Project {

  def createNew(): Project = ???
  def load(): Project = ???

}