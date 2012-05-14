package com.taskadapter.webui;

import com.taskadapter.config.TAFile;
import com.vaadin.ui.*;

/**
 * @author Alexey Skorokhodov
 */
public class ConfigsPage extends Page {
    public static final int COLUMNS_NUMBER = 3;
    private VerticalLayout layout = new VerticalLayout();
    private GridLayout configsLayout = new GridLayout();
    public static final String SYSTEM_1_TITLE = "System 1";
    public static final String SYSTEM_2_TITLE = "System 2";

    public ConfigsPage() {
        buildUI();
    }

    private void buildUI() {
        layout.setSpacing(true);
        createAddButton();

        configsLayout.setColumns(COLUMNS_NUMBER);
        configsLayout.setSpacing(true);
        layout.addComponent(configsLayout);
        configsLayout.addStyleName("configsTable");
    }

    private void createAddButton() {
        Button addButton = new Button("New config");
        addButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                navigator.show(Navigator.NEW_CONFIG_PAGE);
            }
        });
        layout.addComponent(addButton);
    }

    private void reloadConfigs() {
        configsLayout.removeAllComponents();
        for (TAFile file : services.getConfigStorage().getAllConfigs()) {
            addTask(file);
        }
    }

    private void addTask(final TAFile file) {
        configsLayout.addComponent(new Label(file.getConfigLabel()));
        configsLayout.addComponent(new ConfigToolbarPanel(navigator, file));
        configsLayout.addComponent(new ConfigButtonsPanel(navigator, file, services));
    }

    @Override
    public String getPageTitle() {
        return "Configs";
    }

    @Override
    public Component getUI() {
        reloadConfigs();
        return layout;
    }
}