package com.taskadapter.connector.trello

import java.util.Date

import com.julienvey.trello.domain.{Card, TList}
import com.taskadapter.connector.common.data.ConnectorConverter
import com.taskadapter.model.{Description, DueDate, GTask, Summary}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

class GTaskToTrello(config:TrelloConfig, listCache: ListCache) extends ConnectorConverter[GTask, Card] {
  val logger = LoggerFactory.getLogger(classOf[GTaskToTrello])

  override def convert(source: GTask): Card = {
    val card = new Card
    card.setId(source.getKey)
    card.setIdBoard(config.boardId)
    source.getFields.asScala.foreach { e =>
      val field = e._1
      val value = e._2
      field match {
        case TrelloField.listId =>
          card.setIdList(value.asInstanceOf[String])
        case TrelloField.listName =>
          val listName = value.asInstanceOf[String]
          val listId = listCache.getListIdByName(listName)
          card.setIdList(listId)
        case Summary => card.setName(value.asInstanceOf[String])
        case Description => card.setDesc(value.asInstanceOf[String])
        case DueDate => card.setDue(value.asInstanceOf[Date])
        case _ => logger.warn(s"Unknown field in GTask: $field. Skipping it")
      }
    }
    card
  }
}
