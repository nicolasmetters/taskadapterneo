package com.taskadapter.connector.msp

import java.util

import com.google.gson.{JsonElement, JsonParseException}
import com.taskadapter.connector.Field
import com.taskadapter.connector.common.ConfigUtils
import com.taskadapter.connector.definition.{Descriptor, FileSetup, PluginFactory}
import com.taskadapter.model.StandardField

import scala.collection.JavaConverters._

class MSPFactory extends PluginFactory[MSPConfig, FileSetup] {
  private val DESCRIPTOR = Descriptor(MSPConnector.ID, MSPConfig.DEFAULT_LABEL)

  override def getAvailableFields: util.List[Field] = MspField.fields.asJava

  override def getSuggestedCombinations: Map[Field, StandardField] = MspField.getSuggestedCombinations()

  override def createConnector(config: MSPConfig, setup: FileSetup) = new MSPConnector(setup)

  override def getDescriptor = DESCRIPTOR

  override def writeConfig(config: MSPConfig): JsonElement = ConfigUtils.createDefaultGson.toJsonTree(config)

  @throws[JsonParseException]
  override def readConfig(config: JsonElement): MSPConfig = ConfigUtils.createDefaultGson.fromJson(config, classOf[MSPConfig])

  override def createDefaultConfig = new MSPConfig
}

