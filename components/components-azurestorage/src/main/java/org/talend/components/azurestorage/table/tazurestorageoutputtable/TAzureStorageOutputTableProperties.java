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
package org.talend.components.azurestorage.table.tazurestorageoutputtable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.SchemaBuilder;
import org.talend.components.api.component.ISchemaListener;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.azurestorage.table.AzureStorageTableProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class TAzureStorageOutputTableProperties extends AzureStorageTableProperties {

    private static final long serialVersionUID = 3173788047463486011L;

    public enum ActionOnData {
        Insert,
        Insert_Or_Replace,
        Insert_Or_Merge,
        Merge,
        Replace,
        Delete
    }

    public enum ActionOnTable {
        Default,
        Drop_and_create_table,
        Create_table,
        Create_table_if_does_not_exist,
        Drop_table_if_exist_and_create,
    }

    public Property<ActionOnData> actionOnData = PropertyFactory.newEnum("actionOnData", ActionOnData.class);

    public Property<ActionOnTable> actionOnTable = PropertyFactory.newEnum("actionOnTable", ActionOnTable.class);

    public Property<Boolean> processOperationInBatch = PropertyFactory.newBoolean("processOperationInBatch");

    public Property<Boolean> dieOnError = PropertyFactory.newBoolean("dieOnError");

    public Property<List<String>> partitionKey = PropertyFactory.newStringList("partitionKey");

    public Property<List<String>> rowKey = PropertyFactory.newStringList("rowKey");

    public TAzureStorageOutputTableProperties(String name) {
        super(name);
    }

    @Override
    public Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        HashSet<PropertyPathConnector> connectors = new HashSet<>();
        if (!isOutputConnection) {
            connectors.add(MAIN_CONNECTOR);
        }
        return connectors;
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        dieOnError.setValue(true);

        actionOnData.setValue(ActionOnData.Insert);
        actionOnTable.setValue(ActionOnTable.Default);
        processOperationInBatch.setValue(false);

        Schema s = SchemaBuilder.record("Main").fields()
                //
                .name("PartitionKey").prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255")// $NON-NLS-3$
                .type(AvroUtils._string()).noDefault()
                //
                .name("RowKey").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true")
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255")// $NON-NLS-3$
                .type(AvroUtils._string()).noDefault()
                //
                .endRecord();
        schema.schema.setValue(s);
        partitionKey.setPossibleValues("PartitionKey", "RowKey");
        rowKey.setPossibleValues("PartitionKey", "RowKey");

        // update the properties when schema change
        setSchemaListener(new ISchemaListener() {

            @Override
            public void afterSchema() {
                updateOutputSchemas();
                updatePartitionKeyAndRowKey();

            }
        });
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = getForm(Form.MAIN);
        mainForm.addColumn(Widget.widget(partitionKey).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
        mainForm.addColumn(Widget.widget(rowKey).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
        mainForm.addRow(actionOnData);
        mainForm.addColumn(actionOnTable);
        mainForm.addRow(processOperationInBatch);
        mainForm.addRow(dieOnError);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        updateOutputSchemas();
    }

    public void updatePartitionKeyAndRowKey() {
        Schema inputSchema = schema.schema.getValue();
        List<String> possibleValues = new ArrayList<String>();
        for (Field field : inputSchema.getFields()) {
            Schema fSchema = field.schema();
            if (fSchema.getType() == Type.UNION) {
                for (Schema s : field.schema().getTypes()) {
                    if (s.getType() != Type.NULL) {
                        fSchema = s;
                        break;
                    }
                }
            }
            if (fSchema.getType().equals(Type.STRING)) {
                possibleValues.add(field.name());
            }
        }
        partitionKey.setPossibleValues(possibleValues);
        rowKey.setPossibleValues(possibleValues);
    }

    protected void updateOutputSchemas() {
        Schema inputSchema = schema.schema.getValue();
        schemaFlow.schema.setValue(inputSchema);
        final List<Schema.Field> additionalRejectFields = new ArrayList<Schema.Field>();

        Schema.Field field = null;
        field = new Schema.Field("errorCode", Schema.create(Schema.Type.STRING), null, (Object) null);
        field.addProp(SchemaConstants.TALEND_IS_LOCKED, "false");
        field.addProp(SchemaConstants.TALEND_FIELD_GENERATED, "true");
        field.addProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255");
        additionalRejectFields.add(field);

        field = new Schema.Field("errorMessage", Schema.create(Schema.Type.STRING), null, (Object) null);
        field.addProp(SchemaConstants.TALEND_IS_LOCKED, "false");
        field.addProp(SchemaConstants.TALEND_FIELD_GENERATED, "true");
        field.addProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255");
        additionalRejectFields.add(field);

        Schema rejectSchema = newSchema(inputSchema, "schemaReject", additionalRejectFields);
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

}
