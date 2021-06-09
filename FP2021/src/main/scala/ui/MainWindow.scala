package ui

import image.{Image, Operation}
import project.{Project, Selection}

import java.awt.{BasicStroke, Color}
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
  private var displayActiveSelections = false

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

  def refresh(): Unit = {
    super.repaint()
    for (content <- contents) {
      content.repaint()
      content.revalidate()
    }
  }



  def imagePanel: Panel = new Panel() {
    border = LineBorder(Color.BLACK, 1)

    override protected def paintComponent(g: Graphics2D): Unit = {
      super.paintComponent(g)
      g.drawImage(project.resultingImage.img, 0, 0, null)
      g.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 3.0f, Array(10.0f, 10.0f), 0.0f))
      if (displayActiveSelections) project.selections filter(s => s.active) foreach(s => g.draw(s))
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
          project.createNewSelection(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2))
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
        resolutionSelectionPopup.visible = true
        refresh()
    }

    val resolutionSelectionPopup: Frame = new Frame() {
      title = "New project"
      preferredSize = new Dimension(400, 100)
      contents = new BoxPanel(Orientation.Vertical) {
        val widthField = new TextField(10)
        val heightField = new TextField(10)
        contents += new FlowPanel() {
          contents += new Label("Resolution: ") += widthField += new Label("x") += heightField
        }
        val buttonCreate = new Button("Create")
        listenTo(buttonCreate)
        reactions += {
          case ButtonClicked(`buttonCreate`) =>
            project = new Project(widthField.text.toInt, heightField.text.toInt)
            close()
            refresh()
        }
        contents += buttonCreate
      }
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
      preferredSize = new Dimension(150, preferredSize.height)
      tooltip = project.layers(project.currentLayer).image.path
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
        project.toggleCurrentLayerActive()
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
        if (!opacitySlider.adjusting) {
          project.setCurrentLayerOpacity(opacitySlider.value.toDouble / 100.0)
          refresh()
        }
    }
    contents += new FlowPanel {
      contents += new Label("Opacity") += opacitySlider
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
        project.toggleCurrentSelectionActive()
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
        displayActiveSelections = !displayActiveSelections
        refresh()
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
    val operationsPopup: Frame = new Frame() {
      title = "New operation"
      preferredSize = new Dimension(400, 400)

      var composedOperation: Operation = Operation.id
      contents = new BoxPanel(Orientation.Vertical) {
        val predefinedOperations: ComboBox[String] = new ComboBox[String](Seq("fill", "add", "sub", "revsub", "mul", "div", "revdiv", "pow", "log", "abs", "min", "max", "inv", "grayscale", "median") ++ project.operations.keys)
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
                case "revsub" => Operation.revsub(valueField1.text.toDouble)
                case "mul" => Operation.mul(valueField1.text.toDouble)
                case "div" => Operation.div(valueField1.text.toDouble)
                case "revdiv" => Operation.revdiv(valueField1.text.toDouble)
                case "pow" => Operation.pow(valueField1.text.toDouble)
                case "log" => Operation.log()
                case "abs" => Operation.abs()
                case "min" => Operation.min(valueField1.text.toDouble)
                case "max" => Operation.max(valueField1.text.toDouble)
                case "inv" => Operation.inv()
                case "grayscale" => Operation.grayscale()
                case "median" => Operation.median(valueField1.text.toInt, if (valueField2.text == "v") Orientation.Vertical else Orientation.Horizontal)
                case custom => project.operations(custom)
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
            project.createNewOperation(nameField.text, composedOperation)
            close()
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
        project.perform(operationChoice.selection.item)
        refresh()
    }
    contents += buttonApply
  }
}

