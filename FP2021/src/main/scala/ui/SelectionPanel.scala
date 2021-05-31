package ui

import java.awt.Color
import scala.swing.Swing.LineBorder
import scala.swing.{Button, ComboBox, FlowPanel, GridPanel, Label, ToggleButton}

class SelectionPanel extends GridPanel(4, 1) {
  contents += new Label("Selection")
  val selectionChoicePanel = new FlowPanel {
    contents += new ComboBox[String](Array("S1", "S2", "S3"))
    contents += new Button("Delete")
    contents += new Button("New")
  }
  contents += selectionChoicePanel
  contents += new ToggleButton("Active")
  contents += new Button("Fill")
  border = LineBorder(Color.BLACK, 1)
}
