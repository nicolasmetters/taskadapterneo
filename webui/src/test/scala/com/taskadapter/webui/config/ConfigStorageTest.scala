package com.taskadapter.webui.config

import com.taskadapter.config.ConfigStorage
import com.taskadapter.web.uiapi.SetupId
import com.taskadapter.webui.uiapi.ConfigsTempFolder
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSpec, Matchers}

import java.util.Optional

@RunWith(classOf[JUnitRunner])
class ConfigStorageTest extends FunSpec with Matchers with ConfigsTempFolder {
  private val configName = "some_config_name"
  private val login = "autotest"

  it("config can be created in storage") {
    withTempFolder { folder =>
      val storage = new ConfigStorage(folder)
      val configId = storage.createNewConfig(login, configName, "jira", new SetupId("web1"), "value1",
        "jira", new SetupId("web2"), "value2", "mappings")
      val config = storage.getConfig(configId)
      config.isPresent shouldBe true
      config.get.getMappingsString shouldBe "mappings"
    }
  }

  it("config is deleted") {
    withTempFolder { folder =>
      val storage = new ConfigStorage(folder)
      val configId = storage.createNewConfig(login, configName, "jira", new SetupId("web"), "value1",
        "jira", new SetupId("web"), "value2", "mappings")
      storage.deleteConfig(configId)
      storage.getConfig(configId) shouldBe Optional.empty()
    }
  }
}
