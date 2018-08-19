package ru.sbt.kafka.fx

import com.google.inject.{AbstractModule, Injector, Scopes}
import javafx.event.EventHandler
import javafx.stage.WindowEvent
import jfxtras.styles.jmetro8.JMetro
import ru.sbt.guice.{InjectionConfigurator, InjectorHolder}
import ru.sbt.kafka.fx.utils.KafkaReceiversModel
import ru.sbt.kafka.services.{ConfigFileConfigurationService, ConfigurationService}
import scalafx.Includes._
import scalafx.application.{JFXApp, Platform}
import scalafx.scene.Scene
import scalafxml.core.FXMLView
import scalafxml.guice.GuiceDependencyResolver

object KafkaReceiverApp
  extends JFXApp
    with InjectionConfigurator {

  // Configure Guice
  val modules = List(
    new AbstractModule {
      override def configure() {
        bind(classOf[ConfigurationService]) to classOf[ConfigFileConfigurationService] in Scopes.SINGLETON
      }
    }
  )

  installAll(modules)
  implicit val injector: Injector = InjectorHolder.injectorEnsureGet

  // Dark theme style
  val style = JMetro.Style.DARK
  val Scene = new Scene(
    FXMLView(
      getClass.getResource("sample.fxml"),
      new GuiceDependencyResolver()
    ), 800, 600
  )

  new JMetro(style).applyTheme(Scene)

  stage = new JFXApp.PrimaryStage() {
    title = "Подписчик на данные из Kafka"
    scene = Scene
  }
  stage.setOnCloseRequest(new EventHandler[WindowEvent] {
    override def handle(event: WindowEvent): Unit = {
      KafkaReceiversModel.elements.foreach(rwt => {
        rwt.receiver.stopPolling()
      })
      //TODO: wait for KafkaReceiversModel.elements.forall(!_.receiver.isRunning.get())
      Platform.exit()
      System.exit(0)
    }
  })
}
