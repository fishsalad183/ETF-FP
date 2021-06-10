package ui

import image.{Image, Operation, OperationSequence, SimpleOperation}
import project.Project

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

    // crating a new project
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

    // saving a project
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

    // loading a project
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

    // image exporting
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
    val layersChoice: ComboBox[String] = new ComboBox[String](project.layers.zipWithIndex map(layerAndIndex => (layerAndIndex._2 + 1) + ": " + layerAndIndex._1.image.toString())) {
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
    val checkboxActive: CheckBox = new CheckBox("Active") {
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
    val opacitySlider: Slider = new Slider {
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

    // showing/hiding active selections
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

    // choosing an operation
    val operationChoice: ComboBox[String] = new ComboBox[String](project.operations.keys.toSeq) {
      selection.item = project.currentOperation
    }

    // creating a new operation
    val operationsPopup: Frame = new Frame() {
      title = "New operation"
      preferredSize = new Dimension(800, 400)

      val nameField = new TextField(20)
      val grid = new GridPanel(1, 3)
      contents = new BoxPanel(Orientation.Vertical) {
        contents += new FlowPanel() {
          contents += new Label("Function name: ") += nameField
        }
        contents += grid
      }

      // simple operations
      grid.contents += new BoxPanel(Orientation.Vertical) {
        var composedOperation: SimpleOperation = Operation.id

        val simpleOps: ComboBox[String] = new ComboBox[String](Seq("fill", "add", "sub", "revsub", "mul", "div", "revdiv", "pow", "log", "abs", "min", "max", "inv", "grayscale") ++ project.operations.filter{
          case (_, op) => op.isInstanceOf[SimpleOperation]
        } .keys)
        contents += new FlowPanel() {
          contents += new Label("Simple operation") += simpleOps
        }
        val simpleOpValueFields: Array[TextField] = Array.fill[TextField](3)(new TextField(20))
        simpleOpValueFields.zipWithIndex.foreach {
          case (field, i) =>
            contents += new FlowPanel() {
              contents += new Label("Value " + i.toString) += field
            }
        }

        val buttonCompose = new Button("Compose")
        listenTo(buttonCompose)
        reactions += {
          case ButtonClicked(`buttonCompose`) =>
            composedOperation = composedOperation andThen {
              simpleOps.selection.item match {
                case "fill" => Operation.fill(new Color(simpleOpValueFields(0).text.toFloat, simpleOpValueFields(1).text.toFloat, simpleOpValueFields(2).text.toFloat))
                case "add" => Operation.add(simpleOpValueFields(0).text.toDouble)
                case "sub" => Operation.sub(simpleOpValueFields(0).text.toDouble)
                case "revsub" => Operation.revsub(simpleOpValueFields(0).text.toDouble)
                case "mul" => Operation.mul(simpleOpValueFields(0).text.toDouble)
                case "div" => Operation.div(simpleOpValueFields(0).text.toDouble)
                case "revdiv" => Operation.revdiv(simpleOpValueFields(0).text.toDouble)
                case "pow" => Operation.pow(simpleOpValueFields(0).text.toDouble)
                case "log" => Operation.log()
                case "abs" => Operation.abs()
                case "min" => Operation.min(simpleOpValueFields(0).text.toDouble)
                case "max" => Operation.max(simpleOpValueFields(0).text.toDouble)
                case "inv" => Operation.inv()
                case "grayscale" => Operation.grayscale()
                case custom => project.operations(custom).asInstanceOf[SimpleOperation]
              }
            }
            buttonCreate.enabled = true
        }
        contents += buttonCompose

        val buttonCreate: Button = new Button("Create composite") {
          enabled = false
        }
        listenTo(buttonCreate)
        reactions += {
          case ButtonClicked(`buttonCreate`) =>
            project.createNewOperation(nameField.text, composedOperation)
            close()
            refresh()
        }
        contents += buttonCreate
      }

      // filter operations
      grid.contents += new BoxPanel(Orientation.Vertical) {
        val filterOps: ComboBox[String] = new ComboBox[String](Seq("median"))
        contents += new FlowPanel() {
          contents += new Label("Filter operations") += filterOps
        }
        val filterOpValueFields: Array[TextField] = Array.fill[TextField](3)(new TextField(20))
        filterOpValueFields.zipWithIndex.foreach {
          case (field, i) =>
            contents += new FlowPanel() {
              contents += new Label("Value " + i.toString) += field
            }
        }

        val buttonCreate = new Button("Create filtering")
        listenTo(buttonCreate)
        reactions += {
          case ButtonClicked(`buttonCreate`) =>
            val newOp = filterOps.selection.item match {
              case "median" => Operation.median(filterOpValueFields(0).text.toInt, if (filterOpValueFields(1).text == "v") Orientation.Vertical else Orientation.Horizontal)
            }
            project.createNewOperation(nameField.text, newOp)
            close()
            refresh()
        }
        contents += buttonCreate
      }

      // operation sequence
      grid.contents += new BoxPanel(Orientation.Vertical) {
        val allOps: ComboBox[String] = new ComboBox[String](project.operations.keys.toSeq)
        contents += new FlowPanel() {
          contents += new Label("All operations") += allOps
        }
        var ops: List[Operation] = List()

        val buttonAdd = new Button("Add to sequence")
        listenTo(buttonAdd)
        reactions += {
          case ButtonClicked(`buttonAdd`) => ops :+= project.operations(allOps.selection.item)
        }
        contents += buttonAdd

        val buttonCreate = new Button("Create sequence")
        listenTo(buttonCreate)
        reactions += {
          case ButtonClicked(`buttonCreate`) =>
            project.createNewOperation(nameField.text, new OperationSequence(ops))
            close()
            refresh()
        }
        contents += buttonCreate
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

