package com.taskadapter.webui.export


import com.taskadapter.vaadin14shim.VerticalLayout
import com.taskadapter.vaadin14shim.HorizontalLayout
import com.taskadapter.vaadin14shim.GridLayout
import java.util

import com.taskadapter.config.StorageException
import com.taskadapter.model.GTask
import com.taskadapter.web.event.{ConfigSaveRequested, EventBusImpl}
import com.taskadapter.web.uiapi.UISyncConfig
import com.taskadapter.webui.Page
import com.taskadapter.webui.action.MyTree
import com.taskadapter.webui.config.TaskFieldsMappingFragment
import com.vaadin.ui._
import org.slf4j.LoggerFactory

object ConfirmExportFragment {
  private val LOGGER = LoggerFactory.getLogger(ConfirmExportFragment.getClass)

  /**
    * Confirm export fragment callbacks.
    */
  trait Callback {
    /**
      * Notifies about selected tasks.
      *
      * @param selectedTasks selected tasks.
      */
    def onTasks(selectedTasks: util.List[GTask]): Unit

    /**
      * Notifies about process cancellation.
      */
    def onCancel(): Unit
  }

  /**
    * Renders export confirmation fragment.
    *
    * @return confirmation dialog.
    */
  def render(config: UISyncConfig,
             initialTasks: util.List[GTask], callback: Callback): Component = {
    val resolver = config.getPreviouslyCreatedTasksResolver()
    val layout = new VerticalLayout
    layout.setSpacing(true)
    val destinationLocation = config.getConnector2.getDestinationLocation
    val destinationWithDecoration = destinationLocation + " (" + config.getConnector2.getConnectorTypeId + ")"
    val text1 = new Label(Page.message("exportConfirmation.pleaseConfirm", destinationWithDecoration))
    layout.add(text1)
    val connectorTree = new MyTree(resolver, initialTasks, destinationLocation)
    connectorTree.setSizeFull()
    layout.add(connectorTree)
    val buttonsLayout = new HorizontalLayout
    val goButton = new Button(Page.message("button.go"))
    buttonsLayout.add(goButton)
    val backButton = new Button(Page.message("button.cancel"))
    backButton.addClickListener(_ => callback.onCancel())
    buttonsLayout.add(backButton)
    layout.add(buttonsLayout)
    val taskFieldsMappingFragment = new TaskFieldsMappingFragment(Page.MESSAGES,
      config.getConnector1.getAllFields, config.getConnector1.fieldNames, config.getConnector1.getLabel,
      config.getConnector2.getAllFields, config.getConnector2.fieldNames, config.getConnector2.getLabel,
      config.getNewMappings)

    def getPossiblyUpdatedConfig = {
      val newFieldMappings = taskFieldsMappingFragment.getElements.toSeq
      config.copy(fieldMappings = newFieldMappings)
    }
    layout.add(taskFieldsMappingFragment.getUI)
    goButton.addClickListener(_ => {
      try {
        EventBusImpl.post(ConfigSaveRequested(getPossiblyUpdatedConfig))
      } catch {
        case e: StorageException =>
          LOGGER.error(Page.message("action.cantSaveUpdatedConfig", e.getMessage), e)
      }
      callback.onTasks(connectorTree.getSelectedRootLevelTasks)
    })
    layout
  }
}
