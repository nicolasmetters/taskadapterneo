package com.taskadapter.webui

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSpec, Matchers}
import org.slf4j.LoggerFactory

@RunWith(classOf[JUnitRunner])
class LogFinderTest extends FunSpec with Matchers {
  /* ignore: this runs fine in IDEA and in local gradle build, but somehow fails with NPE in Jenkins
     in this line:
     val location = LogFinder.getLogFileLocation
   */
  ignore("finds the log file") {
    // need to init at least one log first because log4j uses lazy init
    LoggerFactory.getLogger(classOf[LogFinderTest])
    println("loaded some log file to trigger Log4J loggers init")
    val location = LogFinder.getLogFileLocation
    location should include("taskadapter.log")
  }
}
