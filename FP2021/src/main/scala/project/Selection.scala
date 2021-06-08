package project


import java.awt.Rectangle
import scala.swing.{Component, Graphics2D}

//@SerialVersionUID(103L)
//class Selection(x: Int, y: Int, width: Int, height: Int, var active: Boolean = true) extends Rectangle(x, y, width, height) with Serializable {
//
//
//  override def toString: String = "x: " + x + ", y: " + y + ", width: " + width + ", height: " + height
//}

@SerialVersionUID(103L)
class Selection(x: Int, y: Int, width: Int, height: Int, var active: Boolean = true) extends Component with Serializable {
  @transient lazy val rect = new Rectangle(x, y, width, height)

  def contains(x: Int, y: Int): Boolean = rect.contains(x, y)

  override protected def paintComponent(g: Graphics2D): Unit = {
    super.paintComponent(g)
    g.fillRect(x, y, width, height)
  }

  override def toString: String = "x: " + x + ", y: " + y + ", width: " + width + ", height: " + height
}

