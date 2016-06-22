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
package org.talend.components.salesforce.tsalesforceoutput;

import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newBoolean;
import static org.talend.daikon.properties.property.PropertyFactory.newInteger;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.talend.components.api.component.ISchemaListener;
import org.talend.components.salesforce.SalesforceOutputProperties;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

public class TSalesforceOutputProperties extends SalesforceOutputProperties {

    public static final String FIELD_SALESFORCE_ID = "salesforce_id";

    public static final String FIELD_ERROR_CODE = "errorCode";

    public static final String FIELD_ERROR_FIELDS = "errorFields";

    public static final String FIELD_ERROR_MESSAGE = "errorMessage";

    //
    // Advanced
    //
    public Property<Boolean> extendInsert = newBoolean("extendInsert", true); //$NON-NLS-1$

    public Property<Boolean> ceaseForError = newBoolean("ceaseForError", true); //$NON-NLS-1$

    public Property<Boolean> ignoreNull = newBoolean("ignoreNull"); //$NON-NLS-1$

    public Property<Boolean> retrieveInsertId = newBoolean("retrieveInsertId"); //$NON-NLS-1$

    public Property<Integer> commitLevel = newInteger("commitLevel", 200); //$NON-NLS-1$

    // FIXME - should be file
    public Property<String> logFileName = newString("logFileName"); //$NON-NLS-1$

    public TSalesforceOutputProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        upsertRelationTable.setUseLookupFieldName(true);
        module.setSchemaListener(new ISchemaListener() {

            @Override
            public void afterSchema() {
                updateOutputSchemas();
                beforeUpsertKeyColumn();
                beforeUpsertRelationTable();
            }
        });
    }
    
    private void updateOutputSchemas() {
        Schema inputSchema = module.main.schema.getValue();
        
        Schema.Field field = null;
        
        if (!extendInsert.getValue() && retrieveInsertId.getValue() && OutputAction.INSERT.equals(outputAction.getValue())) {
            final List<Schema.Field> additionalMainFields = new ArrayList<Schema.Field>();
            
            field = new Schema.Field(FIELD_SALESFORCE_ID, Schema.create(Schema.Type.STRING), null, (Object) null);
            field.addProp(SchemaConstants.TALEND_IS_LOCKED, "false");
            field.addProp(SchemaConstants.TALEND_FIELD_GENERATED, "true");
            field.addProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255");
            additionalMainFields.add(field);
            
            Schema mainOutputSchema = newSchema(inputSchema, "output", additionalMainFields);
            schemaFlow.schema.setValue(mainOutputSchema);
        } else {
            schemaFlow.schema.setValue(inputSchema);
        }

        final List<Schema.Field> additionalRejectFields = new ArrayList<Schema.Field>();

        field = new Schema.Field(FIELD_ERROR_CODE, Schema.create(Schema.Type.STRING), null, (Object) null);
        field.addProp(SchemaConstants.TALEND_IS_LOCKED, "false");
        field.addProp(SchemaConstants.TALEND_FIELD_GENERATED, "true");
        field.addProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255");
        additionalRejectFields.add(field);
        
        field = new Schema.Field(FIELD_ERROR_FIELDS, Schema.create(Schema.Type.STRING), null, (Object) null);
        field.addProp(SchemaConstants.TALEND_IS_LOCKED, "false");
        field.addProp(SchemaConstants.TALEND_FIELD_GENERATED, "true");
        field.addProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255");
        additionalRejectFields.add(field);
        
        field = new Schema.Field(FIELD_ERROR_MESSAGE, Schema.create(Schema.Type.STRING), null, (Object) null);
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
            for (Map.Entry<String,Object> entry : se.getObjectProps().entrySet()) {
                field.addProp(entry.getKey(), entry.getValue());
            }
            copyFieldList.add(field);
        }

        copyFieldList.addAll(moreFields);

        newSchema.setFields(copyFieldList);
        for (Map.Entry<String,Object> entry : metadataSchema.getObjectProps().entrySet()) {
            newSchema.addProp(entry.getKey(), entry.getValue());
        }

        return newSchema;
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form advancedForm = getForm(Form.ADVANCED);
        advancedForm.addRow(extendInsert);
        advancedForm.addRow(ceaseForError);
        advancedForm.addRow(ignoreNull);
        advancedForm.addRow(retrieveInsertId);
        advancedForm.addRow(commitLevel);
        advancedForm.addRow(widget(logFileName).setWidgetType(Widget.FILE_WIDGET_TYPE));
    }

    public void afterExtendInsert() {
        refreshLayout(getForm(Form.ADVANCED));
        updateOutputSchemas();
    }

    public void afterRetrieveInsertId() {
        refreshLayout(getForm(Form.ADVANCED));
        updateOutputSchemas();
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        if (form.getName().equals(Form.ADVANCED)) {

            form.getChildForm(connection.getName()).getWidget(connection.bulkConnection.getName()).setHidden(true);
            form.getChildForm(connection.getName()).getWidget(connection.httpTraceMessage.getName()).setHidden(true);
            form.getWidget("commitLevel").setHidden(!extendInsert.getValue());
            form.getWidget("retrieveInsertId")
                    .setHidden(extendInsert.getValue() || !OutputAction.INSERT.equals(outputAction.getValue()));
            form.getWidget("ignoreNull").setHidden(!(OutputAction.UPDATE.equals(outputAction.getValue())
                    || OutputAction.UPSERT.equals(outputAction.getValue())));
        }
    }

}
