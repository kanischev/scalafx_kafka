package ru.sbt.kafka.fx.components.records

import scalafx.scene.control.TableView
import scalafxml.core.macros.sfxml


@sfxml
class RecordsViewPresenter (
                             val recordsTable: TableView[KafkaRecordContainer[String]]
                           ) {

}
