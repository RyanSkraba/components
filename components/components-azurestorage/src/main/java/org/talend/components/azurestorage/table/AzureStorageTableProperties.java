// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.azurestorage.table;

import static org.talend.daikon.properties.presentation.Widget.widget;

import java.util.HashSet;
import java.util.Set;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.ISchemaListener;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.azurestorage.AzureStorageProvideConnectionProperties;
import org.talend.components.azurestorage.table.helpers.NameMappingTable;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class AzureStorageTableProperties extends FixedConnectorsComponentProperties
        implements AzureStorageProvideConnectionProperties {

    private static final long serialVersionUID = -5089410955960231243L;

    // for wizard
    public static final String FORM_WIZARD = "Wizard";

    public Property<String> name = PropertyFactory.newString("name").setRequired();

    public PresentationItem preview = new PresentationItem("preview", "Preview");

    public Property<String> previewTable = PropertyFactory.newString("previewTable");

    // main properties

    public static final String TABLE_PARTITION_KEY = "PartitionKey";

    public static final String TABLE_ROW_KEY = "RowKey";

    public static final String TABLE_TIMESTAMP = "Timestamp";

    public TAzureStorageConnectionProperties connection = new TAzureStorageConnectionProperties("connection");

    public Property<String> tableName = PropertyFactory.newString("tableName").setRequired();

    public Property<Boolean> dieOnError = PropertyFactory.newBoolean("dieOnError");

    public NameMappingTable nameMapping = new NameMappingTable("nameMapping");

    protected transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "schema");

    protected transient PropertyPathConnector FLOW_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "schemaFlow");

    protected transient PropertyPathConnector REJECT_CONNECTOR = new PropertyPathConnector(Connector.REJECT_NAME, "schemaReject");

    public SchemaProperties schemaFlow = new SchemaProperties("schemaFlow"); //$NON-NLS-1$

    public SchemaProperties schemaReject = new SchemaProperties("schemaReject"); //$NON-NLS-1$

    public ISchemaListener schemaListener;

    public SchemaProperties schema = new SchemaProperties("schema") {

        private static final long serialVersionUID = 4803323553282704087L;

        @SuppressWarnings("unused")
        public void afterSchema() {
            if (schemaListener != null) {
                schemaListener.afterSchema();
            }
        }

    };

    public AzureStorageTableProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form main = new Form(this, Form.MAIN);
        main.addRow(connection.getForm(Form.REFERENCE));
        main.addRow(tableName);
        main.addRow(schema.getForm(Form.REFERENCE));

        Form advancedForm = new Form(this, Form.ADVANCED);
        advancedForm.addRow(widget(nameMapping).setWidgetType(Widget.TABLE_WIDGET_TYPE));

        Form wizardForm = Form.create(this, FORM_WIZARD);
        wizardForm.addRow(name);
        wizardForm.addRow(tableName);
        wizardForm.addRow(widget(preview).setLongRunning(true).setWidgetType(Widget.BUTTON_WIDGET_TYPE));
        wizardForm.addRow(widget(previewTable).setWidgetType(Widget.JSON_TABLE_WIDGET_TYPE));

    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        dieOnError.setValue(true);
        tableName.setValue("");
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        for (Form childForm : connection.getForms()) {
            connection.refreshLayout(childForm);
        }
    }

    @Override
    public TAzureStorageConnectionProperties getConnectionProperties() {
        return connection.getConnectionProperties();
    }

    public void setSchemaListener(ISchemaListener schemaListener) {
        this.schemaListener = schemaListener;
    }

    public ValidationResult validateNameMapping() {
        return nameMapping.validateNameMappings();
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        HashSet<PropertyPathConnector> connectors = new HashSet<>();
        if (isOutputConnection) {
            connectors.add(FLOW_CONNECTOR);
            connectors.add(REJECT_CONNECTOR);
        } else {
            connectors.add(MAIN_CONNECTOR);
        }
        return connectors;
    }
}
