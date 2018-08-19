package ru.sbt.kafka.fx.dialogs

import javafx.{scene => jfxs}
import ru.sbt.guice.InjectorHolder
import ru.sbt.kafka.fx.utils.FXMLViewExt
import ru.sbt.kafka.services.{ConfigurationModel, KafkaReceiverConfiguration}
import scalafx.scene.control.Dialog
import scalafxml.guice.GuiceDependencyResolver

/**
  * Class for settings dialog to be created from somewhere in code
  *
  * @param configuration Model object (prefer to use case classes with tranponding to Properties JavaFX objects where need to)
  * @param onSubmit Optional additional handler for saving config Data
  * @param onClose Optional additional handler for closing settings window
  */
class SettingsDialog(
                      configuration: KafkaReceiverConfiguration,
                      onSubmit: Option[KafkaReceiverConfiguration => Unit] = None,
                      onClose: Option[KafkaReceiverConfiguration => Unit] = None
                    ) extends Dialog[KafkaReceiverConfiguration] {

  title = "Настройки приложения"
  val (view, controller) = FXMLViewExt[SettingsController](getClass.getResource("settings.fxml"), new GuiceDependencyResolver()(InjectorHolder.injectorEnsureGet))
  dialogPane().setContent(view)
  // Configuting controller
  controller.setModel(new ConfigurationModel(configuration))
  onSubmit.foreach(controller.addSaveListener)
  onClose.foreach(controller.addCloseListener)
  controller.addCloseListener(_ => {
    dialogPane().getScene.getWindow.hide()
  })
  controller.addSaveListener(_ => {
    dialogPane().getScene.getWindow.hide()
  })
  dialogPane().getScene.getWindow.setOnCloseRequest(event => {
    onClose.foreach(_.apply(configuration))
    dialogPane().getScene.getWindow.hide()
  })
}
