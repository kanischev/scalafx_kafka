# KafkaReader
Sample application written in Scala with ScalaFx UI to read Kafka messages from specified topics and show them in desktop app
The further Improvements should make it possible to store consumed data in some files on the client

UI is in Russian and should be localized later

To run this app properly - create on local Kafka Broker topic testTopic (or another one - and edit application.conf)
It receives data from topic and automatically rerenders table.