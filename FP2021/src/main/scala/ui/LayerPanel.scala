package ui

import java.awt.Color
import scala.swing.Swing.LineBorder
import scala.swing.{Button, ComboBox, FlowPanel, GridPanel, Label, Slider, ToggleButton}

class LayerPanel extends GridPanel(4, 1) {
  contents += new Label("Layer")
  val layerChoicePanel = new FlowPanel {
    contents += new ComboBox[String](Array("L1", "L2", "L3"))
    contents += new Button("New")
  }
  contents += layerChoicePanel
  contents += new ToggleButton("Active")
  val opacityPanel = new FlowPanel {
    contents += new Label("Opacity")
    contents += new Slider {
      min = 0
      max = 100
      value = 100
      labels = Map(min -> new Label("0.00"), max -> new Label("1.00"))
    }
  }
  contents += opacityPanel
  border = LineBorder(Color.BLACK, 1)
}
