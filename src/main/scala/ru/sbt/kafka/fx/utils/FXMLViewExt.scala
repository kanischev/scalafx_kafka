package ru.sbt.kafka.fx.utils

import java.net.URL

import scalafxml.core.{ControllerDependencyResolver, FXMLLoader}
import javafx.{scene => jfxs}

object FXMLViewExt {
  def apply[T](fxml: URL, dependencies: ControllerDependencyResolver): (jfxs.Parent, T) = {
    val loader = new FXMLLoader(fxml, dependencies)
    loader.load()
    (loader.getRoot[jfxs.Parent](), loader.getController[T]())
  }

}
