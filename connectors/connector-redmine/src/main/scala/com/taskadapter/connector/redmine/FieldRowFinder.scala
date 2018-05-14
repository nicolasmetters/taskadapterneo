package com.taskadapter.connector.redmine

import com.taskadapter.connector.FieldRow
import com.taskadapter.model.Field

object FieldRowFinder {
  def containsTargetField(fieldRows: Seq[FieldRow[_]], field: Field[_]): Boolean = {
    fieldRows.foreach { row =>
      val target = row.targetField
      if (target.isDefined && target.get.name.equals(field.name)) {
        return true
      }
    }
    false
  }
}
