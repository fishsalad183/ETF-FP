package project

import image.{Image, Operation}

import java.awt.Color
import java.io._
import scala.collection.mutable.ArrayBuffer

@SerialVersionUID(100L)
class Project private (var layers: ArrayBuffer[Layer], val imageWidth: Int, val imageHeight: Int) extends Serializable {
  var selections: ArrayBuffer[Selection] = ArrayBuffer(new Selection(0, 0, imageWidth, imageHeight))
  val operations: Map[String, Operation] = Map("add" -> Operation.add(10))
  var operationsPerformed = ArrayBuffer[(String, ArrayBuffer[Selection])]()
  var currentLayer: Int = 0
  var currentSelection: Int = 0
  var currentOperation: Int = 0
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

  def evaluateLayers(): Unit = {
    var opacitySum: Double = 0.0
    var count: Int = 0
    for (layer <- this.layers filter(_.active) if opacitySum <= 1.0) {
      opacitySum += layer.opacity
      count += 1
    }
    val layers = this.layers filter(_.active) take(if (opacitySum >= 2.0) count - 1 else count)

    // allow the last layer to be visible only up to total opacity value of 1.0
    if (opacitySum > 1.0 && opacitySum < 2.0) {
      layers.update(layers.length - 1, new Layer(layers.last.image, layers.last.opacity - (opacitySum - 1.0), true))
    }

    this.resultingImage = layers.foldLeft(new Image(1280, 800, Color.WHITE))((image, layer) => image blend(layer.image, layer.opacity))
  }

  def evaluateOperations(): Unit = {
    for ((opName, sels) <- operationsPerformed) resultingImage.perform(operations(opName), sels.toArray)
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