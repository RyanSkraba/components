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
package org.talend.components.salesforce;

import static org.talend.components.api.schema.SchemaFactory.*;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.schema.SchemaElement;

public class SalesforceGetDeletedUpdatedProperties extends ComponentProperties {

    public SchemaElement startDate = newDate("startDate");

    public SchemaElement endDate = newDate("endDate");

    //
    // Collections
    //
    public SalesforceConnectionProperties connection = new SalesforceConnectionProperties();

    public SalesforceModuleProperties module = new SalesforceModuleProperties().setConnection(connection);

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = Form.create(this, Form.MAIN, "Salesforce Get Updated/Deleted");
        mainForm.addRow(connection.getForm(Form.REFERENCE));
        mainForm.addRow(module.getForm(Form.REFERENCE));
        mainForm.addRow(startDate);
        mainForm.addRow(endDate);
    }

}
