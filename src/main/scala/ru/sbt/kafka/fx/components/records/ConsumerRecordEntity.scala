package ru.sbt.kafka.fx.components.records

import org.apache.kafka.clients.consumer.ConsumerRecord
import scalafx.beans.property.{IntegerProperty, LongProperty, StringProperty}
import scalafx.scene.control.TableColumn

class ConsumerRecordEntity[K, V](
                                  val _headers: List[(String, Array[Byte])],
                                  val _key: K,
                                  val _value: V,
                                  val _topic: String,
                                  val _partition: Int,
                                  val _offset: Long
                                ) {
  val headers = new StringProperty(this, "header", _headers.map{case (k, v) => s"$k -> $v"}.mkString("\n"))
  val key = new StringProperty(this, "key", Option(_key).map(_.toString).getOrElse(""))
  val value = new StringProperty(this, "value", Option(_value).map(_.toString).getOrElse(""))
  val topic = new StringProperty(this, "topic", _topic)
  val partition = new IntegerProperty(this, "partition", _partition)
  val offset = new LongProperty(this, "offset", _offset)
}

object ConsumerRecordEntity {
  def apply[K, V](record: ConsumerRecord[K, V]) = {
    new ConsumerRecordEntity[K, V](
      record.headers().toArray.map(h => (h.key(), h.value())).toList,
      record.key(),
      record.value(),
      record.topic(),
      record.partition(),
      record.offset()
    )
  }
}

