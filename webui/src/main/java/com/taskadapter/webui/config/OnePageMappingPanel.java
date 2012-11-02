package com.taskadapter.webui.config;

import com.google.common.collect.ImmutableSet;
import com.taskadapter.connector.definition.AvailableFields;
import com.taskadapter.connector.definition.FieldMapping;
import com.taskadapter.connector.definition.NewMappings;
import com.taskadapter.connector.definition.exceptions.BadConfigException;
import com.taskadapter.model.GTaskDescriptor;
import com.taskadapter.model.GTaskDescriptor.FIELD;
import com.taskadapter.web.configeditor.Validatable;
import com.taskadapter.web.data.Messages;
import com.taskadapter.web.uiapi.UIConnectorConfig;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;

import java.util.Arrays;

public class OnePageMappingPanel extends Panel implements Validatable {
    private static final int COLUMN_DESCRIPTION = 0;
    private static final int COLUMN_HELP = 1;
    private static final int COLUMN_LEFT_CONNECTOR = 2;
    private static final int COLUMN_RIGHT_CONNECTOR = 3;
    private static final int COLUMNS_NUMBER = 4;

    // TODO merge this help file with all the other localized strings
    private static final String BUNDLE_NAME = "help";
    private static final com.taskadapter.web.data.Messages HELP_MESSAGES = new com.taskadapter.web.data.Messages(BUNDLE_NAME);
    private static final Resource HELP_ICON_RESOURCE = new ThemeResource("../runo/icons/16/help.png");

    private GridLayout gridLayout;

    private Messages messages;
    private UIConnectorConfig connector1;
    private UIConnectorConfig connector2;
    private NewMappings originalMappings;
    private NewMappings mappings;

    public OnePageMappingPanel(Messages messages, UIConnectorConfig connector1,
                               UIConnectorConfig connector2, NewMappings mappings) {
        super(messages.get("editConfig.mappings.caption"));
        this.messages = messages;
        this.connector1 = connector1;
        this.connector2 = connector2;
        this.mappings = mappings;
        this.originalMappings = new NewMappings(mappings.getMappings());

        addFields();
    }

    private void addFields() {
        createGridLayout();
        addTableHeaders();
        addSupportedFields();
    }

    private void createGridLayout() {
        gridLayout = new GridLayout();
        gridLayout.setMargin(true);
        gridLayout.setSpacing(true);
        gridLayout.setRows(GTaskDescriptor.FIELD.values().length + 2);
        gridLayout.setColumns(COLUMNS_NUMBER);
        addComponent(gridLayout);
    }

    private void addTableHeaders() {
        Label label2 = new Label(messages.get("editConfig.mappings.exportFieldHeader"));
        label2.addStyleName("fieldsTitle");
        label2.setWidth(50, UNITS_PIXELS);
        gridLayout.addComponent(label2, COLUMN_DESCRIPTION, 0);
        gridLayout.setComponentAlignment(label2, Alignment.MIDDLE_LEFT);

        Label label = new Label(" ");
        label.setWidth(20, UNITS_PIXELS);
        gridLayout.addComponent(label, COLUMN_HELP, 0);

        Label label1 = new Label(connector1.getLabel());
        label1.addStyleName("fieldsTitle");
        label1.setWidth(135, UNITS_PIXELS);
        gridLayout.addComponent(label1, COLUMN_LEFT_CONNECTOR, 0);


        Label label3 = new Label(connector2.getLabel());
        label3.addStyleName("fieldsTitle");
        gridLayout.addComponent(label3, COLUMN_RIGHT_CONNECTOR, 0);

        gridLayout.addComponent(new Label("<hr>", Label.CONTENT_XHTML), 0, 1, COLUMNS_NUMBER - 1, 1);
    }

    private void addSupportedFields() {
        for (FieldMapping mapping : mappings.getMappings()) {
            addField(mapping);
        }
    }

    private void addField(FieldMapping field) {
        addCheckbox(field);
        String helpForField = getHelpForField(field);
        if (helpForField != null) {
            addHelp(helpForField);
        } else {
            addEmptyCell();
        }
        String idFieldDisplayValue = GTaskDescriptor.getDisplayValue(FIELD.ID);
        if (field.getConnector1() == null) {
            createMappingForSingleValue(idFieldDisplayValue);
        } else {
            addConnectorField(connector1.getAvailableFields(), field, "connector1");
        }

        if (field.getConnector2() == null) {
            createMappingForSingleValue(idFieldDisplayValue);
        } else {
            addConnectorField(connector2.getAvailableFields(), field, "connector2");
        }
    }

    private void addCheckbox(FieldMapping field) {
        CheckBox checkbox = new CheckBox();
        final MethodProperty<Boolean> selected = new MethodProperty<Boolean>(field, "selected");
        checkbox.setPropertyDataSource(selected);
        gridLayout.addComponent(checkbox);
        gridLayout.setComponentAlignment(checkbox, Alignment.MIDDLE_CENTER);
    }

    private void addHelp(String helpForField) {
        Embedded helpIcon = new Embedded(null, HELP_ICON_RESOURCE);
        helpIcon.setDescription(helpForField);
        gridLayout.addComponent(helpIcon);
        gridLayout.setComponentAlignment(helpIcon, Alignment.MIDDLE_CENTER);
    }

    private void addEmptyCell() {
        Label emptyLabel = new Label(" ");
        gridLayout.addComponent(emptyLabel);
        gridLayout.setComponentAlignment(emptyLabel, Alignment.MIDDLE_LEFT);
    }

    private void addConnectorField(AvailableFields connectorFields, FieldMapping fieldMapping, String leftRightField) {
        String[] allowedValues = connectorFields.getAllowedValues(fieldMapping.getField());
        BeanItemContainer<String> container = new BeanItemContainer<String>(String.class);
        final MethodProperty<String> mappedTo = new MethodProperty<String>(fieldMapping, leftRightField);

        if (connectorFields.isFieldSupported(fieldMapping.getField())) {
            if (allowedValues.length > 1) {
                container.addAll(Arrays.asList(allowedValues));
                ComboBox combo = new ComboBox(null, container);
                combo.setPropertyDataSource(mappedTo);
                combo.setWidth(160, UNITS_PIXELS);
                gridLayout.addComponent(combo);
                gridLayout.setComponentAlignment(combo, Alignment.MIDDLE_LEFT);
                Object currentValue = mappedTo.getValue();
                combo.select(currentValue);
            } else if (allowedValues.length == 1) {
                createMappingForSingleValue(allowedValues[0]);
            } else {
                final String displayValue = GTaskDescriptor
                        .getDisplayValue(fieldMapping.getField());
                createMappingForSingleValue(displayValue);
            }
        } else {
            addEmptyCell();
        }
    }

    private void createMappingForSingleValue(String displayValue) {
        Label label = new Label(displayValue);
        gridLayout.addComponent(label);
        gridLayout.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
    }

    String getHelpForField(FieldMapping field) {
        String elementId = field.getField().toString();
        return HELP_MESSAGES.getNoDefault(elementId);
    }

    @Override
    public void validate() throws BadConfigException {
        MappingsValidator.validate(mappings);
    }

    /**
     * Checks, if there were any changes in the mappings made by the user since this page was created.
     *
     * @return <code>true</code> if there are changes since panel creation.
     */
    public boolean hasChanges() {
        // TODO !! Add unit tests here. this comparison seems too complex.
        ImmutableSet<FieldMapping> original = ImmutableSet.copyOf(originalMappings.getMappings());
        ImmutableSet<FieldMapping> current = ImmutableSet.copyOf(mappings.getMappings());
        return !original.equals(current);
    }
}