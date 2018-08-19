import sbt._

object Dependencies {
  val JacksonVersion = "2.9.4"
  val ScalaLoggingVersion = "3.8.0"
  val ScalaFxCoreVersion = "8.0.144-R12"
  val ScalaFxmlCoreVersion = "0.4"
  val LogbackVersion = "1.2.3"
  val ScalaGuiceVersion = "4.2.1"
  val JMetroVersion = "4.1"
  val TypesafeConfigVersion = "1.3.2"
  val KafkaVersion = "1.1.1"
  val PureConfigVersion = "0.9.1"

  lazy val rootDependencies = Seq(
    "org.scalafx" %% "scalafx" % ScalaFxCoreVersion,
    "org.scalafx" %% "scalafxml-core-sfx8" % ScalaFxmlCoreVersion,
    "org.scalafx" %% "scalafxml-guice-sfx8" % ScalaFxmlCoreVersion,
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % JacksonVersion,
    "ch.qos.logback" % "logback-classic" % LogbackVersion,
    "org.jfxtras" % "jmetro" % JMetroVersion,
    "com.typesafe.scala-logging" %% "scala-logging" % ScalaLoggingVersion,
    "net.codingwell" %% "scala-guice" % ScalaGuiceVersion,
    "com.typesafe" % "config" % TypesafeConfigVersion,
    "com.github.pureconfig" %% "pureconfig" % PureConfigVersion,
    "org.apache.kafka" %% "kafka" % KafkaVersion,
    "org.apache.kafka" % "kafka-clients" % KafkaVersion
  )
}
