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
package org.talend.components.salesforce.tsalesforcebulkexec;

import static org.talend.daikon.properties.PropertyFactory.newProperty;
import static org.talend.daikon.properties.presentation.Widget.widget;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.SchemaBuilder.FieldAssembler;
import org.apache.avro.SchemaBuilder.FieldBuilder;
import org.apache.avro.SchemaBuilder.RecordBuilder;
import org.talend.components.api.component.ISchemaListener;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.salesforce.SalesforceBulkProperties;
import org.talend.components.salesforce.SalesforceOutputProperties;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.talend6.Talend6SchemaConstants;

public class TSalesforceBulkExecProperties extends SalesforceOutputProperties {

    public Property bulkFilePath = newProperty("bulkFilePath");

    public SalesforceBulkProperties bulkProperties = new SalesforceBulkProperties("bulkProperties");

    public TSalesforceBulkExecProperties(String name) {
        super(name);
    }
    
    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(widget(bulkFilePath).setWidgetType(Widget.WidgetType.FILE));

        Form advancedForm = getForm(Form.ADVANCED);
        advancedForm.addRow(widget(bulkProperties.getForm(Form.MAIN).setName("bulkProperties")));
        advancedForm.addRow(widget(upsertRelationTable).setWidgetType(Widget.WidgetType.TABLE));
    }
    
    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        
        if(Form.ADVANCED.equals(form.getName())) {
        	form.getChildForm(connection.getName()).getWidget(connection.bulkConnection.getName()).setHidden(true);
        	form.getChildForm(connection.getName()).getWidget(connection.httpChunked.getName()).setHidden(true);
        	form.getWidget(upsertRelationTable.getName()).setHidden(true);
        }
    }
    
    @Override
    public void setupProperties() {
        super.setupProperties();
        
        connection.bulkConnection.setValue(true);
        connection.httpChunked.setValue(false);
        upsertRelationTable.setUsePolymorphic(true);
        
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

    	Schema inputSchema = (Schema) module.main.schema.getValue();
        Schema mainOutputSchema = createRecordBuilderFromSchema(inputSchema, "output")
        	.name("salesforce_id")
    		.prop(SchemaConstants.TALEND_IS_LOCKED, "false")
	        .prop(Talend6SchemaConstants.TALEND6_COLUMN_CUSTOM, "true")
	        .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255")
	        .type().stringType().noDefault()
	        
	        .name("salesforce_created")
	        .prop(SchemaConstants.TALEND_IS_LOCKED, "false")
	        .prop(Talend6SchemaConstants.TALEND6_COLUMN_CUSTOM, "true")
	        .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255")
	        .type().stringType().noDefault()
	    .endRecord();

        schemaFlow.schema.setValue(mainOutputSchema);
        
    	Schema rejectSchema = createRecordBuilderFromSchema(inputSchema, "rejectOutput")
			.name("error")
	        .prop(Talend6SchemaConstants.TALEND6_COLUMN_CUSTOM, "true")
	        .prop(SchemaConstants.TALEND_IS_LOCKED, "false")
	        .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255")
	        .type().stringType().noDefault()
	    .endRecord();

        schemaReject.schema.setValue(rejectSchema);

    }

    private FieldAssembler<Schema> createRecordBuilderFromSchema(Schema inputSchema, String newSchemaName) {
        RecordBuilder<Schema> recordBuilder = SchemaBuilder.record(newSchemaName);
        FieldAssembler<Schema> fieldAssembler = recordBuilder.fields();
        for (Field field : inputSchema.getFields()) {
            FieldBuilder<Schema> fieldBuilder = fieldAssembler.name(field.name());
            for (String propName : field.getObjectProps().keySet()) {
                fieldBuilder.prop(propName, field.getObjectProps().get(propName).toString());
            }
            fieldAssembler = fieldBuilder.type().stringType().noDefault();
        }
        return fieldAssembler;
    }
    
    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        if (isOutputConnection) {
        	HashSet<PropertyPathConnector> connectors = new HashSet<>();
        	connectors.add(FLOW_CONNECTOR);
        	connectors.add(REJECT_CONNECTOR);
            return connectors;
        } else {
            return Collections.emptySet();
        }
    }

}
