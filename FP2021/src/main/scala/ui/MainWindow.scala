package ui

import image.{Image, Operation, RGB}
import project.{Layer, Project, Selection}

import java.awt.Color
import java.awt.image.BufferedImage
import scala.swing._
import scala.swing.BorderPanel.Position._
import scala.swing.Swing.LineBorder
import scala.swing.event.ButtonClicked

class MainWindow(var project: Project) extends MainFrame {
  title = "Image Processing"
  preferredSize = new Dimension(1600, 900)



  val imagePanel = new BoxPanel(Orientation.NoOrientation) {

    def setImage(): Unit = {
      contents.clear()
      contents += {
        (project.layers(0) blend project.layers(1)).image
//        var opacitySum = 0.0
//        val layers = project.layers filter(_.active) takeWhile { layer =>
//          opacitySum += layer.opacity
//          opacitySum <= 1.0
//        }
//        layers.foldLeft(new Image)((image, layer) => image blend(layer.image, layer.opacity))
      }
    }
    setImage()

    override def revalidate(): Unit = {
      super.revalidate()
      setImage()
    }

    override def repaint(): Unit = {
      super.repaint()
      setImage()
    }

    border = LineBorder(Color.BLACK, 1)
  }




  val projectPanel = new GridPanel(3, 1) {
    contents += new Label("Project")

    val buttonNew = new Button("New")
    listenTo(buttonNew)
    reactions += {
      case ButtonClicked(buttonNew) => {
        project = new Project(Array(new Layer(new Image("src/resource/image/wallhaven-4o7x1p.jpg", 100), 1, true)))
        imagePanel.repaint()
        imagePanel.revalidate()
      }
    }

    val buttonPanel = new FlowPanel {
      contents += buttonNew
      contents += new Button("Load")
      contents += new Button("Save")
    }
    contents += buttonPanel
    contents += new Button("Export Image")
    border = LineBorder(Color.BLACK, 1)
  }



  val layerPanel = new GridPanel(4, 1) {
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



  val selectionPanel = new GridPanel(4, 1) {
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


  val optionPanel = new GridPanel(3, 1) {
    contents += new Label("Operation")
    val operationChoicePanel = new FlowPanel {
      contents += new ComboBox[String](Array("O1", "O2", "O3"))
      contents += new Button("New")
    }
    contents += operationChoicePanel
    contents += new Button("Apply")
    border = LineBorder(Color.BLACK, 1)
  }






  contents = new BorderPanel {
    layout(imagePanel) = Center
    val menuPanel = new GridPanel(4, 1) {
      contents += projectPanel
      contents += layerPanel
      contents += selectionPanel
      contents += optionPanel
    }
    layout(menuPanel) = West
  }

}

