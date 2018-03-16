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
package org.talend.components.jdbc.tjdbcoutput;

import static org.talend.daikon.properties.presentation.Widget.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avro.Schema;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.ComponentConstants;
import org.talend.components.jdbc.JdbcRuntimeInfo;
import org.talend.components.jdbc.RuntimeSettingProvider;
import org.talend.components.jdbc.module.AdditionalColumnsTable;
import org.talend.components.jdbc.module.FieldOptionsTable;
import org.talend.components.jdbc.module.JDBCConnectionModule;
import org.talend.components.jdbc.module.JDBCTableSelectionModule;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.components.jdbc.runtime.setting.JdbcRuntimeSourceOrSink;
import org.talend.components.jdbc.tjdbcconnection.TJDBCConnectionDefinition;
import org.talend.components.jdbc.tjdbcconnection.TJDBCConnectionProperties;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;
import org.talend.daikon.properties.runtime.RuntimeContext;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

public class TJDBCOutputProperties extends FixedConnectorsComponentProperties implements RuntimeSettingProvider {

    public TJDBCOutputProperties(String name) {
        super(name);
    }

    // main
    public ComponentReferenceProperties<TJDBCConnectionProperties> referencedComponent = new ComponentReferenceProperties<>(
            "referencedComponent", TJDBCConnectionDefinition.COMPONENT_NAME);

    public JDBCConnectionModule connection = new JDBCConnectionModule("connection");

    public JDBCTableSelectionModule tableSelection = new JDBCTableSelectionModule("tableSelection");

    public enum DataAction {
        INSERT,
        UPDATE,
        INSERT_OR_UPDATE,
        UPDATE_OR_INSERT,
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

    public final transient PresentationItem fetchSchemaFromTable = new PresentationItem("fetchSchemaFromTable", "Guess schema");

    public Property<Boolean> dieOnError = PropertyFactory.newBoolean("dieOnError").setRequired();

    public Property<Boolean> useDataSource = PropertyFactory.newBoolean("useDataSource").setRequired();

    public Property<String> dataSource = PropertyFactory.newProperty("dataSource").setRequired();

    // advanced
    public Property<Integer> commitEvery = PropertyFactory.newInteger("commitEvery").setRequired();

    public AdditionalColumnsTable additionalColumns = new AdditionalColumnsTable("additionalColumns");

    public Property<Boolean> enableFieldOptions = PropertyFactory.newBoolean("enableFieldOptions").setRequired();

    public FieldOptionsTable fieldOptions = new FieldOptionsTable("fieldOptions");

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

        Schema rejectSchema = CommonUtils.newSchema(inputSchema, "rejectOutput", additionalRejectFields);

        schemaReject.schema.setValue(rejectSchema);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = CommonUtils.addForm(this, Form.MAIN);

        Widget compListWidget = widget(referencedComponent).setWidgetType(Widget.COMPONENT_REFERENCE_WIDGET_TYPE);
        mainForm.addRow(compListWidget);

        mainForm.addRow(connection.getForm(Form.MAIN));
        mainForm.addRow(tableSelection.getForm(Form.REFERENCE));

        mainForm.addRow(dataAction);
        mainForm.addColumn(clearDataInTable);

        mainForm.addRow(main.getForm(Form.REFERENCE));

        mainForm.addRow(Widget.widget(fetchSchemaFromTable).setWidgetType(Widget.BUTTON_WIDGET_TYPE));

        mainForm.addRow(dieOnError);

        mainForm.addRow(useDataSource);
        mainForm.addRow(dataSource);

        Form advancedForm = CommonUtils.addForm(this, Form.ADVANCED);
        advancedForm.addRow(commitEvery);

        advancedForm.addRow(Widget.widget(additionalColumns).setWidgetType(Widget.TABLE_WIDGET_TYPE));

        advancedForm.addRow(enableFieldOptions);
        advancedForm.addRow(Widget.widget(fieldOptions).setWidgetType(Widget.TABLE_WIDGET_TYPE));

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

        connection.setNotRequired();
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        boolean useOtherConnection = CommonUtils.useExistedConnection(referencedComponent);

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
            if ((dataAction.getValue() == DataAction.INSERT) || (dataAction.getValue() == DataAction.UPDATE)
                    || (dataAction.getValue() == DataAction.DELETE)) {
                form.getWidget(useBatch.getName()).setHidden(false);
                form.getWidget(batchSize.getName()).setHidden(!useBatch.getValue());
            } else {
                form.getWidget(useBatch.getName()).setHidden(true);
                form.getWidget(batchSize.getName()).setHidden(true);
            }
            form.getWidget(fieldOptions.getName()).setVisible(enableFieldOptions.getValue());

