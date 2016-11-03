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
package org.talend.components.jdbc.tjdbcoutput;

import static org.talend.daikon.properties.presentation.Widget.widget;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avro.Schema;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.api.properties.ComponentReferencePropertiesEnclosing;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.RuntimeSettingProvider;
import org.talend.components.jdbc.module.JDBCConnectionModule;
import org.talend.components.jdbc.module.JDBCTableSelectionModule;
import org.talend.components.jdbc.runtime.JDBCSourceOrSink;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.components.jdbc.tjdbcconnection.TJDBCConnectionDefinition;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class TJDBCOutputProperties extends FixedConnectorsComponentProperties
        implements ComponentReferencePropertiesEnclosing, RuntimeSettingProvider {

    public TJDBCOutputProperties(String name) {
        super(name);
    }

    // main
    public ComponentReferenceProperties referencedComponent = new ComponentReferenceProperties("referencedComponent", this);

    public JDBCConnectionModule connection = new JDBCConnectionModule("connection");

    public JDBCTableSelectionModule tableSelection = new JDBCTableSelectionModule("tableSelection");

    public enum DataAction {
        INSERT,
        UPDATE,
        INSERTORUPDATE,
        UPDATEORINSERT,
        DELETE
    }

    public Property<DataAction> dataAction = PropertyFactory.newEnum("dataAction", DataAction.class).setRequired();

    public Property<Boolean> clearDataInTable = PropertyFactory.newBoolean("clearDataInTable").setRequired();

    public transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "main");

    public transient PropertyPathConnector FLOW_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "schemaFlow");

    public transient PropertyPathConnector REJECT_CONNECTOR = new PropertyPathConnector(Connector.REJECT_NAME, "schemaReject");

    public SchemaProperties main = new SchemaProperties("main") {

        @SuppressWarnings("unused")
        public void afterSchema() {
            updateOutputSchemas();
        }

    };

    public SchemaProperties schemaFlow = new SchemaProperties("schemaFlow");

    public SchemaProperties schemaReject = new SchemaProperties("schemaReject");

    public final PresentationItem fetchSchemaFromTable = new PresentationItem("fetchSchemaFromTable", "Fetch schema from table");

    public Property<Boolean> dieOnError = PropertyFactory.newBoolean("dieOnError").setRequired();

    public Property<Boolean> useDataSource = PropertyFactory.newBoolean("useDataSource").setRequired();

    public Property<String> dataSource = PropertyFactory.newProperty("dataSource").setRequired();

    // advanced
    public Property<Integer> commitEvery = PropertyFactory.newInteger("commitEvery").setRequired();

    // TODO additional columns

    // TODO use field options and table

    public Property<Boolean> debug = PropertyFactory.newBoolean("debug").setRequired();

    public Property<Boolean> useBatch = PropertyFactory.newBoolean("useBatch").setRequired();

    public Property<Integer> batchSize = PropertyFactory.newInteger("batchSize").setRequired();

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
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = CommonUtils.addForm(this, Form.MAIN);

        Widget compListWidget = widget(referencedComponent).setWidgetType(Widget.COMPONENT_REFERENCE_WIDGET_TYPE);
        referencedComponent.componentType.setValue(TJDBCConnectionDefinition.COMPONENT_NAME);
        mainForm.addRow(compListWidget);

        mainForm.addRow(connection.getForm(Form.MAIN));
        mainForm.addRow(tableSelection.getForm(Form.REFERENCE));

        mainForm.addRow(dataAction);
        mainForm.addRow(clearDataInTable);

        mainForm.addRow(main.getForm(Form.REFERENCE));

        mainForm.addRow(Widget.widget(fetchSchemaFromTable).setWidgetType(Widget.BUTTON_WIDGET_TYPE));

        mainForm.addRow(dieOnError);

        mainForm.addRow(useDataSource);
        mainForm.addRow(dataSource);

        Form advancedForm = CommonUtils.addForm(this, Form.ADVANCED);
        advancedForm.addRow(commitEvery);
        advancedForm.addRow(debug);
        advancedForm.addRow(useBatch);
        advancedForm.addRow(batchSize);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        dataAction.setValue(DataAction.INSERT);

        commitEvery.setValue(10000);

        useBatch.setValue(true);
        batchSize.setValue(10000);

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
            form.getWidget(batchSize.getName()).setHidden(!useBatch.getValue());
            form.getWidget(commitEvery.getName()).setHidden(useOtherConnection);
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

    public void afterUseBatch() {
        refreshLayout(getForm(Form.ADVANCED));
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

    public ValidationResult afterFetchSchemaFromTable() {
        JDBCSourceOrSink ss = new JDBCSourceOrSink();
        ss.initialize(null, this);
        Schema schema = null;
        try {
            schema = ss.getEndpointSchema(null, tableSelection.tablename.getValue());
        } catch (Exception e) {
            return new ValidationResult().setStatus(ValidationResult.Result.ERROR).setMessage(e.getCause().getMessage());
        }
        main.schema.setValue(schema);
        updateOutputSchemas();
        return ValidationResult.OK;
    }

    @Override
    public AllSetting getRuntimeSetting() {
        AllSetting setting = new AllSetting();

        setting.setReferencedComponentId(referencedComponent.componentInstanceId.getValue());
        setting.setReferencedComponentProperties(referencedComponent.componentProperties);

        setting.setDriverPaths(this.connection.driverTable.drivers.getValue());
        setting.setDriverClass(this.connection.driverClass.getValue());
        setting.setJdbcUrl(this.connection.jdbcUrl.getValue());
        setting.setUsername(this.connection.userPassword.userId.getValue());
        setting.setPassword(this.connection.userPassword.userId.getValue());

        setting.setTablename(this.tableSelection.tablename.getValue());
        setting.setDataAction(this.dataAction.getValue());
        setting.setClearDataInTable(this.clearDataInTable.getValue());
        setting.setDieOnError(this.dieOnError.getValue());

        setting.setCommitEvery(this.commitEvery.getValue());
        setting.setDebug(this.debug.getValue());
        setting.setUseBatch(this.useBatch.getValue());
        setting.setBatchSize(this.batchSize.getValue());

        return setting;
    }

}
