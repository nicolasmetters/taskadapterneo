package com.taskadapter.webui.pages

import com.taskadapter.Constants
import com.taskadapter.connector.definition.FieldMapping
import com.taskadapter.connector.definition.exception.FieldNotMappedException
import com.taskadapter.connector.definition.exceptions.BadConfigException
import com.taskadapter.license.LicenseManager
import com.taskadapter.web.service.Sandbox
import com.taskadapter.web.uiapi.{ConfigId, UIConnectorConfig, UISyncConfig}
import com.taskadapter.webui.pages.config.{FieldMappingPanel, ResultsPanel}
import com.taskadapter.webui.service.Preservices
import com.taskadapter.webui.{BasePage, ConfigActionsFragment, ConfigOperations, ImageLoader, Layout, Page, SessionController, Sizes}
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dependency.CssImport
import com.vaadin.flow.component.html.{Div, Label}
import com.vaadin.flow.component.orderedlayout.{FlexComponent, HorizontalLayout, VerticalLayout}
import com.vaadin.flow.component.tabs.{Tab, Tabs}
import com.vaadin.flow.component.{ClickEvent, Component, ComponentEventListener}
import com.vaadin.flow.router.{BeforeEvent, HasUrlParameter, Route}
import org.slf4j.LoggerFactory

import scala.collection.mutable

@Route(value = "config", layout = classOf[Layout])
@CssImport(value = "./styles/views/mytheme.css")
class ConfigPage() extends BasePage with HasUrlParameter[String] {
  private val configOps: ConfigOperations = SessionController.buildConfigOperations()
  private val services: Preservices = SessionController.getServices
  private val sandbox: Sandbox = SessionController.createSandbox()

  def setParameter(event: BeforeEvent, configIdStr: String) = {
    removeAll()

    val configId = ConfigId(SessionController.getCurrentUserName, Integer.parseInt(configIdStr))
    val maybeConfig = configOps.getConfig(configId)
    if (maybeConfig.isDefined) {
      val config = maybeConfig.get
      val panel = new ConfigPanel(config, configOps, services, sandbox)
      panel.overviewPanel.showInitialState()
      add(LayoutsUtil.centered(Sizes.mainWidth,
        panel)
      )
    }
  }
}

/**
  * Config panel with left/right arrows, connector names, action buttons (Delete/Clone/etc).
  */
