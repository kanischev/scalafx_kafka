package ru.sbt.kafka.util

import java.util
import java.util.{Collections, Properties}

import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.consumer.{ConsumerRecord, KafkaConsumer}
import org.apache.kafka.common.errors.WakeupException

import scala.collection.JavaConverters._
import scala.util.control.NonFatal

//Can be used in executors ThreadPool if you want to
//TODO: the way to commit offsets should be configurable. Now they're supposed to be autocommited
case class RunnableKafkaConsumer[K, V](
                                        kafkaProperties: Properties,
                                        topic: String,
                                        onMessage: ConsumerRecord[K, V] => Unit,
                                        onError: (Throwable) => Boolean = _ => true, //Whether to keep polling on exception
                                        pollTimoutMs: Int = 100
                                ) extends Runnable with LazyLogging {
  @volatile
  private var executed: Boolean = true
  private lazy val consumer = new KafkaConsumer[K, V](kafkaProperties)

  override def run(): Unit = {
    try{
      consumer.subscribe(util.Arrays.asList(topic))
      while (executed) {
        try{
          logger.trace(s"Polling $topic topic")
          val records = consumer.poll(100)
          if (records.count() > 0) logger.info(s"Got ${records.count()} messages from $topic topic")
          records.asScala.foreach(consumerRecord => {
            onMessage(consumerRecord)
          })
        } catch {
          case wue: WakeupException =>
            logger.error(s"Exiting consumer polling for topic $topic")
            executed = false
            onError(wue)
          case NonFatal(e) =>
            logger.error("Got exception on processing", e)
            if (onError(e))
              logger.info("Keep polling after exception")
            else
              executed = false
        }
      }
    } finally {
      consumer.close()
    }
  }

  def stopPolling() = consumer.wakeup()
}
