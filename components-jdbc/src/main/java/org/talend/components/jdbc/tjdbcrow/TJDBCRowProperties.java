// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.jdbc.tjdbcrow;

import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avro.Schema;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.api.properties.ComponentReferencePropertiesEnclosing;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.JDBCConnectionInfoProperties;
import org.talend.components.jdbc.ReferAnotherComponent;
import org.talend.components.jdbc.module.JDBCConnectionModule;
import org.talend.components.jdbc.module.JDBCTableSelectionModule;
import org.talend.components.jdbc.module.PreparedStatementTable;
import org.talend.components.jdbc.runtime.sqlbuilder.JDBCSQLBuilder;
import org.talend.components.jdbc.tjdbcconnection.TJDBCConnectionDefinition;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class TJDBCRowProperties extends FixedConnectorsComponentProperties
        implements ComponentReferencePropertiesEnclosing, JDBCConnectionInfoProperties, ReferAnotherComponent {

    public TJDBCRowProperties(String name) {
        super(name);
    }

    // main
    public ComponentReferenceProperties referencedComponent = new ComponentReferenceProperties("referencedComponent", this);

    public JDBCConnectionModule connection = new JDBCConnectionModule("connection");

    public transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "main");

    public transient PropertyPathConnector FLOW_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "schemaFlow");

    public transient PropertyPathConnector REJECT_CONNECTOR = new PropertyPathConnector(Connector.REJECT_NAME, "schemaReject");

    public SchemaProperties main = new SchemaProperties("main") {

        @SuppressWarnings("unused")
        public void afterSchema() {
            updateOutputSchemas();
            updateUseColumn();
        }

    };

    public SchemaProperties schemaFlow = new SchemaProperties("schemaFlow");

    public SchemaProperties schemaReject = new SchemaProperties("schemaReject");

    public JDBCTableSelectionModule tableSelection = new JDBCTableSelectionModule("tableSelection");

    // TODO query type

    public Property<String> sql = PropertyFactory.newString("sql").setRequired(true);

    public final PresentationItem guessQueryFromSchema = new PresentationItem("guessQueryFromSchema", "Guess query from schema");

    public Property<Boolean> useDataSource = PropertyFactory.newBoolean("useDataSource").setRequired();

    public Property<String> dataSource = PropertyFactory.newProperty("dataSource").setRequired();

    public Property<Boolean> dieOnError = PropertyFactory.newBoolean("dieOnError").setRequired();

    // advanced
    public Property<Boolean> propagateQueryResultSet = PropertyFactory.newBoolean("propagateQueryResultSet").setRequired();

    public Property<String> useColumn = newString("useColumn");

    public Property<Boolean> usePreparedStatement = PropertyFactory.newBoolean("usePreparedStatement").setRequired();

    public PreparedStatementTable preparedStatementTable = new PreparedStatementTable("preparedStatementTable");

    public Property<Integer> commitEvery = PropertyFactory.newInteger("commitEvery").setRequired();

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = CommonUtils.addForm(this, Form.MAIN);

        Widget compListWidget = widget(referencedComponent).setWidgetType(Widget.COMPONENT_REFERENCE_WIDGET_TYPE);
        referencedComponent.componentType.setValue(TJDBCConnectionDefinition.COMPONENT_NAME);
        mainForm.addRow(compListWidget);

        mainForm.addRow(connection.getForm(Form.MAIN));
        mainForm.addRow(main.getForm(Form.REFERENCE));

        mainForm.addRow(tableSelection.getForm(Form.REFERENCE));
        mainForm.addRow(sql);

        mainForm.addRow(Widget.widget(guessQueryFromSchema).setWidgetType(Widget.BUTTON_WIDGET_TYPE));

        mainForm.addRow(useDataSource);
        mainForm.addRow(dataSource);

        mainForm.addRow(dieOnError);

        Form advancedForm = CommonUtils.addForm(this, Form.ADVANCED);

        advancedForm.addRow(propagateQueryResultSet);
        advancedForm.addColumn(widget(useColumn).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));

        advancedForm.addRow(usePreparedStatement);
        advancedForm.addRow(widget(preparedStatementTable).setWidgetType(Widget.TABLE_WIDGET_TYPE));

        advancedForm.addRow(commitEvery);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        commitEvery.setValue(10000);
        tableSelection.setConnection(this);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        String refComponentIdValue = referencedComponent.componentInstanceId.getStringValue();
        boolean useOtherConnection = refComponentIdValue != null
                && refComponentIdValue.startsWith(TJDBCConnectionDefinition.COMPONENT_NAME);

        if (form.getName().equals(Form.MAIN)) {
            form.getChildForm(connection.getName()).setHidden(useOtherConnection);
            if (useOtherConnection) {
                form.getWidget(useDataSource.getName()).setHidden(true);
                form.getWidget(dataSource.getName()).setHidden(true);
            } else {
                form.getWidget(useDataSource.getName()).setHidden(false);
                form.getWidget(dataSource.getName()).setHidden(!useDataSource.getValue());
            }
        }

        if (form.getName().equals(Form.ADVANCED)) {
            form.getWidget(commitEvery.getName()).setHidden(useOtherConnection);

            form.getWidget(useColumn.getName()).setHidden(!propagateQueryResultSet.getValue());
            form.getWidget(preparedStatementTable.getName()).setHidden(!usePreparedStatement.getValue());
        }
    }

    @Override
    public void afterReferencedComponent() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(Form.ADVANCED));
    }

    public void afterUseDataSource() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterPropagateQueryResultSet() {
        refreshLayout(getForm(Form.ADVANCED));
    }

    public void afterUsePreparedStatement() {
        refreshLayout(getForm(Form.ADVANCED));
    }

    @Override
    public JDBCConnectionModule getJDBCConnectionModule() {
        return connection;
    }

    @Override
    public String getReferencedComponentId() {
        return referencedComponent.componentInstanceId.getValue();
    }

    @Override
    public ComponentProperties getReferencedComponentProperties() {
        return referencedComponent.componentProperties;
    }

    public void updateOutputSchemas() {
        Schema inputSchema = main.schema.getValue();

        schemaFlow.schema.setValue(inputSchema);

        final List<Schema.Field> additionalRejectFields = new ArrayList<Schema.Field>();

        Schema.Field field = new Schema.Field("errorCode", Schema.create(Schema.Type.STRING), null, (Object) null);
        field.addProp(SchemaConstants.TALEND_IS_LOCKED, "false");
        field.addProp(SchemaConstants.TALEND_FIELD_GENERATED, "true");
        field.addProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255");
        additionalRejectFields.add(field);

        field = new Schema.Field("errorMessage", Schema.create(Schema.Type.STRING), null, (Object) null);
        field.addProp(SchemaConstants.TALEND_IS_LOCKED, "false");
        field.addProp(SchemaConstants.TALEND_FIELD_GENERATED, "true");
        field.addProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255");
        additionalRejectFields.add(field);

        Schema rejectSchema = newSchema(inputSchema, "rejectOutput", additionalRejectFields);

        schemaReject.schema.setValue(rejectSchema);
    }

    private Schema newSchema(Schema metadataSchema, String newSchemaName, List<Schema.Field> moreFields) {
        Schema newSchema = Schema.createRecord(newSchemaName, metadataSchema.getDoc(), metadataSchema.getNamespace(),
                metadataSchema.isError());

        List<Schema.Field> copyFieldList = new ArrayList<>();
        for (Schema.Field se : metadataSchema.getFields()) {
            Schema.Field field = new Schema.Field(se.name(), se.schema(), se.doc(), se.defaultVal(), se.order());
            field.getObjectProps().putAll(se.getObjectProps());
            for (Map.Entry<String, Object> entry : se.getObjectProps().entrySet()) {
                field.addProp(entry.getKey(), entry.getValue());
            }
            copyFieldList.add(field);
        }

        copyFieldList.addAll(moreFields);

        newSchema.setFields(copyFieldList);
        for (Map.Entry<String, Object> entry : metadataSchema.getObjectProps().entrySet()) {
            newSchema.addProp(entry.getKey(), entry.getValue());
        }

        return newSchema;
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

    public ValidationResult afterGuessQueryFromSchema() {
        String tablename = tableSelection.tablename.getValue();
        Schema schema = main.schema.getValue();
        if (tablename == null || tablename.isEmpty()) {
            return new ValidationResult().setStatus(ValidationResult.Result.ERROR)
                    .setMessage("Please set the table name before it");
        }
        if (schema == null || schema.getFields().isEmpty()) {
            return new ValidationResult().setStatus(ValidationResult.Result.ERROR).setMessage("Please set the schema before it");
        }
        String query = JDBCSQLBuilder.getInstance().generateSQL4SelectTable(tablename, schema);
        sql.setValue("\"" + query + "\"");

        // TODO : it doesn't work
        refreshLayout(getForm(Form.MAIN));

        return ValidationResult.OK;
    }

    public void beforeUseColumn() {
        updateUseColumn();
    }

    private void updateUseColumn() {
        useColumn.setPossibleValues(getFieldNames(main.schema));
    }

    private List<String> getFieldNames(Property<Schema> schema) {
        Schema s = (Schema) schema.getValue();
        List<String> fieldNames = new ArrayList<>();
        for (Schema.Field f : s.getFields()) {
            fieldNames.add(f.name());
        }
        return fieldNames;
    }

}
