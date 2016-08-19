package org.talend.components.fileinput;

import static org.talend.daikon.properties.presentation.Widget.widget;

import java.util.Collections;
import java.util.Set;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

/**
 * The ComponentProperties subclass provided by a component stores the
 * configuration of a component and is used for:
 * 
 * <ol>
 * <li>Specifying the format and type of information (properties) that is
 * provided at design-time to configure a component for run-time,</li>
 * <li>Validating the properties of the component at design-time,</li>
 * <li>Containing the untyped values of the properties, and</li>
 * <li>All of the UI information for laying out and presenting the properties to
 * the user.</li>
 * </ol>
 * 
 * The FileInputProperties has two properties:
 * <ol>
 * <li>{code filename}, a simple property which is a String containing the file
 * path that this component will read.</li>
 * <li>{code schema}, an embedded property referring to a Schema.</li>
 * </ol>
 */
public class FileInputProperties extends FixedConnectorsComponentProperties {

	public Property<String> filename = PropertyFactory.newString("filename"); //$NON-NLS-1$
	public SchemaProperties schema = new SchemaProperties("schema"); //$NON-NLS-1$

	protected transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "schema");

	/*
	 * protected transient PropertyPathConnector FLOW_CONNECTOR = new
	 * PropertyPathConnector(Connector.MAIN_NAME, "schemaFlow");
	 */

	protected transient PropertyPathConnector REJECT_CONNECTOR = new PropertyPathConnector(Connector.REJECT_NAME,
			"schemaReject");

	public FileInputProperties(String name) {
		super(name);
	}

	@Override
	public void setupProperties() {
		super.setupProperties();
		// Code for property initialization goes here
	}

	@Override
	public void setupLayout() {
		super.setupLayout();
		Form form = Form.create(this, Form.MAIN);
		form.addRow(schema.getForm(Form.REFERENCE));
		form.addRow(widget(filename).setWidgetType(Widget.FILE_WIDGET_TYPE));
	}

	@Override
	protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputComponent) {
		if (isOutputComponent) {
			return Collections.singleton(MAIN_CONNECTOR);
		}
		return Collections.emptySet();
	}

}
