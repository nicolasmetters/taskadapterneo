package com.taskadapter.web.configeditor;

import com.taskadapter.connector.definition.AvailableFields;
import com.taskadapter.connector.definition.Mappings;
import com.taskadapter.connector.definition.ValidationException;
import com.taskadapter.model.GTaskDescriptor;
import com.taskadapter.util.InternalError;
import com.taskadapter.web.Messages;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;

import java.util.*;

/**
 * @author Alexey Skorokhodov
 */
public class FieldsMappingPanel extends Panel implements Validatable {
	private static final String PANEL_TITLE = "Task fields";
	private static final String COLUMN1_HEADER = "Task Adapter field";
	private static final String COLUMN2_HEADER = "System field or constraint";

	private final AvailableFields availableFields;

	/**
	 * Mappings to edit.
	 */
	private final Mappings mappings;

	/**
	 * Source (original) mappings.
	 */
	private final Mappings originalMappings;

	private static final int COLUMNS_NUMBER = 2;
	private GridLayout gridLayout;
	private Resource helpIconResource = new ThemeResource(
			"../runo/icons/16/help.png");

	public FieldsMappingPanel(AvailableFields availableFieldsProvider,
			Mappings mappings) {
		super("Task fields mapping");
		this.availableFields = availableFieldsProvider;
		this.mappings = mappings;
		this.originalMappings = new Mappings(mappings);

		setDescription("Select fields to export when SAVING data to this system");
		addFields();
		setWidth(DefaultPanel.WIDE_PANEL_WIDTH);
	}

	private void addFields() {
		createGridLayout();
		addTableHeaders();
		addSupportedFields();
	}

	private void createGridLayout() {
		gridLayout = new GridLayout();
		addComponent(gridLayout);
		gridLayout.setMargin(true);
		gridLayout.setSpacing(true);
		Collection<GTaskDescriptor.FIELD> supportedFields = availableFields
				.getSupportedFields();
		gridLayout.setRows(supportedFields.size() + 2);
		gridLayout.setColumns(COLUMNS_NUMBER);
	}

	private void addTableHeaders() {
		Label label1 = new Label(COLUMN1_HEADER);
		label1.addStyleName("fieldsTitle");
		label1.setWidth("135px");
		gridLayout.addComponent(label1, 0, 0);
		Label label2 = new Label(COLUMN2_HEADER);
		label2.addStyleName("fieldsTitle");
		gridLayout.addComponent(label2, 1, 0);
		gridLayout.addComponent(new Label("<hr>", Label.CONTENT_XHTML), 0, 1,
				1, 1);
	}

	private void addSupportedFields() {
		Collection<GTaskDescriptor.FIELD> supportedFields = availableFields
				.getSupportedFields();
		for (GTaskDescriptor.FIELD field : supportedFields) {
			addField(field);
		}
	}

	private void addField(GTaskDescriptor.FIELD field) {
		addCheckbox(field);
		addComboBox(field);
	}

	/**
	 * Adds a combo box.
	 * @param field field to add a combo for.
	 */
	private void addComboBox(GTaskDescriptor.FIELD field) {
		String[] allowedValues = availableFields.getAllowedValues(field);
		BeanItemContainer<String> container = new BeanItemContainer<String>(
				String.class);
		final MethodProperty<String> mappedTo = new MethodProperty<String>(
				String.class, mappings, "getMappedTo", "setMapping",
				new Object[] { field }, new Object[] { field, null }, 1);

		if (allowedValues.length > 1) {
			container.addAll(Arrays.asList(allowedValues));
			ComboBox combo = new ComboBox(null, container);
			combo.setPropertyDataSource(mappedTo);
			combo.setWidth("160px");
			gridLayout.addComponent(combo);
			gridLayout.setComponentAlignment(combo, Alignment.MIDDLE_LEFT);
			combo.select(mappings.getMappedTo(field));
		} else if (allowedValues.length == 1) {
			mappings.setMapping(field, allowedValues[0]);
			Label label = new Label(allowedValues[0]);
			gridLayout.addComponent(label);
			gridLayout.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
		} else {
			throw new InternalError(
					"Mapping for "
							+ field
							+ " defined by a task provider but no mapping values are provided");
		}
	}

	private CheckBox addCheckbox(GTaskDescriptor.FIELD field) {
		CheckBox checkbox = new CheckBox(GTaskDescriptor.getDisplayValue(field));
		final MethodProperty<Boolean> selected = new MethodProperty<Boolean>(
				boolean.class, mappings, "isFieldSelected", "setFieldSelected",
				new Object[] { field }, new Object[] { field, null }, 1);
		checkbox.setPropertyDataSource(selected);

		String helpForField = getHelpForField(field);
		if (helpForField != null) {
			HorizontalLayout layout = addHelpTipToCheckbox(checkbox,
					helpForField);
			gridLayout.addComponent(layout);
			gridLayout.setComponentAlignment(layout, Alignment.MIDDLE_LEFT);
		} else {
			gridLayout.addComponent(checkbox);
			gridLayout.setComponentAlignment(checkbox, Alignment.MIDDLE_LEFT);
		}

		return checkbox;
	}

	private String getHelpForField(GTaskDescriptor.FIELD field) {
		return Messages.getMessageDefaultLocale(field.toString());
	}

	private HorizontalLayout addHelpTipToCheckbox(CheckBox checkbox,
			String helpForField) {
		Embedded helpIcon = new Embedded(null, helpIconResource);
		helpIcon.setDescription(helpForField);
		HorizontalLayout layout = new HorizontalLayout();
		layout.addComponent(checkbox);
		layout.addComponent(helpIcon);
		return layout;
	}

	@Override
	public void validate() throws ValidationException {
		for (GTaskDescriptor.FIELD f : availableFields.getSupportedFields()) {
			if (mappings.isFieldSelected(f) && mappings.getMappedTo(f) == null) {
				throw new ValidationException(getRequiredFieldErrorMessage(f));
			}
		}
	}

	private String getRequiredFieldErrorMessage(GTaskDescriptor.FIELD f) {
		return "Field \"" + GTaskDescriptor.getDisplayValue(f)
				+ "\" is selected for export."
				+ "\nPlease set the *destination* field or constraint in "
				+ PANEL_TITLE + " section.";
	}

	/**
	 * Checks, if there are any changes to perform.
	 * 
	 * @return <code>true</code> iff there are changes since panel creation.
	 */
	public boolean haveChanges() {
		return originalMappings.equals(mappings);
	}
}
