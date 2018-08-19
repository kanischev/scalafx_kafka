package ru.sbt.kafka.fx

import java.util.Properties
import java.util.concurrent.ConcurrentHashMap

import javafx.collections.ListChangeListener
import javafx.scene.{control => jfxsc, layout => jfxsl}
import javafx.{event => jfxe, fxml => jfxf}
import ru.sbt.guice.InjectorHolder
import ru.sbt.kafka.fx.components.records.RecordsViewController
import ru.sbt.kafka.fx.dialogs.SettingsDialog
import ru.sbt.kafka.fx.utils.{FXMLViewExt, KafkaReceiversModel, ReceiverViewTemplate}
import ru.sbt.kafka.services.{ConfigurationModel, ConfigurationService}
import scalafxml.core.macros.sfxml
import scalafxml.guice.GuiceDependencyResolver

import scala.collection.JavaConverters._

@sfxml
class MainController(
                      val mainVBox: jfxsl.VBox,
                      val configService: ConfigurationService
                    ) {

  val model = new ConfigurationModel(configService.getConfiguration)
  implicit val injector = InjectorHolder.injectorEnsureGet
  val messageViewers = new ConcurrentHashMap[String, RecordsViewController]()

  KafkaReceiversModel.elements.addListener(new ListChangeListener[ReceiverViewTemplate[_, _]] {
    override def onChanged(c: ListChangeListener.Change[_ <: ReceiverViewTemplate[_, _]]): Unit = {
      while (c.next()) {
        if (c.wasAdded()) {
          c.getAddedSubList.asScala.foreach(rwt => {
            val (elem, controller) = FXMLViewExt[RecordsViewController](getClass.getResource("components/records/recordsView.fxml"), new GuiceDependencyResolver())
            controller.bindModel(rwt.asInstanceOf[ReceiverViewTemplate[AnyRef, AnyRef]]) // Ugly Types
            mainVBox.getChildren.addAll(controller.getContents)
          })
        }
        if (c.wasRemoved()) {

        }
      }
    }
  })

  model.topicsList.asScala.foreach(topic => {
    KafkaReceiversModel.addStringRecieverFor(
      topic,
      new Properties() {
        putAll(model.getConfiguration.kafkaConsumerConnectionProperties.asJava)
      },
      model.maxMessagesToKeep.value
    )
  })

  model.topicsList.addListener(new ListChangeListener[String] {
    override def onChanged(c: ListChangeListener.Change[_ <: String]): Unit = {
      if (c.wasAdded()) {
        c.getAddedSubList.asScala.foreach(topic => KafkaReceiversModel.addStringRecieverFor(
          topic,
          new Properties() {
            putAll(model.getConfiguration.kafkaConsumerConnectionProperties.asJava)
          },
          model.maxMessagesToKeep.value
        ))
        KafkaReceiversModel.elements
      }
      if (c.wasRemoved()) {
        c.getRemoved.asScala.foreach(KafkaReceiversModel.removeReceiverFor)
      }
    }
  })

  def handleSettingsButtonClick(event: jfxe.ActionEvent): Unit = {
    new SettingsDialog(configService.getConfiguration, Some(cfg => configService.setConfiguration(cfg))).showAndWait()
  }

}
