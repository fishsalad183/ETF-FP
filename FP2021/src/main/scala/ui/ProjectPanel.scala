package ui

import image.Image
import project.{Layer, Project}

import java.awt.Color
import scala.swing.Swing.LineBorder
import scala.swing.event.ButtonClicked
import scala.swing.{Button, FlowPanel, GridPanel, Label}

class ProjectPanel(val mainWindow: MainWindow) extends GridPanel(3, 1) {
  contents += new Label("Project")

  val buttonNew = new Button("New")
  listenTo(buttonNew)
  reactions += {
    case ButtonClicked(buttonNew) => {
      mainWindow.project = new Project(Array(new Layer(new Image("src/resource/image/wallhaven-4o7x1p.jpg"), 1, true)))
      mainWindow.imagePanel.revalidate()
      mainWindow.imagePanel.repaint()
      mainWindow.repaint()
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
