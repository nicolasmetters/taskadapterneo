package com.taskadapter.connector.common

import java.io.File

import org.slf4j.LoggerFactory

object FileNameGenerator {
  val logger = LoggerFactory.getLogger(FileNameGenerator.getClass)

  /**
    * Search for an unused file name in the given folder starting with suffix 1.
    *
    * @param rootFolder folder to generate a file name for
    * @param format     sample: `MSP_export_%d.xml`
    * @return
    */
  def createSafeAvailableFile(rootFolder: File, format: String): File = {
    createSafeAvailableFile(rootFolder, format, 10000)
  }

  def createSafeAvailableFile(rootFolder: File, format: String, numberOfTries: Int): File = {

    val safeFormat = makeFileNameDiskSafe(format)
    var number = 1
    rootFolder.mkdirs
    while ( {
      number < numberOfTries // give a chance to exit
    }) {
      val identifier = System.currentTimeMillis()
      val file = new File(rootFolder, safeFormat.format(identifier))
      logger.debug(s"Checking if file name ${file.getAbsolutePath} is available...")
      if (!file.exists) return file

      number += 1
    }
    throw new RuntimeException(s"cannot generate available file name after $numberOfTries attempts")
  }

  def makeFileNameDiskSafe(potentialFileName: String): String = {
    potentialFileName.replace(" ", "_")
  }
}
