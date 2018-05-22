// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.snowflake.tsnowflakerow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.commons.lang3.StringUtils;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.ComponentConstants;
import org.talend.components.common.SchemaProperties;
import org.talend.components.snowflake.SnowflakeConnectionTableProperties;
import org.talend.components.snowflake.SnowflakePreparedStatementTableProperties;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

/**
 * This properties class is responsible for design tSnowlfakeRow component. For more detailed information see {@link Properties}.
 *
 */
public class TSnowflakeRowProperties extends SnowflakeConnectionTableProperties {

    private static final long serialVersionUID = -1694372250903352577L;

    protected transient PropertyPathConnector FLOW_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "schemaFlow");

    protected transient PropertyPathConnector REJECT_CONNECTOR = new PropertyPathConnector(Connector.REJECT_NAME, "schemaReject");

    public SchemaProperties schemaFlow = new SchemaProperties("schemaFlow");

    public SchemaProperties schemaReject = new SchemaProperties("schemaReject");

    public transient PresentationItem guessQuery = new PresentationItem("guessQuery");

    public Property<String> query = PropertyFactory.newString("query", "\"select id, name from employee\"");

    public Property<Boolean> dieOnError = PropertyFactory.newBoolean("dieOnError").setRequired();

    // advanced

    public Property<Boolean> usePreparedStatement = PropertyFactory.newBoolean("usePreparedStatement").setRequired();

    public SnowflakePreparedStatementTableProperties preparedStatementTable = new SnowflakePreparedStatementTableProperties(
            "preparedStatementTable");

    public Property<Integer> commitCount = PropertyFactory.newInteger("commitCount", 10000);

    public TSnowflakeRowProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form main = getForm(Form.MAIN);
        main.addRow(Widget.widget(guessQuery).setWidgetType(Widget.BUTTON_WIDGET_TYPE));
        main.addRow(Widget.widget(query).setWidgetType(Widget.TEXT_AREA_WIDGET_TYPE));
        main.addRow(dieOnError);

        Form advancedForm = getForm(Form.ADVANCED);
        advancedForm.addRow(usePreparedStatement);
        advancedForm.addRow(Widget.widget(preparedStatementTable).setWidgetType(Widget.TABLE_WIDGET_TYPE));
        advancedForm.addRow(commitCount);
    }

    @Override
    public void setupProperties() {
      super.setupProperties();
      //TODO IMHO, the tag and flag should not be stored, they are only status for technical level, not user data.
      //Now it make the migration a little more complex as we will not call setupProperties when deserialize
      //Maybe should not call it in setupProperties, need another better place to call it
      //Solution : a new method : initTechnicalStatus which can set the tag and flag, the method can be called at the end of deserialize method
      query.setTaggedValue(ComponentConstants.LINE_SEPARATOR_REPLACED_TO, " ");
    }

    public void afterUsePreparedStatement() {
        refreshLayout(getForm(Form.ADVANCED));
    }

    public void afterPropagateQueryResultSet() {
        refreshLayout(getForm(Form.ADVANCED));
    }

    public ValidationResult validateGuessQuery() {
        String tableName = StringUtils.strip((String) table.tableName.getStoredValue(), "\"");
        if (tableName == null || tableName.isEmpty()) {
            return new ValidationResult(Result.ERROR, getI18nMessage("error.missing.tableName"));
        }

        Schema schema = table.main.schema.getValue();
        if (schema == null || schema.getFields().isEmpty()) {
            return new ValidationResult(Result.ERROR, getI18nMessage("error.missing.schema"));
        }

        query.setValue("\"" + generateSqlQuery(tableName, schema) + "\"");
        return ValidationResult.OK;
    }

    public void afterGuessQuery() {
        refreshLayout(getForm(Form.MAIN));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form.getName().equals(Form.ADVANCED)) {
            form.getWidget(preparedStatementTable.getName()).setVisible(usePreparedStatement.getValue());
        }
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

    private void updateOutputSchema() {
        schemaFlow.schema.setValue(table.main.schema.getValue());
    }

    private void updateRejectSchema() {

        Schema schema = schemaFlow.schema.getValue();
        final List<Schema.Field> additionalRejectFields = new ArrayList<Schema.Field>();

        additionalRejectFields.add(createRejectField("errorCode"));
        additionalRejectFields.add(createRejectField("errorMessage"));

        Schema newSchema = Schema.createRecord("rejectOutput", schema.getDoc(), schema.getNamespace(), schema.isError());
        List<Schema.Field> copyFieldList = new ArrayList<>();
        for (Schema.Field se : schema.getFields()) {
            Schema.Field field = new Schema.Field(se.name(), se.schema(), se.doc(), se.defaultVal(), se.order());
            field.getObjectProps().putAll(se.getObjectProps());
            for (Map.Entry<String, Object> entry : se.getObjectProps().entrySet()) {
                field.addProp(entry.getKey(), entry.getValue());
            }
            copyFieldList.add(field);
        }

        copyFieldList.addAll(additionalRejectFields);

        newSchema.setFields(copyFieldList);
        for (Map.Entry<String, Object> entry : schema.getObjectProps().entrySet()) {
            newSchema.addProp(entry.getKey(), entry.getValue());
        }
        schemaReject.schema.setValue(newSchema);
    }

    private Field createRejectField(String name) {
        Schema.Field rejectField = null;
        rejectField = new Schema.Field(name, Schema.create(Schema.Type.STRING), null, (Object) null);
        rejectField.addProp(SchemaConstants.TALEND_IS_LOCKED, "false");
        rejectField.addProp(SchemaConstants.TALEND_FIELD_GENERATED, "true");
        rejectField.addProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255");
        return rejectField;
    }

    @Override
    public void afterMainSchema() {
        updateOutputSchema();
        updateRejectSchema();
    }

    private String generateSqlQuery(String tableName, Schema schema) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        List<Schema.Field> fields = schema.getFields();
        boolean firstOne = true;
        for (Schema.Field field : fields) {
            if (firstOne) {
                firstOne = false;
            } else {
                sql.append(", ");
            }
            sql.append(tableName).append(".").append(field.name());
        }
        sql.append(" FROM ").append(tableName);

        return sql.toString();
    }


    public String getQuery() {
        return query.getValue();
    }

    public boolean usePreparedStatement() {
        return usePreparedStatement.getValue();
    }
}
