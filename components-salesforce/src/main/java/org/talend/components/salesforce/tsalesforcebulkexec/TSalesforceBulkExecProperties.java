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

import static org.talend.daikon.properties.PropertyFactory.*;
import static org.talend.daikon.properties.presentation.Widget.*;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
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
    }

    @Override
    protected void setupRejectSchema() {
        Schema s = SchemaBuilder.record("Reject")
                // record set as read only for talend schema
                .prop(SchemaConstants.TALEND_IS_LOCKED, "true")//$NON-NLS-1$
                .fields().name("error")//$NON-NLS-1$
                .prop(Talend6SchemaConstants.TALEND6_COLUMN_CUSTOM, "true")//$NON-NLS-1$
                .prop(SchemaConstants.TALEND_IS_LOCKED, "false")//$NON-NLS-1$
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255")//$NON-NLS-1$
                .type().stringType().noDefault().endRecord();
        schemaReject.schema.setValue(s);
    }

}
