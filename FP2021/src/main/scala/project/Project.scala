package project

import image.Image

class Project(var layers: Array[Layer]) {

  def save(): Unit = ???

}

object Project {

  def createNew(): Project = ???
  def load(): Project = ???

}