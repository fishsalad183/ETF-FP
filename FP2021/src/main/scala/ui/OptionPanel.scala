package ui

import java.awt.Color
import scala.swing.Swing.LineBorder
import scala.swing.{Button, ComboBox, FlowPanel, GridPanel, Label}

class OptionPanel extends GridPanel(3, 1) {
  contents += new Label("Operation")
  val operationChoicePanel = new FlowPanel {
    contents += new ComboBox[String](Array("O1", "O2", "O3"))
    contents += new Button("New")
  }
  contents += operationChoicePanel
  contents += new Button("Apply")
  border = LineBorder(Color.BLACK, 1)
}
