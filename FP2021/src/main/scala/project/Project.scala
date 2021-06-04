package project

import image.Image

import java.awt.Color
import java.io.{File, FileInputStream, FileOutputStream, ObjectInputStream, ObjectOutputStream}
import scala.swing.FileChooser

@SerialVersionUID(100L)
class Project private (val layers: Array[Layer], val imageWidth: Int, val imageHeight: Int) extends Serializable {
  def this(layers: Array[Layer]) = this(layers, layers(0).image.width, layers(0).image.height)
  def this(imageWidth: Int, imageHeight: Int) = this(Array(new Layer(new Image(imageWidth, imageHeight, Color.WHITE), 1.0, true)), imageWidth, imageHeight)

  def save(file: File): Unit = {
    val oos = new ObjectOutputStream(new FileOutputStream(file))
    oos.writeObject(this)
    oos.close()
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