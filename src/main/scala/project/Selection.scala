package project

import java.awt.Rectangle

@SerialVersionUID(103L)
class Selection(x: Int, y: Int, width: Int, height: Int, var active: Boolean = true) extends Rectangle(x, y, width, height) with Serializable {

  override def toString: String = "x: " + x + ", y: " + y + ", width: " + width + ", height: " + height
}

