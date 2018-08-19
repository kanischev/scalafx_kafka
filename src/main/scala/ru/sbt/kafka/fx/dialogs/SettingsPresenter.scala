package ru.sbt.kafka.fx.dialogs

import java.text.DecimalFormat

import com.typesafe.scalalogging.LazyLogging
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.cell
import javafx.scene.{control => jfxsc, layout => jfxsl}
import javafx.{event => jfxe, fxml => jfxf}
import ru.sbt.kafka.services.{ConfigurationModel, ConfigurationService, KafkaReceiverConfiguration}
import scalafx.Includes._
import scalafx.event.Event
import scalafx.util.converter.DefaultStringConverter
import scalafxml.core.macros.sfxml

import scala.collection.mutable.ListBuffer

/**
  * Interface is needed because otherwise we win't be able to cast instance
  * generated with macroses to origin type
  */
trait SettingsController {
  def setModel(actualModel: ConfigurationModel): Unit
  def addSaveListener(handler: KafkaReceiverConfiguration => Unit): Unit
  def addCloseListener(handler: KafkaReceiverConfiguration => Unit): Unit
  def onCancel(event: Event): Unit
  def onSave(event: Event): Unit
}

/**
  * Controller implementation class
  */
@sfxml
class SettingsPresenter (
                          val maxRecordsCountInput: jfxsc.TextField,
                          val zkConnectionStringInput: jfxsc.TextField,
                          val settingsKafkaParamKeyInput: jfxsc.TextField,
                          val settingsKafkaParamValueInput: jfxsc.TextField,
                          val kafkaParamsTable: jfxsc.TableView[java.util.Map.Entry[String, String]],
                          val kafkaConnectionKeyColumn: jfxsc.TableColumn[java.util.Map.Entry[String, String], String],
                          val kafkaConnectionValueColumn: jfxsc.TableColumn[java.util.Map.Entry[String, String], String],
                          val settingsSaveButton: jfxsc.Button,
                          val settingsCancelButton: jfxsc.Button,
                          val configurationService: ConfigurationService
                       ) extends LazyLogging with SettingsController {
  val model = new ConfigurationModel()

  private val saveListeners = ListBuffer.empty[KafkaReceiverConfiguration => Unit]
  private val cancelListeners = ListBuffer.empty[KafkaReceiverConfiguration => Unit]

  //prevent non-numeric chars in input
  maxRecordsCountInput.textProperty().addListener {
    (observable, oldValue, newValue) => {
      if (!newValue.matches("\\d*")) {
        maxRecordsCountInput.setText(newValue.replaceAll("[^\\d]", ""))
      }
    }
  }
  maxRecordsCountInput.textProperty().bindBidirectional(model.maxMessagesToKeep, new DecimalFormat()) //Not a good solution - not working properly
  zkConnectionStringInput.textProperty() <==> model.zkConnectionString

  kafkaParamsTable.setItems(model.kafkaConsumerProperties.entries)
  // To handle keys change

  kafkaConnectionKeyColumn.setCellValueFactory(
    (p) => new SimpleStringProperty(p.getValue.getKey)
  )
  kafkaConnectionKeyColumn.setCellFactory(_ => new cell.TextFieldTableCell(new DefaultStringConverter()))
  kafkaConnectionKeyColumn.setOnEditCommit(event => {
    val oldKey = event.getOldValue
    val oldLineItem = model.kafkaConsumerProperties.map.get(oldKey)
    model.kafkaConsumerProperties.map.remove(oldKey) //should remove from list but maybe doesn't always
    if (event.getNewValue.trim.nonEmpty) {
      oldLineItem.foreach(model.kafkaConsumerProperties.map.put(event.getNewValue.trim, _))
    }
  })

  kafkaConnectionValueColumn.setCellValueFactory(
    (p) => new SimpleStringProperty(p.getValue.getValue)
  )
  kafkaConnectionValueColumn.setCellFactory(_ => new cell.TextFieldTableCell(new DefaultStringConverter()))
  kafkaConnectionValueColumn.setOnEditCommit(event => {
    val rowKey = event.getRowValue.getKey
    val oldLineItem = model.kafkaConsumerProperties.map.get(rowKey)
    if (oldLineItem != Option(event.getNewValue)) {
      model.kafkaConsumerProperties.map.put(rowKey, event.getNewValue)
    }
  })

  def onAddKafkaConfigParam(event: Event) = {
    if (settingsKafkaParamKeyInput.getText.trim.nonEmpty) {
      model.kafkaConsumerProperties.map.put(settingsKafkaParamKeyInput.getText.trim, settingsKafkaParamValueInput.getText)
      settingsKafkaParamKeyInput.setText("")
      settingsKafkaParamValueInput.setText("")
    }
  }

  override def setModel(actualModel: ConfigurationModel): Unit = {
    model.merge(actualModel)
  }

  override def addSaveListener(handler: KafkaReceiverConfiguration => Unit): Unit = saveListeners.append(handler)

  override def addCloseListener(handler: KafkaReceiverConfiguration => Unit): Unit = cancelListeners.append(handler)

  override def onCancel(event: Event): Unit = cancelListeners.foreach(_.apply(model.getConfiguration))

  override def onSave(event: Event): Unit = saveListeners.foreach(_.apply(model.getConfiguration))
}