            updateReferenceColumns();
            updateFieldOptions();
        }
    }

    private void updateReferenceColumns() {
        Schema schema = main.schema.getValue();
        if (schema == null) {
            return;
        }

        List<String> fieldNames = new ArrayList<>();
        for (Schema.Field f : schema.getFields()) {
            fieldNames.add(f.name());
        }

        additionalColumns.referenceColumns.setPossibleValues(fieldNames);
    }

    public void afterReferencedComponent() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(Form.ADVANCED));
    }

    public void afterUseDataSource() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterDataAction() {
        refreshLayout(getForm(Form.ADVANCED));
    }
    
    public void afterUseBatch() {
        refreshLayout(getForm(Form.ADVANCED));
    }

    public void afterEnableFieldOptions() {
        refreshLayout(getForm(Form.ADVANCED));
    }

    private void updateFieldOptions() {
        Schema schema = main.schema.getValue();
        if (schema == null) {
            return;
        }

        Map<String, FieldOption> oldValues = getOldFieldOptions();

        List<String> fieldNames = new ArrayList<>();
        List<Boolean> insertable = new ArrayList<>();
        List<Boolean> updateable = new ArrayList<>();

        for (Schema.Field f : schema.getFields()) {
            fieldNames.add(f.name());
            FieldOption oldValue = null;
            if (oldValues != null && (oldValue = oldValues.get(f.name())) != null) {
                // set the old value if the field is old after schema changed
                insertable.add(oldValue.insertable);
                updateable.add(oldValue.updatable);
            } else {
                // set the default value if the field is new after schema changed
                insertable.add(true);
                updateable.add(true);
            }
        }
        fieldOptions.schemaColumns.setValue(fieldNames);
        fieldOptions.insertable.setValue(insertable);
        fieldOptions.updatable.setValue(updateable);
    }

    private Map<String, FieldOption> getOldFieldOptions() {
        Map<String, FieldOption> oldValueMap = null;
        Object fs = fieldOptions.schemaColumns.getValue();
        Object is = fieldOptions.insertable.getValue();
        Object us = fieldOptions.updatable.getValue();
        if (fs != null && is != null && us != null && (fs instanceof List) && (is instanceof List) && (us instanceof List)) {
            oldValueMap = new HashMap<>();
            List<String> names = (List<String>) fs;
            List<Object> insertables = (List<Object>) is;
            List<Object> updatables = (List<Object>) us;
            for (int i = 0; i < names.size(); i++) {
                FieldOption option = new FieldOption();
                option.fieldName = names.get(i);
                Object its = insertables.get(i);
                Object uts = updatables.get(i);

                if (its instanceof Boolean) {
                    option.insertable = (Boolean) its;
                } else {
                    option.insertable = Boolean.valueOf((String) its);
                }

                if (its instanceof Boolean) {
                    option.updatable = (Boolean) uts;
                } else {
                    option.updatable = Boolean.valueOf((String) uts);
                }

                oldValueMap.put(option.fieldName, option);
            }
        }
        return oldValueMap;
    }

    private class FieldOption {

        String fieldName;

        boolean insertable;

        boolean updatable;
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

    public ValidationResult afterFetchSchemaFromTable(RuntimeContext runtimeContext) {
        Object mappingFileLocation = runtimeContext.getData(ComponentConstants.MAPPING_LOCATION);
        if (mappingFileLocation == null) {
            return new ValidationResult(ValidationResult.Result.ERROR, "can't find the mapping files directory");
        }

        JdbcRuntimeInfo jdbcRuntimeInfo = new JdbcRuntimeInfo(this, "org.talend.components.jdbc.runtime.JDBCSource");
        try (SandboxedInstance sandboxI = RuntimeUtil.createRuntimeClass(jdbcRuntimeInfo,
                connection.getClass().getClassLoader())) {
            JdbcRuntimeSourceOrSink ss = (JdbcRuntimeSourceOrSink) sandboxI.getInstance();
            ss.initialize(null, this);
            Schema schema = null;
            try {
                ss.setDBTypeMapping(CommonUtils.getMapping((String) mappingFileLocation, this.getRuntimeSetting(), null, null));
                schema = ss.getEndpointSchema(null, tableSelection.tablename.getValue());
            } catch (Exception e) {
                return new ValidationResult(ValidationResult.Result.ERROR, CommonUtils.getClearExceptionInfo(e));
            }
            main.schema.setValue(schema);
            updateOutputSchemas();
        }
        return ValidationResult.OK;
    }

    // this method is necessary as the callback logic, we need to improve it
    public ValidationResult afterFetchSchemaFromTable() {
        // do nothing
        return ValidationResult.OK;
    }

    @Override
    public AllSetting getRuntimeSetting() {
        AllSetting setting = new AllSetting();

        CommonUtils.setReferenceInfoAndConnectionInfo(setting, referencedComponent, connection);

        setting.setTablename(this.tableSelection.tablename.getValue());
        setting.setDataAction(this.dataAction.getValue());
        setting.setClearDataInTable(this.clearDataInTable.getValue());
        setting.setDieOnError(this.dieOnError.getValue());

        setting.setUseAutoCommit(this.useDataSource.getValue());
        setting.setDataSource(this.dataSource.getValue());

        setting.setCommitEvery(this.commitEvery.getValue());
        setting.setDebug(this.debug.getValue());
        setting.setUseBatch(this.useBatch.getValue());
        setting.setBatchSize(this.batchSize.getValue());

        setting.setNewDBColumnNames4AdditionalParameters(this.additionalColumns.names.getValue());
        setting.setSqlExpressions4AdditionalParameters(this.additionalColumns.sqlExpressions.getValue());
        setting.setPositions4AdditionalParameters(this.additionalColumns.positions.getValue());
        setting.setReferenceColumns4AdditionalParameters(this.additionalColumns.referenceColumns.getValue());

        setting.setEnableFieldOptions(this.enableFieldOptions.getValue());
        setting.setSchemaColumns4FieldOption(this.fieldOptions.schemaColumns.getValue());
        setting.setInsertable4FieldOption(this.fieldOptions.insertable.getValue());
        setting.setUpdatable4FieldOption(this.fieldOptions.updatable.getValue());
        setting.setUpdateKey4FieldOption(this.fieldOptions.updateKey.getValue());
        setting.setDeletionKey4FieldOption(this.fieldOptions.deletionKey.getValue());

        return setting;
    }

}
