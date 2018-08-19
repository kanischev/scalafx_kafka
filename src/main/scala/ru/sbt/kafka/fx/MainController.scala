package ru.sbt.kafka.fx

import javafx.scene.{control => jfxsc, layout => jfxsl}
import javafx.{event => jfxe, fxml => jfxf}
import ru.sbt.kafka.fx.dialogs.SettingsDialog
import ru.sbt.kafka.services.{ConfigurationModel, ConfigurationService}
import scalafxml.core.macros.sfxml

import scala.collection.JavaConverters._

@sfxml
class MainController(
                      val mainVBox: jfxsl.VBox,
                      val configService: ConfigurationService
                    ) {

  val model = new ConfigurationModel(configService.getConfiguration)
  //model.topicsList.asScala.foreach()

  def handleSettingsButtonClick(event: jfxe.ActionEvent): Unit = {
    new SettingsDialog(configService.getConfiguration, Some(cfg => configService.setConfiguration(cfg))).showAndWait()
  }

}
