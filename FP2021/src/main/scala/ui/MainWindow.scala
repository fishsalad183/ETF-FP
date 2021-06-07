package ui

import image.{Image, Operation}
import project.{Project, Selection}

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

  contents = new BorderPanel {
    setContents()

    def setContents(): Unit = {
      layout(imagePanel) = Center
      val menuPanel: GridPanel = new GridPanel(4, 1) {
        contents += projectPanel += layerPanel += selectionPanel += operationPanel
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

  def refresh(guiOnly: Boolean = false): Unit = {
    super.repaint()
    for (content <- contents) {
      content.repaint()
      content.revalidate()
    }
  }

  def imagePanel: BoxPanel = new BoxPanel(Orientation.NoOrientation) {
    border = LineBorder(Color.BLACK, 1)
    refreshImage()

    def refreshImage(): Unit = {
      contents.clear()
      contents += {
        project.evaluateLayers()
        project.evaluateOperations()
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
          project.currentSelection = project.selections.indices.last
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
      val layersChoice = new ComboBox[String](project.layers.zipWithIndex map(layerAndIndex => (layerAndIndex._2 + 1) + ": " + layerAndIndex._1.image.toString())) {
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

    // choosing a selection
    val selectionChoice: ComboBox[String] = new ComboBox[String](project.selections map(_.toString)) {
      selection.index = project.currentSelection
    }
    listenTo(selectionChoice.selection)
    reactions += {
      case SelectionChanged(`selectionChoice`) =>
        project.currentSelection = selectionChoice.selection.index
        refresh()
    }

    // activating/deactivating a selection
    val checkboxActive: CheckBox = new CheckBox("Active") {
      selected = project.selections(project.currentSelection).active
    }
    listenTo(checkboxActive)
    reactions += {
      case ButtonClicked(`checkboxActive`) =>
        project.selections(project.currentSelection).active = !project.selections(project.currentSelection).active
        refresh()
    }

    // deleting a selection
    val buttonDelete = new Button("Delete")
    listenTo(buttonDelete)
    reactions += {
      case ButtonClicked(`buttonDelete`) =>
        project.deleteCurrentSelection()
        refresh()
    }

    contents += new FlowPanel {
      contents += selectionChoice += checkboxActive += buttonDelete
    }

    // showing/hiding selections
    val buttonShowHide = new Button("Show/hide active")
    listenTo(buttonShowHide)
    reactions += {
      case ButtonClicked(`buttonShowHide`) =>
        project.selections filter(_.active) foreach(_.visible = true)
    }
    contents += buttonShowHide
  }



  def operationPanel: GridPanel = new GridPanel(3, 1) {
    border = LineBorder(Color.BLACK, 1)
    contents += new Label("Operation")

    // operation choice
    val operationChoice: ComboBox[String] = new ComboBox[String](project.operations.keys.toSeq) {
      selection.item = project.currentOperation
    }

    // creating a new operation
    def operationsPopup: Frame = new Frame() {
      preferredSize = new Dimension(400, 400)
      var composedOperation: Operation = Operation.id()
      contents = new BoxPanel(Orientation.Vertical) {
        val predefinedOperations: ComboBox[String] = new ComboBox[String](Seq("fill", "add", "sub", "pow", "inv", "grayscale") ++ project.operations.keys)
        contents += new FlowPanel() {
          contents += new Label("Operation") += predefinedOperations
        }
        val valueField1 = new TextField(20)
        contents += new FlowPanel() {
          contents += new Label("Value 1") += valueField1
        }
        val valueField2 = new TextField(20)
        contents += new FlowPanel() {
          contents += new Label("Value 2") += valueField2
        }
        val valueField3 = new TextField(20)
        contents += new FlowPanel() {
          contents += new Label("Value 3") += valueField3
        }

        val buttonCompose = new Button("Compose")
        listenTo(buttonCompose)
        reactions += {
          case ButtonClicked(`buttonCompose`) =>
            composedOperation = composedOperation andThen {
              predefinedOperations.selection.item match {
                case "fill" => Operation.fill(new Color(valueField1.text.toFloat, valueField2.text.toFloat, valueField3.text.toFloat))
                case "add" => Operation.add(valueField1.text.toDouble)
                case "sub" => Operation.sub(valueField1.text.toDouble)
                case "pow" => Operation.pow(valueField1.text.toDouble)
                case "inv" => Operation.inv()
                case "grayscale" => Operation.grayscale()
              }
            }
            buttonCreate.enabled = true
        }

        contents += new FlowPanel() {
          contents += buttonCompose
        }

        val nameField = new TextField(20)
        val buttonCreate = new Button("Create") {
          enabled = false
        }
        listenTo(buttonCreate)
        reactions += {
          case ButtonClicked(`buttonCreate`) =>
            project.operations += (nameField.text -> composedOperation)
            close()
            project.currentOperation = nameField.text
            refresh()
        }

        contents += new FlowPanel() {
          contents += nameField += buttonCreate
        }
      }
    }

    val buttonNew = new Button("New")
    listenTo(buttonNew)
    reactions += {
      case ButtonClicked(`buttonNew`) =>
        operationsPopup.visible = true
    }

    contents += new FlowPanel {
      contents += operationChoice += buttonNew
    }

    // applying an operation
    val buttonApply = new Button("Apply")
    listenTo(buttonApply)
    reactions += {
      case ButtonClicked(`buttonApply`) =>
        project.operationsPerformed += ((operationChoice.selection.item, project.selections filter(_.active)))
        refresh()
    }
    contents += buttonApply
  }
}

