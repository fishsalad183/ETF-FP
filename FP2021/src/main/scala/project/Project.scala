package project

import image.Image

import java.awt.Color
import java.io._
import scala.collection.mutable.ArrayBuffer

@SerialVersionUID(100L)
class Project private (var layers: ArrayBuffer[Layer], val imageWidth: Int, val imageHeight: Int) extends Serializable {
  var selections: ArrayBuffer[Selection] = ArrayBuffer(new Selection(0, 0, imageWidth, imageHeight))
  var currentLayer: Int = 0
  @transient var resultingImage: Image = null

  def this(layers: ArrayBuffer[Layer]) = this(layers, layers(0).image.width, layers(0).image.height)
  def this(imageWidth: Int, imageHeight: Int) = this(ArrayBuffer(new Layer(new Image(imageWidth, imageHeight, Color.WHITE), 1.0, true)), imageWidth, imageHeight)

  def save(file: File): Unit = {
    val oos = new ObjectOutputStream(new FileOutputStream(file))
    oos.writeObject(this)
    oos.close()
  }

  def createNewLayer(img: Image): Unit = {
    layers += new Layer(img, 1.0, true)
    currentLayer = layers.length - 1
  }

  def deleteCurrentLayer(): Unit = {
    layers.remove(currentLayer)
    currentLayer = 0
  }

}

object Project {

  def load(file: File): Option[Project] = {
    val ois = new ObjectInputStream(new FileInputStream(file))
    val obj = ois.readObject()
    obj match {
      case p if p.isInstanceOf[Project] => Some(p.asInstanceOf[Project])
      case _ => None
    }
  }

}