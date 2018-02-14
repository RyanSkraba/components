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
package org.talend.components.snowflake.tsnowflakeoutput;

import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newEnum;
import static org.talend.daikon.properties.property.PropertyFactory.newString;
import static org.talend.daikon.properties.property.PropertyFactory.newBoolean;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.ISchemaListener;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.SchemaProperties;
import org.talend.components.snowflake.SnowflakeConnectionTableProperties;
import org.talend.components.snowflake.SnowflakeTableProperties;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.serialize.PostDeserializeSetup;
import org.talend.daikon.serialize.migration.SerializeSetVersion;

public class TSnowflakeOutputProperties extends SnowflakeConnectionTableProperties implements SerializeSetVersion {

    private static final int CONVERT_COLUMNS_AND_TABLE_TO_UPPERCASE_VERSION = 1;

    public enum OutputAction {
        INSERT,
        UPDATE,
        UPSERT,
        DELETE
    }

    public Property<OutputAction> outputAction = newEnum("outputAction", OutputAction.class); // $NON-NLS-1$

    public Property<String> upsertKeyColumn = newString("upsertKeyColumn"); //$NON-NLS-1$

    protected transient PropertyPathConnector FLOW_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "schemaFlow");

    protected transient PropertyPathConnector REJECT_CONNECTOR = new PropertyPathConnector(Connector.REJECT_NAME, "schemaReject");

    public SchemaProperties schemaReject = new SchemaProperties("schemaReject"); //$NON-NLS-1$

    public Property<Boolean> convertColumnsAndTableToUppercase = newBoolean("convertColumnsAndTableToUppercase");

    // Have to use an explicit class to get the override of afterTableName(), an anonymous
    // class cannot be public and thus cannot be called.
    public class TableSubclass extends SnowflakeTableProperties {

        public TableSubclass(String name) {
            super(name);
        }

        @Override
        public ValidationResult afterTableName() throws Exception {
            ValidationResult validationResult = super.afterTableName();
            if (table.main.schema.getValue() != null) {
                List<String> fieldNames = getFieldNames(table.main.schema);
                upsertKeyColumn.setPossibleValues(fieldNames);
            }
            return validationResult;
        }
    }

    public static final String FIELD_COLUMN_NAME= "columnName";
    public static final String FIELD_ROW_NUMBER= "rowNumber";
    public static final String FIELD_CATEGORY= "category";
    public static final String FIELD_CHARACTER= "character";
    public static final String FIELD_ERROR_MESSAGE= "errorMessage";
    public static final String FIELD_BYTE_OFFSET= "byteOffset";
    public static final String FIELD_LINE= "line";
    public static final String FIELD_SQL_STATE= "sqlState";
    public static final String FIELD_CODE= "code";


    public TSnowflakeOutputProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        outputAction.setValue(OutputAction.INSERT);
        ISchemaListener listener;

        //This condition was added due to some strange behaviour of serialization.
        if (table != null) {
            listener = table.schemaListener;
        } else {
            listener = new ISchemaListener() {

                @Override
                public void afterSchema() {
                    afterMainSchema();
                }
            };
        }

        table = new TableSubclass("table");
        table.connection = connection;
        table.setSchemaListener(listener);
        table.setupProperties();

        convertColumnsAndTableToUppercase.setValue(true);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(outputAction);
        mainForm.addColumn(widget(upsertKeyColumn).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));

        Form advancedForm = getForm(Form.ADVANCED);
        advancedForm.addRow(convertColumnsAndTableToUppercase);
    }

    public void afterOutputAction() {
        refreshLayout(getForm(Form.MAIN));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        if (form.getName().equals(Form.MAIN)) {
            Form advForm = getForm(Form.ADVANCED);
            if (advForm != null) {
                boolean isUpsert = OutputAction.UPSERT.equals(outputAction.getValue());
                form.getWidget(upsertKeyColumn.getName()).setHidden(!isUpsert);
                if (isUpsert) {
                    beforeUpsertKeyColumn();
                }
            }
        }
    }

    protected List<String> getFieldNames(Property<?> schema) {
        Schema s = (Schema) schema.getValue();
        List<String> fieldNames = new ArrayList<>();
        for (Schema.Field f : s.getFields()) {
            fieldNames.add(f.name());
        }
        return fieldNames;
    }

    public void beforeUpsertKeyColumn() {
        if (getSchema() != null) {
            upsertKeyColumn.setPossibleValues(getFieldNames(table.main.schema));
        }
    }


    private void addSchemaField(String name, List<Schema.Field> fields) {
        Schema.Field field = new Schema.Field(name, Schema.create(Schema.Type.STRING), null, (Object) null);
        field.addProp(SchemaConstants.TALEND_IS_LOCKED, "false");
        field.addProp(SchemaConstants.TALEND_FIELD_GENERATED, "true");
        field.addProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255");
        fields.add(field);
    }

    private void updateOutputSchemas() {
        Schema inputSchema = table.main.schema.getValue();

        final List<Schema.Field> additionalRejectFields = new ArrayList<Schema.Field>();
        addSchemaField(FIELD_COLUMN_NAME, additionalRejectFields);
        addSchemaField(FIELD_ROW_NUMBER, additionalRejectFields);
        addSchemaField(FIELD_CATEGORY, additionalRejectFields);
        addSchemaField(FIELD_CHARACTER, additionalRejectFields);
        addSchemaField(FIELD_ERROR_MESSAGE, additionalRejectFields);
        addSchemaField(FIELD_BYTE_OFFSET, additionalRejectFields);
        addSchemaField(FIELD_LINE, additionalRejectFields);
        addSchemaField(FIELD_SQL_STATE, additionalRejectFields);
        addSchemaField(FIELD_CODE, additionalRejectFields);

        Schema rejectSchema = Schema.createRecord("rejectOutput", inputSchema.getDoc(), inputSchema.getNamespace(),
                inputSchema.isError());

        List<Schema.Field> copyFieldList = new ArrayList<>();
        copyFieldList.addAll(additionalRejectFields);

        rejectSchema.setFields(copyFieldList);

        schemaReject.schema.setValue(rejectSchema);
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        HashSet<PropertyPathConnector> connectors = new HashSet<>();
        if (isOutputConnection) {
            connectors.add(REJECT_CONNECTOR);
        } else {
            connectors.add(MAIN_CONNECTOR);
        }
        return connectors;
    }

    @Override
    public void afterMainSchema() {
        updateOutputSchemas();
        beforeUpsertKeyColumn();
    }

    @Override
    public int getVersionNumber() {
        return 1;
    }

    @Override
    public boolean postDeserialize(int version, PostDeserializeSetup setup, boolean persistent) {
        boolean migrated = super.postDeserialize(version, setup, persistent);
        if (version < CONVERT_COLUMNS_AND_TABLE_TO_UPPERCASE_VERSION) {
            convertColumnsAndTableToUppercase.setValue(false);
            migrated = true;
        }
        return migrated;
    }
}
