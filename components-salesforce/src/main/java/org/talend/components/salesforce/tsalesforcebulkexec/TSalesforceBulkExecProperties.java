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

import java.util.ArrayList;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.talend.components.salesforce.SalesforceBulkProperties;
import org.talend.components.salesforce.SalesforceOutputProperties;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.talend6.Talend6SchemaConstants;

public class TSalesforceBulkExecProperties extends SalesforceOutputProperties {

    public Property bulkFilePath = newProperty("bulkFilePath");

    public SalesforceBulkProperties bulkProperties = new SalesforceBulkProperties("bulkProperties");

    public TSalesforceBulkExecProperties(String name) {
        super(name);
    }
    
    protected void setupUpsertRelation(Property ur) {
        // They might have been set previously in some inheritance cases
        ur.setChildren(new ArrayList<Property>());
        ur.addChild(newProperty("columnName")); //$NON-NLS-1$
        ur.addChild(newProperty("lookupFieldName")); //$NON-NLS-1$
        ur.addChild(newProperty("lookupFieldModuleName")); //$NON-NLS-1$
        
    	Property property = newProperty(Property.Type.BOOLEAN, "polymorphic");
    	property.setValue(false);
        ur.addChild(property); //$NON-NLS-1$
        
        ur.addChild(newProperty("lookupFieldExternalIdName")); //$NON-NLS-1$
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(bulkFilePath);

        Form advancedForm = getForm(Form.ADVANCED);
        advancedForm.addRow(widget(bulkProperties.getForm(Form.MAIN).setName("bulkProperties")));
    }
    
    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        
        if(Form.ADVANCED.equals(form.getName())) {
        	form.getChildForm(connection.getName()).getWidget(connection.bulkConnection.getName()).setVisible(false);
        	form.getChildForm(connection.getName()).getWidget(connection.httpChunked.getName()).setVisible(false);
        	form.getWidget(upsertRelation.getName()).setVisible(false);
        }
    }
    
    @Override
    public void setupProperties() {
        super.setupProperties();
        
        connection.bulkConnection.setValue(true);
        connection.httpChunked.setValue(false);
    }

    @Override
    protected void setupRejectSchema() {
        Schema s = SchemaBuilder.record("Reject")
                // record set as read only for talend schema
                .prop(Talend6SchemaConstants.TALEND6_IS_READ_ONLY, "true")//$NON-NLS-1$
                .fields().name("error")//$NON-NLS-1$
                .prop(Talend6SchemaConstants.TALEND6_COLUMN_CUSTOM, "true")//$NON-NLS-1$
                .prop(Talend6SchemaConstants.TALEND6_IS_READ_ONLY, "false")//$NON-NLS-1$
                .prop(Talend6SchemaConstants.TALEND6_COLUMN_TALEND_TYPE, "id_String")//$NON-NLS-1$
                .prop(Talend6SchemaConstants.TALEND6_COLUMN_LENGTH, "255")//$NON-NLS-1$
                .type().stringType().noDefault().endRecord();
        schemaReject.schema.setValue(s);
    }

}