class ConfigPanel(config: UISyncConfig,
                  configOps: ConfigOperations,
                  services: Preservices,
                  sandbox: Sandbox) extends VerticalLayout {
  private val log = LoggerFactory.getLogger(classOf[ConfigPanel])

  private val configId = config.configId

  val configTitleLine = new Label(config.label)
  add(configTitleLine)

  val previousResultsPanel = new ResultsPanel(services, configId)
  val fieldMappingsPanel = new FieldMappingPanel(config, configOps)
  val overviewPanel = new OverviewPanel(config)

  createTabs()

  private val rightButton = createArrow("arrow_right.png", _ => sync(config))
  private val leftButton = createArrow("arrow_left.png", _ => sync(config.reverse))


  private def createTabs(): Unit = {

    val overviewTab = new Tab("Overview")
    val mappingsTab = new Tab("Field mappings")
    val resultsTab = new Tab("Results")

    val tabsToPages = mutable.Map[Tab, Component]()

    tabsToPages += (overviewTab -> overviewPanel)
    tabsToPages += (mappingsTab -> fieldMappingsPanel)
    tabsToPages += (resultsTab -> previousResultsPanel)

    overviewPanel.setVisible(true)
    fieldMappingsPanel.setVisible(false)
    previousResultsPanel.setVisible(false)

    val tabs = new Tabs(
      overviewTab,
      mappingsTab,
      resultsTab
    )
    tabs.setOrientation(Tabs.Orientation.HORIZONTAL)
    tabs.addSelectedChangeListener(event => {
      tabsToPages.values.foreach(page => page.setVisible(false))
      val selectedPage = tabsToPages.get(tabs.getSelectedTab()).get
      selectedPage.setVisible(true);
    })
    overviewTab.setSelected(true)
    add(tabs,
      new Div(overviewPanel, fieldMappingsPanel, previousResultsPanel))

  }

  def updateConfigTitleLine(config: UISyncConfig): Unit = {
    configTitleLine.setText(config.label)
  }

  def createArrow(imageFileName: String, listener: ComponentEventListener[ClickEvent[Button]]): Button = {
    val leftArrow = ImageLoader.getImage(imageFileName)
    val button = new Button(leftArrow)
    button.setHeight("40px")
    button.setWidth("100px")
    button.getElement.setProperty("title", Page.message("export.exportButtonTooltip"))
    button.addClickListener(listener)
    button
  }

  def loadConfig(): UISyncConfig = {
    val maybeConfig = configOps.getConfig(configId)
    if (maybeConfig.isEmpty) {
      throw new RuntimeException(s"Config with id $configId is not found")
    }
    maybeConfig.get
  }

  /**
    * Performs a synchronization operation from first connector to second.
    *
    * @param config base config. May be saved!
    */
  private def sync(config: UISyncConfig): Unit = {
    exportCommon(config)
  }

  class OverviewPanel(config: UISyncConfig) extends VerticalLayout {
    val height = "100px"

    val horizontalLayout = new HorizontalLayout
    horizontalLayout.setHeight(height)
    horizontalLayout.setPadding(true)

    val validationPanelSaveToRight = new ValidationMessagesPanel(
      Page.message("configSummary.validationPanelCaption", config.getConnector2.getLabel))
    val validationPanelSaveToLeft = new ValidationMessagesPanel(
      Page.message("configSummary.validationPanelCaption", config.getConnector1.getLabel))

    private def showEditConnectorDialog(connectorConfig: UIConnectorConfig,
                                        configSaver: Runnable, sandbox: Sandbox): Unit = {
      val systemPanel = connectorConfig.createMiniPanel(sandbox)
      val dialog = ModalWindow.showDialog(systemPanel.getComponent)
      dialog.addDialogCloseActionListener(_ => {
        // save the fields from the component into the original bean
        systemPanel.save()

        // save the config to disk
        configSaver.run()

        dialog.close()
      })
    }

    private val configSaver = new Runnable {
      override def run(): Unit = {
        // TODO 14 check that the new config is used here
        configOps.saveConfig(config)
        updateConfigTitleLine(config)
        recreateContents(config)
      }
    }

    private def recreateContents(config: UISyncConfig): Unit = {

      val leftConnectorEditListener = new Runnable {
        override def run(): Unit = showEditConnectorDialog(config.getConnector1, configSaver, sandbox)
      }
      val leftSystemButton = createConfigureConnectorButton(config.connector1, leftConnectorEditListener)

      val leftRightButtonsPanel = new VerticalLayout()
      leftRightButtonsPanel.add(rightButton)
      leftRightButtonsPanel.add(leftButton)
      leftRightButtonsPanel.setWidth("120px")
      leftRightButtonsPanel.setHeight(height)
      leftRightButtonsPanel.setPadding(false)

      val rightConnectorEditListener = new Runnable {
        override def run(): Unit = showEditConnectorDialog(config.getConnector2, configSaver, sandbox)
      }
      val rightSystemButton = createConfigureConnectorButton(config.connector2, rightConnectorEditListener)

      horizontalLayout.removeAll()
      horizontalLayout.addAndExpand(
        leftSystemButton,
        leftRightButtonsPanel,
        rightSystemButton)

      horizontalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER,
        leftSystemButton,
        leftRightButtonsPanel,
        rightSystemButton)

      performValidation(config, configSaver)
    }

    private def createConfigureConnectorButton(connectorConfig: UIConnectorConfig,
                                               buttonListener: Runnable): Component = {
      val iconResource = ImageLoader.getImage("edit.png")
      val button = new Button(connectorConfig.getLabel)
      button.setIcon(iconResource)
      button.setWidth("400px")
      button.setHeight(height)
      button.addClickListener(_ => buttonListener.run())
      button
    }

    def showInitialState() = {
      removeAll()
      val buttonsLayout = new ConfigActionsFragment(configId)

      add(buttonsLayout,
        horizontalLayout,
        validationPanelSaveToRight.ui,
        validationPanelSaveToLeft.ui)

      recreateContents(config)
    }

    def performValidation(config: UISyncConfig, configSaver: Runnable): Unit = {
      val errorsSaveToLeft = validateSaveToLeft(config, configSaver)
      leftButton.setEnabled(errorsSaveToLeft.isEmpty)
      validationPanelSaveToLeft.show(errorsSaveToLeft)

      val errorsSaveToRight = validateSaveToRight(config, configSaver)
      rightButton.setEnabled(errorsSaveToRight.isEmpty)
      validationPanelSaveToRight.show(errorsSaveToRight)
    }

    def validateSaveToLeft(config: UISyncConfig, configSaver: Runnable): Seq[ValidationErrorTextWithProcessor] = {
      val loadErrors = validateLoad(config.getConnector2, config.fieldMappings, configSaver)
      val saveErrors = validateSave(config.getConnector1, config.fieldMappings, configSaver)
      loadErrors ++ saveErrors
    }

    def validateSaveToRight(config: UISyncConfig, configSaver: Runnable): Seq[ValidationErrorTextWithProcessor] = {
      val loadErrors = validateLoad(config.getConnector1, config.fieldMappings, configSaver)
      val saveErrors = validateSave(config.getConnector2, config.fieldMappings, configSaver)
      loadErrors ++ saveErrors
    }

    def validateSave(uiConfig: UIConnectorConfig, fieldMappings: Seq[FieldMapping[_]], configSaver: Runnable): Seq[ValidationErrorTextWithProcessor] = {
      val errors = uiConfig.validateForSave(fieldMappings)
      errors.map(e => buildItem(uiConfig, e, configSaver))
    }

    def validateLoad(uiConfig: UIConnectorConfig, fieldMappings: Seq[FieldMapping[_]], configSaver: Runnable): Seq[ValidationErrorTextWithProcessor] = {
      val errors = uiConfig.validateForLoad()
      errors.map(e => buildItem(uiConfig, e, configSaver))
    }

    def buildItem(uiConfig: UIConnectorConfig, e: BadConfigException, configSaver: Runnable): ValidationErrorTextWithProcessor = {
      ValidationErrorTextWithProcessor(uiConfig.decodeException(e), buildFixProcessor(uiConfig, e, configSaver))
    }

    private def buildFixProcessor(uiConnectorConfig: UIConnectorConfig, e: BadConfigException, configSaver: Runnable): Runnable =
      () => {
        e match {
          case _: FieldNotMappedException => showConfigEditor(uiConnectorConfig.decodeException(e))
          case _ => showEditConnectorDialog(uiConnectorConfig, configSaver, sandbox)
        }
      }

    private def showConfigEditor(error: String): Unit = {
      // TODO 14 restore "fix connector config" dialogs
//      val window = ModalWindow.showWindow(layout.getUI)
//      val editor = getConfigEditor(loadConfig(), error, () => {
//        window.close()
        // the config may have been changed by the editor. reload it
//        val maybeConfig = configOps.getConfig(configId)
//        if (maybeConfig.isDefined) {
//          recreateContents(maybeConfig.get)
//        }
//      })
//      window.setContent(editor)
    }
  }

  private def exportCommon(config: UISyncConfig): Unit = {
    log.info(
      s"""Starting export
    from ${config.connector1.getConnectorTypeId} (${config.connector1.getSourceLocation})
    to   ${config.connector2.getConnectorTypeId} (${config.connector2.getDestinationLocation}""")

    val maxTasks = if (services.licenseManager.isSomeValidLicenseInstalled) {
      Constants.maxTasksToLoad
    } else {
      LicenseManager.TRIAL_TASKS_NUMBER_LIMIT
    }
    log.info(s"License installed? ${services.licenseManager.isSomeValidLicenseInstalled}")
    val panel = new ExportPage(getUI.get(), services.exportResultStorage, config, maxTasks,
      services.settingsManager.isTAWorkingOnLocalMachine,
      () => overviewPanel.showInitialState(),
      configOps)
    overviewPanel.removeAll()
    overviewPanel.add(panel)
    panel.startLoading()
  }

}