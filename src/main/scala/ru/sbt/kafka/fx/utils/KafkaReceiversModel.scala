package ru.sbt.kafka.fx.utils

import java.util.Properties

import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.common.serialization.StringDeserializer
import ru.sbt.kafka.fx.components.records.ConsumerRecordEntity
import ru.sbt.kafka.util.KafkaReceiverElement
import scalafx.beans.value.ObservableValue
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.TableColumn

case class ReceiverViewTemplate[K, V](
                                       receiver: KafkaReceiverElement[K, V],
                                       columns: Seq[TableColumn[ConsumerRecordEntity[K, V], _]]
                                     )

object KafkaReceiversModel extends LazyLogging {
  val elements = ObservableBuffer[ReceiverViewTemplate[_, _]]()

  def addStringRecieverFor(
                            topic: String,
                            kafkaConfig: Properties,
                            maxQuantity: Int,
                            stopWhenMaxAchieved: Boolean = false
                          ): Unit = {
    logger.info(s"Adding data receiver from $topic")
    elements.append(
      ReceiverViewTemplate(
        new KafkaReceiverElement[String, String](kafkaConfig,
          topic,
          classOf[StringDeserializer].getCanonicalName,
          classOf[StringDeserializer].getCanonicalName,
          maxQuantity,
          stopWhenMaxAchieved
        ),
        Seq(
          new TableColumn[ConsumerRecordEntity[String, String], java.lang.Long]{
            text = "Offset"
            cellValueFactory = {_.value.offset.asInstanceOf[ObservableValue[java.lang.Long, java.lang.Long]]}
            prefWidth = 100
          },
          new TableColumn[ConsumerRecordEntity[String, String], String]{
            text = "Key"
            cellValueFactory = {_.value.key}
            prefWidth = 100
          },
          new TableColumn[ConsumerRecordEntity[String, String], String]{
            text = "Value"
            cellValueFactory = {_.value.value}
            prefWidth = 300
          },
          new TableColumn[ConsumerRecordEntity[String, String], String]{
            text = "Headers"
            cellValueFactory = {_.value.headers}
            prefWidth = 150
          },
          new TableColumn[ConsumerRecordEntity[String, String], java.lang.Integer]{
            text = "partition"
            cellValueFactory = {_.value.partition.asInstanceOf[ObservableValue[java.lang.Integer, java.lang.Integer]]}
            prefWidth = 100
          }
        )
      )
    )
  }

  def removeReceiverFor(topic: String) = {
    elements.find(_.receiver.topic == topic).foreach(elem => {
      logger.warn(s"Stopping data receiver from $topic")
      elem.receiver.stopPolling()
      elements.remove(elem)
    })
  }

  def setMaxQuantity(qtt: Int): Unit = {
    elements.foreach(_.receiver.maxQuantity = qtt)
  }
}
