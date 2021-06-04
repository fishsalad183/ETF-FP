package ui

import image.Image
import project.{Layer, Project}

import java.awt.Color
import scala.swing._
import scala.swing.BorderPanel.Position._
import scala.swing.Swing.LineBorder
import scala.swing.event.{ButtonClicked, SelectionChanged, ValueChanged}

class MainWindow(var project: Project) extends MainFrame {
  var currentLayer: Int = 0
  title = "Image Processing"
  preferredSize = new Dimension(1600, 900)



  def imagePanel: BoxPanel = new BoxPanel(Orientation.NoOrientation) {
    border = LineBorder(Color.BLACK, 1)
    setContents()

    def setContents(): Unit = {
      contents.clear()
      contents += {
        var opacitySum: Double = 0.0
        var count: Int = 0
        for (layer <- project.layers filter(_.active) if opacitySum <= 1.0) {
          opacitySum += layer.opacity
          count += 1
        }
        val layers = project.layers filter(_.active) take(if (opacitySum >= 2.0) count - 1 else count)

        // allow the last layer to be visible only up to total opacity value of 1.0
        if (opacitySum > 1.0 && opacitySum < 2.0) {
          layers.update(layers.length - 1, new Layer(layers.last.image, layers.last.opacity - (opacitySum - 1.0), true))
        }

        layers.foldLeft(new Image(1280, 800, Color.WHITE))((image, layer) => image blend(layer.image, layer.opacity))
      }
    }
  }






  def projectPanel: GridPanel = new GridPanel(3, 1) {
    border = LineBorder(Color.BLACK, 1)
    contents += new Label("Project: " + project.imageWidth + "x" + project.imageHeight)

    val buttonNew = new Button("New")
    listenTo(buttonNew)
    reactions += {
      case ButtonClicked(buttonNew) => {
        project = new Project(1280, 800)
        refresh()
      }
    }

    val buttonPanel: FlowPanel = new FlowPanel {
      contents += buttonNew
      contents += new Button("Load")
      contents += new Button("Save")
    }
    contents += buttonPanel
    contents += new Button("Export Image")
  }



  def layerPanel: GridPanel = new GridPanel(4, 1) {
    border = LineBorder(Color.BLACK, 1)
    setContents()

    def setContents(): Unit = {
      contents.clear()
      contents += new Label("Layer")

      // choosing layers
      val layersChoice = new ComboBox[String]((project.layers.zipWithIndex map(layerIndex => (layerIndex._2 + 1) + ": " + layerIndex._1.image.path.split("/").last))) {
        selection.index = currentLayer
      }
      listenTo(layersChoice.selection)
      reactions += {
        case SelectionChanged(`layersChoice`) => {
          currentLayer = layersChoice.selection.index
          refresh()
        }
      }

      // creating a new layer


      contents += new FlowPanel {
        contents += layersChoice
        contents += new Button("New")
      }

      // setting if active
      val checkboxActive = new CheckBox("Active") {
        selected = project.layers(currentLayer).active
      }
      contents += checkboxActive
      listenTo(checkboxActive)
      reactions += {
        case ButtonClicked(checboxActive) => {
          project.layers(currentLayer).active = !project.layers(currentLayer).active
          refresh()
        }
      }

      // setting opacity
      val opacitySlider = new Slider {
        min = 0
        max = 100
        value = (project.layers(currentLayer).opacity * 100).toInt
        labels = Map(min -> new Label("0.00"), max -> new Label("1.00"))
      }
      listenTo(opacitySlider)
      reactions += {
        case ValueChanged(`opacitySlider`) => {
          project.layers(currentLayer).opacity = opacitySlider.value.toDouble / 100.0
          if (!opacitySlider.adjusting) refresh()
        }
      }
      contents += new FlowPanel {
        contents += new Label("Opacity")
        contents += opacitySlider
      }
    }
  }



  def selectionPanel: GridPanel = new GridPanel(4, 1) {
    border = LineBorder(Color.BLACK, 1)
    contents += new Label("Selection")
    val selectionChoicePanel: FlowPanel = new FlowPanel {
      contents += new ComboBox[String](Array("S1", "S2", "S3"))
      contents += new Button("Delete")
      contents += new Button("New")
    }
    contents += selectionChoicePanel
    contents += new ToggleButton("Active")
    contents += new Button("Fill")
  }


  def optionPanel: GridPanel = new GridPanel(3, 1) {
    border = LineBorder(Color.BLACK, 1)
    contents += new Label("Operation")
    val operationChoicePanel: FlowPanel = new FlowPanel {
      contents += new ComboBox[String](Array("O1", "O2", "O3"))
      contents += new Button("New")
    }
    contents += operationChoicePanel
    contents += new Button("Apply")
  }



  def refresh(): Unit = {
    super.repaint()
    for (content <- contents) {
      content.repaint()
      content.revalidate()
    }
  }


  contents = new BorderPanel {
    setContents()

    def setContents(): Unit = {
      layout(imagePanel) = Center
      val menuPanel: GridPanel = new GridPanel(4, 1) {
        contents += projectPanel
        contents += layerPanel
        contents += selectionPanel
        contents += optionPanel
      }
      layout(menuPanel) = West
    }

    override def repaint(): Unit = {
      super.repaint()
      setContents()
    }

    override def revalidate(): Unit = {
      super.revalidate()
      setContents()
    }
  }

}

