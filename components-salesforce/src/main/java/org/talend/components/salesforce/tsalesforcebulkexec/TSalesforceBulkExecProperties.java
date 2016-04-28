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

import org.talend.components.salesforce.SalesforceBulkProperties;
import org.talend.components.salesforce.SalesforceOutputProperties;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

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
        mainForm.addRow(bulkFilePath);

        Form advancedForm = getForm(Form.ADVANCED);
        advancedForm.addRow(widget(bulkProperties.getForm(Form.MAIN).setName("bulkProperties")));
        advancedForm.addRow(widget(upsertRelationTable).setWidgetType(Widget.WidgetType.TABLE));
    }
    
    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        
        if(Form.ADVANCED.equals(form.getName())) {
        	form.getChildForm(connection.getName()).getWidget(connection.bulkConnection.getName()).setVisible(false);
        	form.getChildForm(connection.getName()).getWidget(connection.httpChunked.getName()).setVisible(false);
        	form.getWidget(upsertRelationTable.getName()).setVisible(false);
        }
    }
    
    @Override
    public void setupProperties() {
        super.setupProperties();
        
        connection.bulkConnection.setValue(true);
        connection.httpChunked.setValue(false);
        upsertRelationTable.setUsePolymorphic(true);
    }

}
