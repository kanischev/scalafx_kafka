package ru.sbt.kafka.services

import com.typesafe.config.{Config, ConfigFactory, ConfigObject}
import com.typesafe.scalalogging.LazyLogging
import javafx.collections.{FXCollections, ObservableList}
import javax.naming.ConfigurationException
import pureconfig.{ConfigWriter, loadConfig}
import ru.sbt.kafka.fx.components.ObservableMapWrapper
import scalafx.beans.property.{IntegerProperty, StringProperty}
import scalafx.collections.ObservableMap

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

case class KafkaReceiverConfiguration(
                                       maxMessagesToKeep: Int,
                                       zkConnectionString: String,
                                       subscribedTopics: List[String],
                                       kafkaConsumerConnectionProperties: Map[String, String]
                                     ) {
  def getTypesafeConfig: ConfigObject = {
    ConfigFactory.empty()
      .withValue(KafkaReceiverConfiguration.RootKey, ConfigWriter[KafkaReceiverConfiguration].to(this))
      .root()
  }
}

object KafkaReceiverConfiguration
  extends LazyLogging {

  val RootKey = "kafka-receiver-configuration"

  def apply(config: Config): Try[KafkaReceiverConfiguration] = {
    loadConfig[KafkaReceiverConfiguration](config.getConfig(RootKey)) match {
      case Left(failures) =>
        logger.error(s"Can not extract config: $failures")
        Failure(new ConfigurationException(failures.head.description))
      case Right(cfg) => Success(cfg)
    }
  }
}

class ConfigurationModel {
  val maxMessagesToKeep: IntegerProperty = new IntegerProperty(this, "maxMessagesToKeep", 0)
  val zkConnectionString: StringProperty = new StringProperty(this, "zkConnectionString", "")
  val kafkaConsumerProperties: ObservableMapWrapper[String, String] = ObservableMapWrapper[String, String](ObservableMap[String, String]())
  val topicsList: ObservableList[String] = FXCollections.observableArrayList()

  def this(kafkaReceiverConfiguration: KafkaReceiverConfiguration) {
    this()
    maxMessagesToKeep.set(kafkaReceiverConfiguration.maxMessagesToKeep)
    zkConnectionString.set(kafkaReceiverConfiguration.zkConnectionString)
    topicsList.addAll(kafkaReceiverConfiguration.subscribedTopics: _*)
    kafkaConsumerProperties.map.clear
    kafkaConsumerProperties.map.putAll(kafkaReceiverConfiguration.kafkaConsumerConnectionProperties.asJava)
  }

  def merge(anotherModel: ConfigurationModel): Unit = {
    maxMessagesToKeep.set(anotherModel.maxMessagesToKeep.value)
    zkConnectionString.set(anotherModel.zkConnectionString.value)
    kafkaConsumerProperties.map.clear()
    kafkaConsumerProperties.map.putAll(anotherModel.kafkaConsumerProperties.map)
  }

  def getConfiguration: KafkaReceiverConfiguration = new KafkaReceiverConfiguration(
    maxMessagesToKeep.value,
    zkConnectionString.value,
    topicsList.asScala.toList,
    kafkaConsumerProperties.map.toMap
  )
}

