package ui

import image.Image
import project.{Layer, Project}

import java.awt.Color
import scala.swing._
import scala.swing.BorderPanel.Position._
import scala.swing.Swing.LineBorder
import scala.swing.event.ButtonClicked

class MainWindow(var project: Project) extends MainFrame {
  title = "Image Processing"
  preferredSize = new Dimension(1600, 900)



  val imagePanel: BoxPanel = new BoxPanel(Orientation.NoOrientation) {

    def setImage(): Unit = {
      contents.clear()
      contents += {
        var opacitySum: Double = 0.0
        var count: Int = 0
        for (layer <- project.layers filter(_.active) if opacitySum <= 1.0) {
          opacitySum += layer.opacity
          count += 1
        }
//        val layersWithoutLast = project.layers filter(_.active) takeWhile { layer =>
//          opacitySum += layer.opacity
//          count += 1
//          opacitySum <= 1.0
//        }
        val layers = project.layers filter(_.active) take(if (opacitySum >= 2.0) count - 1 else count)

        // allow the last layer to be visible only up to total opacity value of 1.0
        if (opacitySum > 1.0 && opacitySum < 2.0) {
          layers.update(layers.length - 1, new Layer(layers.last.image, layers.last.opacity - (opacitySum - 1.0), true))
        }

        val t = layers.foldLeft(new Image)((image, layer) => image blend(layer.image, layer.opacity))
        t
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




  val projectPanel: GridPanel = new GridPanel(3, 1) {
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

    val buttonPanel: FlowPanel = new FlowPanel {
      contents += buttonNew
      contents += new Button("Load")
      contents += new Button("Save")
    }
    contents += buttonPanel
    contents += new Button("Export Image")
    border = LineBorder(Color.BLACK, 1)
  }



  val layerPanel: GridPanel = new GridPanel(4, 1) {
    contents += new Label("Layer")
    val layerChoicePanel: FlowPanel = new FlowPanel {
      contents += new ComboBox[String](Array("L1", "L2", "L3"))
      contents += new Button("New")
    }
    contents += layerChoicePanel
    contents += new ToggleButton("Active")
    val opacityPanel: FlowPanel = new FlowPanel {
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



  val selectionPanel: GridPanel = new GridPanel(4, 1) {
    contents += new Label("Selection")
    val selectionChoicePanel: FlowPanel = new FlowPanel {
      contents += new ComboBox[String](Array("S1", "S2", "S3"))
      contents += new Button("Delete")
      contents += new Button("New")
    }
    contents += selectionChoicePanel
    contents += new ToggleButton("Active")
    contents += new Button("Fill")
    border = LineBorder(Color.BLACK, 1)
  }


  val optionPanel: GridPanel = new GridPanel(3, 1) {
    contents += new Label("Operation")
    val operationChoicePanel: FlowPanel = new FlowPanel {
      contents += new ComboBox[String](Array("O1", "O2", "O3"))
      contents += new Button("New")
    }
    contents += operationChoicePanel
    contents += new Button("Apply")
    border = LineBorder(Color.BLACK, 1)
  }






  contents = new BorderPanel {
    layout(imagePanel) = Center
    val menuPanel: GridPanel = new GridPanel(4, 1) {
      contents += projectPanel
      contents += layerPanel
      contents += selectionPanel
      contents += optionPanel
    }
    layout(menuPanel) = West
  }

}

