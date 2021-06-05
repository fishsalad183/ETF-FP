package ui

import image.Image
import project.{Layer, Project, Selection}

import java.awt.Color
import java.io.File
import javax.swing.filechooser.FileNameExtensionFilter
import scala.collection.mutable.ArrayBuffer
import scala.swing.BorderPanel.Position._
import scala.swing.Swing.LineBorder
import scala.swing._
import scala.swing.event.{ButtonClicked, MouseClicked, SelectionChanged, ValueChanged}

class MainWindow(var project: Project) extends MainFrame {
  title = "Image Processing"
  preferredSize = new Dimension(1700, 900)



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

        project.resultingImage = layers.foldLeft(new Image(1280, 800, Color.WHITE))((image, layer) => image blend(layer.image, layer.opacity))
        project.resultingImage
      }
    }

    val clickCoordinates: ArrayBuffer[(Int, Int)] = ArrayBuffer[(Int, Int)]()
    listenTo(mouse.clicks)
    reactions += {
      case e: MouseClicked =>
        clickCoordinates += ((e.point.x, e.point.y))
        if (clickCoordinates.length == 2) {
          val x1 = clickCoordinates(0)._1
          val y1 = clickCoordinates(0)._2
          val x2 = clickCoordinates(1)._1
          val y2 = clickCoordinates(1)._2
          project.selections += new Selection(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2))
          clickCoordinates.clear()
          refresh()
        }
    }

  }




  def projectPanel: GridPanel = new GridPanel(3, 1) {
    border = LineBorder(Color.BLACK, 1)
    contents += new Label("Project: " + project.imageWidth + "x" + project.imageHeight)

    // create new project
    val buttonNew = new Button("New")
    listenTo(buttonNew)
    reactions += {
      case ButtonClicked(`buttonNew`) =>
        project = new Project(1280, 800)
        refresh()
    }

    // save project
    val buttonSave = new Button("Save")
    listenTo(buttonSave)
    reactions += {
      case ButtonClicked(`buttonSave`) =>
        def chooseFile(): Option[File] = {
          val chooser = new FileChooser() {
            fileFilter = new FileNameExtensionFilter(".fp files", "fp")
            selectedFile = new File("project.fp")
          }
          val result = chooser.showSaveDialog(null)
          if (result == FileChooser.Result.Approve) Some(chooser.selectedFile)
          else None
        }

        chooseFile() match {
          case Some(file) => project.save(file)
          case None =>
        }
    }

    // load project
    val buttonLoad = new Button("Load")
    listenTo(buttonLoad)
    reactions += {
      case ButtonClicked(`buttonLoad`) =>
        def chooseFile(): Option[File] = {
          val chooser = new FileChooser() {
            fileSelectionMode = FileChooser.SelectionMode.FilesOnly
            fileFilter = new FileNameExtensionFilter(".fp files", "fp")
          }
          val result = chooser.showOpenDialog(null)
          if (result == FileChooser.Result.Approve) Some(chooser.selectedFile)
          else None
        }

        chooseFile() match {
          case Some(file) =>
            Project.load(file) match {
              case Some(p) =>
                project = p
                refresh()
              case None =>
            }
          case None =>
        }
    }

    contents += new FlowPanel {
      contents += buttonNew += buttonLoad += buttonSave
    }

    // export image
    val buttonExport = new Button("Export image")
    listenTo(buttonExport)
    reactions += {
      case ButtonClicked(`buttonExport`) =>
        def chooseFile(): Option[File] = {
          val chooser = new FileChooser() {
            fileFilter = new FileNameExtensionFilter(".jp(e)g, .png", "jpg", "png")
            selectedFile = new File("image.jpg")
          }
          val result = chooser.showSaveDialog(null)
          if (result == FileChooser.Result.Approve) Some(chooser.selectedFile)
          else None
        }

        chooseFile() match {
          case Some(file) => project.resultingImage.export(file, file.getName.split('.').last)
          case _ =>
        }
    }
    contents += buttonExport
  }



  def layerPanel: GridPanel = new GridPanel(4, 1) {
    border = LineBorder(Color.BLACK, 1)
    setContents()

    def setContents(): Unit = {
      contents.clear()
      contents += new Label("Layer")

      // creating a new empty layer
      val buttonNewEmptyLayer = new Button("New empty layer")
      listenTo(buttonNewEmptyLayer)
      reactions += {
        case ButtonClicked(`buttonNewEmptyLayer`) =>
          project.createNewLayer(new Image(project.imageWidth, project.imageHeight, Color.WHITE))
          refresh()
      }

      // loading images
      val buttonLoadImage = new Button("Load image")
      listenTo(buttonLoadImage)
      reactions += {
        case ButtonClicked(`buttonLoadImage`) =>
          def chooseFile(): Option[File] = {
            val chooser = new FileChooser() {
              fileSelectionMode = FileChooser.SelectionMode.FilesOnly
              fileFilter = new FileNameExtensionFilter(".jp(e)g, .png", "jpg", "jpeg", "png")
            }
            val result = chooser.showOpenDialog(null)
            if (result == FileChooser.Result.Approve) Some(chooser.selectedFile)
            else None
          }

          chooseFile() match {
            case Some(file) =>
              project.createNewLayer(new Image(file.getPath))
              refresh()
            case None =>
          }
      }

      contents += new FlowPanel {
        contents += buttonNewEmptyLayer += buttonLoadImage
      }


      // choosing layers
      val layersChoice = new ComboBox[String](project.layers.zipWithIndex map(layerIndex => (layerIndex._2 + 1) + ": " + layerIndex._1.image.path.split("[\\\\/]").last)) {
        selection.index = project.currentLayer
      }
      listenTo(layersChoice.selection)
      reactions += {
        case SelectionChanged(`layersChoice`) =>
          project.currentLayer = layersChoice.selection.index
          refresh()
      }


      // activating/deactivating a layer
      val checkboxActive = new CheckBox("Active") {
        selected = project.layers(project.currentLayer).active
      }
      listenTo(checkboxActive)
      reactions += {
        case ButtonClicked(`checkboxActive`) =>
          project.layers(project.currentLayer).active = !project.layers(project.currentLayer).active
          refresh()
      }

      // deleting a layer
      val buttonDelete = new Button("Delete")
      listenTo(buttonDelete)
      reactions += {
        case ButtonClicked(`buttonDelete`) =>
          project.deleteCurrentLayer()
          refresh()
      }

      contents += new FlowPanel {
        contents += layersChoice += checkboxActive += buttonDelete
      }

      // setting opacity
      val opacitySlider = new Slider {
        min = 0
        max = 100
        value = (project.layers(project.currentLayer).opacity * 100).toInt
        labels = Map(min -> new Label("0.00"), max -> new Label("1.00"))
      }
      listenTo(opacitySlider)
      reactions += {
        case ValueChanged(`opacitySlider`) =>
          project.layers(project.currentLayer).opacity = opacitySlider.value.toDouble / 100.0
          if (!opacitySlider.adjusting && project.layers(project.currentLayer).active) refresh()
      }
      contents += new FlowPanel {
        contents += new Label("Opacity") += opacitySlider
      }
    }
  }



  def selectionPanel: GridPanel = new GridPanel(3, 1) {
    border = LineBorder(Color.BLACK, 1)
    contents += new Label("Selection")
    val selectionChoicePanel: FlowPanel = new FlowPanel {
      contents += new ComboBox[String](for (i <- project.selections.indices) yield i.toString)
      contents += new CheckBox("Active")
      contents += new Button("Delete")
      contents += new Button("New")
    }
    contents += selectionChoicePanel
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



  def refresh(guiOnly: Boolean = false): Unit = {
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
        contents += projectPanel += layerPanel += selectionPanel += optionPanel
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

