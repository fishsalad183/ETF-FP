package ui

import image.Image

import java.awt.Color
import scala.swing._
import scala.swing.BorderPanel.Position._
import scala.swing.Swing.LineBorder

class MainWindow extends MainFrame {
  title = "Image Processing"
  preferredSize = new Dimension(1280, 720)

//  def createProjectPanel(): Panel = {
//    val projectPanel = new GridPanel(3, 1) {
//      contents += new Label("Project")
//      val buttonPanel = new FlowPanel {
//        contents += new Button("New")
//        contents += new Button("Load")
//        contents += new Button("Save")
//      }
//      contents += buttonPanel
//      contents += new Button("Export Image")
//    }
//    projectPanel.border = LineBorder(Color.BLACK, 1)
//    projectPanel
//  }

//  def createLayerPanel(): Panel = {
//    val layerPanel = new GridPanel(4, 1) {
//      contents += new Label("Layer")
//      val layerChoicePanel = new FlowPanel {
//        contents += new ComboBox[String](Array("L1", "L2", "L3"))
//        contents += new Button("New")
//      }
//      contents += layerChoicePanel
//      contents += new ToggleButton("Active")
//      val opacityPanel = new FlowPanel {
//        contents += new Label("Opacity")
//        contents += new Slider {
//          min = 0
//          max = 100
//          value = 100
//          labels = Map(0 -> new Label("0.00"), 100 -> new Label("1.00"))
//        }
//      }
//      contents += opacityPanel
//    }
//    layerPanel.border = LineBorder(Color.BLACK, 1)
//    layerPanel
//  }

//  def createSelectionPanel(): Panel = {
//    val selectionPanel = new GridPanel(4, 1) {
//      contents += new Label("Selection")
//      val selectionChoicePanel = new FlowPanel {
//        contents += new ComboBox[String](Array("S1", "S2", "S3"))
//        contents += new Button("Delete")
//        contents += new Button("New")
//      }
//      contents += selectionChoicePanel
//      contents += new ToggleButton("Active")
//      contents += new Button("Fill")
//    }
//    selectionPanel.border = LineBorder(Color.BLACK, 1)
//    selectionPanel
//  }

//  def createOperationPanel(): Panel = {
//    val operationPanel = new GridPanel(3, 1) {
//      contents += new Label("Operation")
//      val operationChoicePanel = new FlowPanel {
//        contents += new ComboBox[String](Array("O1", "O2", "O3"))
//        contents += new Button("New")
//      }
//      contents += operationChoicePanel
//      contents += new Button("Apply")
//    }
//    operationPanel.border = LineBorder(Color.BLACK, 1)
//    operationPanel
//  }

  contents = new BorderPanel {
    val imagePanel = new ImagePanel(new Image("src/resource/image/wallhaven-4o7x1p.jpg", 100))
    layout(imagePanel) = Center
    val menuPanel = new GridPanel(4, 1) {
      contents += new ProjectPanel
      contents += new LayerPanel
      contents += new SelectionPanel
      contents += new OptionPanel
    }
    layout(menuPanel) = West
  }

}

