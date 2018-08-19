package ru.sbt.kafka.fx.components.records

import org.apache.kafka.common.TopicPartition
import scalafx.beans.property.{Property, StringProperty}
import scalafx.scene.control.TableColumn

trait KafkaRecordContainer[T] {
  def headers: Map[String, String]
  def readFrom: TopicPartition
  def record: Property[T, T]
  def recordToColumns: Seq[TableColumn[KafkaRecordContainer[T], _]]
}

case class StringKafkaRecordContainer(
                                      headers: Map[String, String],
                                      readFrom: TopicPartition,
                                      recordValue: String
                                     ) extends KafkaRecordContainer[String] {


  override val record = new StringProperty(this, "record", recordValue)

  override def recordToColumns: Seq[TableColumn[KafkaRecordContainer[String], _]] = Seq(
    new TableColumn[KafkaRecordContainer[String], String]{
      text = "Данные"
      // Cell value is loaded from a `Person` object
      cellValueFactory = {_.value.record}
    }
  )
}