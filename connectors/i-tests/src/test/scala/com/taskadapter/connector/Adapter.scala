package com.taskadapter.connector

import com.taskadapter.connector.common.ProgressMonitorUtils
import com.taskadapter.connector.definition.SaveResult
import com.taskadapter.core.TaskKeeper

import scala.collection.JavaConverters._

class Adapter(taskKeeper: TaskKeeper, connector1: NewConnector, connector2: NewConnector) {

  def adapt(rows: List[FieldRow]): SaveResult = {
    val tasks = connector1.loadData()
    val result = connector2.saveData(Map[String, Long](), tasks, ProgressMonitorUtils.DUMMY_MONITOR, rows.asJava)
    result
  }

}
