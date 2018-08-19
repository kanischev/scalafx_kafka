package ru.sbt.kafka.fx.components

import java.util.AbstractMap

import javafx.collections.{ListChangeListener, MapChangeListener}
import scalafx.collections.{ObservableBuffer, ObservableMap}

import scala.collection.JavaConverters._

case class ObservableMapWrapper[K, V](map: ObservableMap[K, V]) {
  val entries = ObservableBuffer(map.entrySet().asScala.toList)

  private val mapChangeListener = new MapChangeListener[K, V]() {
    override def onChanged(change: MapChangeListener.Change[_ <: K, _ <: V]): Unit = {
      entries.removeListener(listChangeListener)
      if (change.wasAdded) entries.append(new java.util.AbstractMap.SimpleEntry(change.getKey, change.getValueAdded))
      if (change.wasRemoved) {
        entries.find(_.getKey == change.getKey).foreach(entries.remove(_))
      }
      entries.addListener(listChangeListener)
    }
  }

  private val listChangeListener: ListChangeListener[java.util.Map.Entry[K, V]] = (change: ListChangeListener.Change[_ <: java.util.Map.Entry[K, V]]) => {
    map.removeListener(mapChangeListener)
    while ( {
      change.next
    }) { //maybe check for uniqueness here
      if (change.wasAdded) {
        change.getAddedSubList.asScala.foreach(e => map.put(e.getKey, e.getValue))
      }
      if (change.wasRemoved) {
        change.getRemoved.asScala.foreach(e => map.remove(e.getKey))
      }
    }
    map.addListener(mapChangeListener)
  }

  map.addListener(mapChangeListener)
  entries.addListener(listChangeListener)

  //adding to list should be unique
  def addUnique(key: K, value: V): Unit = {
    var isFound = false
    //if a duplicate key just change the value
    map.entrySet().asScala.find(_.getKey == key).map(_.setValue(value)).getOrElse(entries.add(new AbstractMap.SimpleEntry[K, V](key, value)))
  }

  //for doing lenghty map operations
  def removeMapListener(): Unit = {
    map.removeListener(mapChangeListener)
  }

  //for resyncing list to map after many changes
  def resetMapListener(): Unit = {
    entries.removeListener(listChangeListener)
    entries.clear()
    entries.addAll(map.entrySet)
    entries.addListener(listChangeListener)
    map.addListener(mapChangeListener)
  }

}
