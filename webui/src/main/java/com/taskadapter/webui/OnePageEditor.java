package com.taskadapter.webui;

import com.taskadapter.config.ConnectorDataHolder;
import com.taskadapter.connector.definition.ConnectorConfig;
import com.taskadapter.web.PluginEditorFactory;
import com.taskadapter.web.WindowProvider;
import com.taskadapter.web.configeditor.MiniPanel;
import com.taskadapter.web.service.Services;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Panel;

public class OnePageEditor extends GridLayout implements WindowProvider {
    private static final int COLUMNS_NUMBER = 2;

    private String connector1Id;
    private ConnectorConfig connector1Config;
    private String connector2Id;
    private ConnectorConfig connector2Config;
    private Services services;

    public OnePageEditor(Services services, ConnectorDataHolder leftConnectorDataHolder, ConnectorDataHolder rightConnectorDataHolder) {
        this.services = services;
        this.connector1Id = leftConnectorDataHolder.getType();
        this.connector1Config = leftConnectorDataHolder.getData();
        this.connector2Id = rightConnectorDataHolder.getType();
        this.connector2Config = rightConnectorDataHolder.getData();
        buildUI();
    }

    private void buildUI() {
        setColumns(COLUMNS_NUMBER);
        setRows(2);
        setWidth(700, UNITS_PIXELS);
        addComponent(createMiniPanel(connector1Id, connector1Config));
        addComponent(createMiniPanel(connector2Id, connector2Config));
        addComponent(addOnePageMappingPanel(), 0, 1, 1, 1);
    }

    private Panel createMiniPanel(String connectorId, ConnectorConfig connectorConfig) {
        MiniPanel miniPanel = new MiniPanel(this, connectorId, connectorConfig);
        PluginEditorFactory editorFactory = services.getEditorManager().getEditorFactory(connectorId);
        ComponentContainer container = editorFactory.getMiniPanelContents(this, connectorConfig);
        miniPanel.setPanelContents(container);

        Panel panel = new Panel(miniPanel);
        panel.setCaption(connectorId);
        return panel;
    }

    private OnePageMappingPanel addOnePageMappingPanel() {
        PluginEditorFactory editor1Factory = services.getEditorManager().getEditorFactory(connector1Id);
        PluginEditorFactory editor2Factory = services.getEditorManager().getEditorFactory(connector2Id);

        return new OnePageMappingPanel(connector1Id, editor1Factory.getAvailableFields(), connector1Config.getFieldMappings(),
                connector2Id, editor2Factory.getAvailableFields(), connector2Config.getFieldMappings());
    }
}
