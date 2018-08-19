package ru.sbt.kafka.services

import java.io.{File, FileNotFoundException, PrintWriter}
import java.util.concurrent.atomic.AtomicReference

import com.typesafe.config.{ConfigFactory, ConfigRenderOptions}
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

import scala.util.{Failure, Success, Try}

trait ConfigurationService {
  def getConfiguration: KafkaReceiverConfiguration
  def setConfiguration(kafkaReceiverConfiguration: KafkaReceiverConfiguration): Try[Unit]
}

/**
  * Configuration Service Impl that uses file to save and extract config from
  * If we try to get config and file is not found - Service tries to create it with default setup
  */
class ConfigFileConfigurationService
  extends ConfigurationService
    with LazyLogging {
  val ActualConfiguration: AtomicReference[KafkaReceiverConfiguration] = new AtomicReference[KafkaReceiverConfiguration]()
  val ConfFile = "config/application.conf"

  lazy val defaultConfig: KafkaReceiverConfiguration = new KafkaReceiverConfiguration(
    200,
    "localhost:2181",
    Nil,
    Map(
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> "localhost:9092",
/* Should depend on topic Data and be configurable on per-topic basis
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer].getCanonicalName,
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer].getCanonicalName,
*/
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "latest",
      ConsumerConfig.GROUP_ID_CONFIG -> "KafkaReaderGroup" //will prepend the "${topic}Group" for topic consumers
    )
  )

  /**
    * Trying to read config from file
    * @return
    */
  private def readConfigFromFile: Try[KafkaReceiverConfiguration] = {
    val file = new File(ConfFile)
    if (!file.exists()) {
      logger.warn("No configuration file found! Trying to create one!")
      Failure(new FileNotFoundException(s"Can not find file with applicationConfiguration $ConfFile"))
    } else {
      Try(ConfigFactory.parseFile(file)).flatMap(KafkaReceiverConfiguration(_))
    }
  }

  /**
    * Trying to write config to file
    * @param cfg configuration structure to save to file
    * @return
    */
  private def writeConfigFile(cfg: KafkaReceiverConfiguration): Try[Unit] = {
    val file = new File(ConfFile)
    logger.info(s"Going to write new configuration $cfg to $ConfFile")

    if (!file.exists()) {
      Try(file.createNewFile()) match {
        case Success(true) => logger.debug("New file created")
        case _ => logger.warn(s"Was unable to create new file in $ConfFile")
      }
    }

    Try {
      val writer = new PrintWriter(ConfFile)
      try {
        writer.write(cfg.getTypesafeConfig.render(ConfigRenderOptions.defaults().setComments(false).setOriginComments(false).setJson(false)))
      } finally {
        writer.close()
      }
    }
  }

  /**
    * Method that return current config (from cache) or tries to get it from file or if this file not exists - uses default values and saves them to file
    * @return
    */
  override def getConfiguration: KafkaReceiverConfiguration = synchronized {
    Option(ActualConfiguration.get()).getOrElse({
      readConfigFromFile match {
        case Success(cfg) =>
          ActualConfiguration.set(cfg)
          cfg
        case Failure(ex) =>
          logger.error(s"Failed to read config from file. Will use default: $defaultConfig", ex)
          writeConfigFile(defaultConfig)
          ActualConfiguration.set(defaultConfig)
          defaultConfig
      }
    })
  }

  override def setConfiguration(kafkaReceiverConfiguration: KafkaReceiverConfiguration): Try[Unit] = synchronized {
    Try {
      if (kafkaReceiverConfiguration != ActualConfiguration.get()) {
        writeConfigFile(kafkaReceiverConfiguration)
        ActualConfiguration.set(kafkaReceiverConfiguration)
      } else {
        logger.info("No need to update configuration. It seems to be the same.")
      }
    }
  }
}