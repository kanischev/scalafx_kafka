package ru.sbt.kafka.util

import java.util.Properties

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import ru.sbt.kafka.fx.components.records.ConsumerRecordEntity
import scalafx.beans.property.{BooleanProperty, LongProperty}
import scalafx.collections.ObservableBuffer

class KafkaReceiverElement[K, V](
                                  val kafkaDefaultProperties: Properties,
                                  val topic: String,
                                  val keyDeserializerClassName: String,
                                  val valueDeserializerClassName: String,
                                  @volatile var maxQuantity: Int,
                                  val stopWhenMaxAchieved: Boolean = false
                                ) {
  val connectionProperties: Properties = new Properties(){
    putAll(kafkaDefaultProperties)
    put(ConsumerConfig.GROUP_ID_CONFIG, s"${kafkaDefaultProperties.getOrDefault(ConsumerConfig.GROUP_ID_CONFIG, "")}${topic.toLowerCase()}Reader")
    put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializerClassName)
    put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializerClassName)
  }

  private val fetcherRunnable = RunnableKafkaConsumer[K, V](
    connectionProperties,
    topic,
    onEvent,
    onError
  )
  private val fetchThread = new Thread(fetcherRunnable)
  fetchThread.start()

  val received: ObservableBuffer[ConsumerRecordEntity[K, V]] = ObservableBuffer[ConsumerRecordEntity[K, V]](Nil)
  val messagesQuantity: LongProperty = new LongProperty(this, "messagesQuantity", 0L)
  val isRunning: BooleanProperty = BooleanProperty(true)

  def clear() = this.synchronized{
    received.clear()
  }

  def onEvent(message: ConsumerRecord[K, V]): Unit = {
    if (received.size + 1 < maxQuantity) {
      received.prepend(ConsumerRecordEntity(message))
      messagesQuantity.set(messagesQuantity.get()+1)
    }
    else if (!stopWhenMaxAchieved) {
      received.remove(maxQuantity-1)
      received.prepend(ConsumerRecordEntity(message))
      messagesQuantity.set(messagesQuantity.get()+1)
    } else {
      received.prepend(ConsumerRecordEntity(message))
      messagesQuantity.set(messagesQuantity.get()+1)
      fetcherRunnable.stopPolling()
    }
  }

  def onError(ex: Throwable): Boolean = {
    isRunning.set(false)
    false
  }

  def stopPolling() = {
    fetcherRunnable.stopPolling()
  }
}


