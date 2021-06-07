package project

import concepts.Rectangular

import java.awt.Rectangle

//class Selection(val x: Int, val y: Int, val width: Int, val height: Int) extends Rectangular {
//
//
//
//}

@SerialVersionUID(103L)
class Selection(x: Int, y: Int, width: Int, height: Int, var active: Boolean = true) extends Rectangle(x, y, width, height) with Serializable {
  override def toString: String = "x: " + x + ", y: " + y + ", width: " + width + ", height: " + height
}
