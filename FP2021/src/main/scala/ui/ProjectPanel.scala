package ui

import java.awt.Color
import scala.swing.Swing.LineBorder
import scala.swing.{Button, FlowPanel, GridPanel, Label}

class ProjectPanel extends GridPanel(3, 1) {
  contents += new Label("Project")
  val buttonPanel = new FlowPanel {
    contents += new Button("New")
    contents += new Button("Load")
    contents += new Button("Save")
  }
  contents += buttonPanel
  contents += new Button("Export Image")
  border = LineBorder(Color.BLACK, 1)
}
