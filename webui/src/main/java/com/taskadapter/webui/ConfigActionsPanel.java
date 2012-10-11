package com.taskadapter.webui;

import com.taskadapter.config.ConnectorDataHolder;
import com.taskadapter.config.TAFile;
import com.taskadapter.connector.definition.MappingSide;
import com.taskadapter.web.service.Services;
import com.taskadapter.webui.export.Exporter;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;

/**
 * Buttons panel with left/right arrows.
 */
public class ConfigActionsPanel extends VerticalLayout {
    private Navigator navigator;
    private TAFile file;
    private Services services;
    private HorizontalLayout horizontalLayout;
    private static final String NO_DESCRIPTION_TEXT = "<i>No description</i>"; //&nbsp;

    public ConfigActionsPanel(Navigator navigator, TAFile file, Services services) {
        this.navigator = navigator;
        this.file = file;
        this.services = services;
        buildUI();
    }

    private void buildUI() {
        addDescription();

        horizontalLayout = new HorizontalLayout();
        horizontalLayout.addStyleName("configs-single-panel-inner");
        horizontalLayout.setSpacing(true);
        addComponent(horizontalLayout);

        createBox(file.getConnectorDataHolder1());
        createActionButtons();
        createBox(file.getConnectorDataHolder2());
    }

    private void addDescription() {
        final String labelText = file.getConfigLabel();
        Label description = new Label(labelText.isEmpty() ? NO_DESCRIPTION_TEXT : labelText, Label.CONTENT_XHTML);
        description.setStyleName("configs-description-label");
        addComponent(description);
        setComponentAlignment(description, Alignment.MIDDLE_CENTER);
    }

    private void createBox(final ConnectorDataHolder dataHolder) {
        final String label = dataHolder.getData().getLabel();
        NativeButton configBoxButton = new NativeButton(label);
        configBoxButton.addStyleName("boxButton");
        configBoxButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                navigator.showConfigureTaskPage(file);
            }
        });
        horizontalLayout.addComponent(configBoxButton);
    }

    private void createActionButtons() {
        VerticalLayout buttonsLayout = new VerticalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.addComponent(createButton(MappingSide.RIGHT));
        buttonsLayout.addComponent(createButton(MappingSide.LEFT));
        horizontalLayout.addComponent(buttonsLayout);
    }

    private Button createButton(MappingSide exportDirection) {
        String imageFile;
        switch (exportDirection) {
            case RIGHT:
                imageFile = "img/arrow_right.png";
                break;
            case LEFT:
                imageFile = "img/arrow_left.png";
                break;
            default:
                throw new IllegalArgumentException();
        }
        Button button = new Button();
        button.setIcon(new ThemeResource(imageFile));
        button.setStyleName(Runo.BUTTON_SMALL);
        button.addStyleName("configsTableArrowButton");

        final Exporter exporter = new Exporter(navigator, services.getPluginManager(), file, exportDirection);
        button.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                exporter.export();
            }
        });
        return button;
    }
}
