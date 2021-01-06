package com.taskadapter.webui.config

import com.taskadapter.vaadin14shim.VerticalLayout
import com.taskadapter.vaadin14shim.HorizontalLayout
import com.taskadapter.PluginManager
import com.taskadapter.connector.definition.ConnectorSetup
import com.taskadapter.web.PluginEditorFactory
import com.taskadapter.web.event.{EventBusImpl, ShowSetupsListPageRequested}
import com.taskadapter.web.service.Sandbox
import com.taskadapter.web.uiapi.SetupId
import com.taskadapter.webui.service.EditorManager
import com.taskadapter.webui.{ConfigOperations, Page}
import com.vaadin.ui._

class EditSetupPage(configOperations: ConfigOperations, editorManager: EditorManager, pluginManager: PluginManager,
                    sandbox: Sandbox, setupId: SetupId) {
  val layout = new VerticalLayout
  layout.setSpacing(true)

  val saveButton = new Button(Page.message("editSetupPage.saveButton"))
  saveButton.addClickListener(_ => saveClicked())

  val closeButton = new Button(Page.message("editSetupPage.closeButton"))
  closeButton.addClickListener(_ => EventBusImpl.post(ShowSetupsListPageRequested()))

  val ui = layout

  val setup: ConnectorSetup = configOperations.getSetup(setupId)

  val editor: PluginEditorFactory[_, ConnectorSetup] = editorManager.getEditorFactory(setup.connectorId)
  val editSetupPanel = editor.getEditSetupPanel(sandbox, setup)
  layout.add(editSetupPanel.getUI)
  layout.add(new HorizontalLayout(saveButton, closeButton))

  def saveClicked(): Unit = {
    val maybeError = editSetupPanel.validate
    if (maybeError.isEmpty) {
      configOperations.saveSetup(editSetupPanel.getResult, setupId)
      EventBusImpl.post(ShowSetupsListPageRequested())
    } else {
      editSetupPanel.showError(maybeError.get)
    }
  }
}
