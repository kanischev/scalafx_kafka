package ru.sbt.kafka.fx.components.records

import ru.sbt.kafka.fx.utils.ReceiverViewTemplate
import scalafx.scene.Node
import scalafx.scene.control.{TitledPane}
import scalafxml.core.macros.sfxml
import javafx.scene.{control => jfxsc, layout => jfxsl}
import scalafx.Includes._

trait RecordsViewController {
  def bindModel(model: ReceiverViewTemplate[AnyRef, AnyRef]): Unit //F*ck generics

  def getContents: Node
}

@sfxml
class RecordsViewPresenter (
                             val recordsContainerPane: TitledPane,
                             val recordsTable: jfxsc.TableView[ConsumerRecordEntity[AnyRef, AnyRef]]
                           ) extends RecordsViewController {
  override def bindModel(model: ReceiverViewTemplate[AnyRef, AnyRef]): Unit = {
    recordsContainerPane.setText(model.receiver.topic)
    model.columns.foreach(tc => recordsTable.columns.add(tc))
    recordsTable.items.setValue(model.receiver.received)
  }

  override def getContents: Node = recordsContainerPane
}
