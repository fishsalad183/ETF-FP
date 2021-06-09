package project

import image.{Image, Operation}

import java.awt.Color
import java.io._
import scala.collection.mutable.{ArrayBuffer, Map}

@SerialVersionUID(100L)
class Project private (val layers: ArrayBuffer[Layer], val imageWidth: Int, val imageHeight: Int) extends Serializable {
  val selections: ArrayBuffer[Selection] = ArrayBuffer(new Selection(0, 0, imageWidth, imageHeight))
  val operations: Map[String, Operation] = Map("id" -> Operation.id)
  var operationsPerformed = ArrayBuffer[(String, ArrayBuffer[Selection])]()
  var currentLayer: Int = 0
  var currentSelection: Int = 0
  var currentOperation: String = "id"

  @transient var resultingImage: Image = null
  evaluateImage()

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
    evaluateImage()
  }

  def deleteCurrentLayer(): Unit = {
    layers.remove(currentLayer)
    if (layers.isEmpty) layers += new Layer(imageWidth, imageHeight, Color.WHITE)
    currentLayer = 0
    evaluateImage()
  }

  def toggleCurrentLayerActive(): Unit = {
    layers(currentLayer).active = !layers(currentLayer).active
    evaluateImage()
  }

  def setCurrentLayerOpacity(value: Double): Unit = {
    layers(currentLayer).opacity = value
    evaluateImage()
  }

  def createNewSelection(x: Int, y: Int, width: Int, height: Int, active: Boolean = true): Unit = {
    selections += new Selection(x, y, width, height, active)
    currentSelection = selections.length - 1
  }

  def deleteCurrentSelection(): Unit = {
    val sel = selections(currentSelection)
    selections -= sel
    operationsPerformed.foreach(_._2 -= sel)
    if (selections.isEmpty) selections += new Selection(0, 0, imageWidth, imageHeight)
    currentSelection = 0
    evaluateImage()
  }

  def toggleCurrentSelectionActive(): Unit = selections(currentSelection).active = !selections(currentSelection).active

  def createNewOperation(name: String, op: Operation): Unit = {
    operations += (name -> op)
    currentOperation = name
  }

  private def evaluateImage(): Unit = {
    evaluateLayers()
    evaluateOperations()

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

      this.resultingImage = layers.foldLeft(new Image(imageWidth, imageHeight, Color.WHITE))((image, layer) => image blend(layer.image, layer.opacity))
    }

    def evaluateOperations(): Unit = for ((opName, sels) <- operationsPerformed) resultingImage.perform(operations(opName), sels.toArray)
  }

  def perform(operation: String): Unit = {
    val activeSelections = selections filter(_.active)
    operationsPerformed += ((operation, activeSelections))
    evaluateImage()
  }

}

object Project {

  def load(file: File): Option[Project] = {
    val ois = new ObjectInputStream(new FileInputStream(file))
    val obj = ois.readObject()
    obj match {
      case p if p.isInstanceOf[Project] => {
        p.asInstanceOf[Project].evaluateImage()
        Some(p.asInstanceOf[Project])
      }
      case _ => None
    }
  }

}