package com.taskadapter.connector.trello

import com.julienvey.trello.domain.{Card, TList}
import com.taskadapter.connector.common.data.ConnectorConverter
import com.taskadapter.model.GTask
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

class GTaskToTrello(listCache: ListCache) extends ConnectorConverter[GTask, Card] {
  val logger = LoggerFactory.getLogger(classOf[GTaskToTrello])

  override def convert(source: GTask): Card = {
    val card = new Card
    card.setId(source.getKey)
    source.getFields.asScala.foreach { e =>
      val fieldName = e._1
      val value = e._2
      fieldName match {
        case TrelloField.listId.name =>
          card.setIdList(value.asInstanceOf[String])
        case TrelloField.listName.name =>
          val listName = value.asInstanceOf[String]
          card.setIdList(listCache.getListIdByName(listName))
        case TrelloField.name.name => card.setName(value.asInstanceOf[String])
        case _ => logger.warn(s"Unknown field in GTask: $fieldName. Skipping it")
      }
    }
    card
  }
}
