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
package org.talend.components.salesforce.tsalesforcegetdeleted;

import static org.talend.components.api.schema.SchemaFactory.newProperty;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.SalesforceModuleProperties;

public class TSalesforceGetDeletedProperties extends ComponentProperties {

    public SchemaElement startDate = newProperty(Type.DATE, "startDate");

    public SchemaElement endDate = newProperty(Type.DATE, "startDate");

    //
    // Collections
    //
    public SalesforceConnectionProperties connection = new SalesforceConnectionProperties("connection"); //$NON-NLS-1$

    public SalesforceModuleProperties module = new SalesforceModuleProperties("module", connection); //$NON-NLS-1$

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = Form.create(this, Form.MAIN, "Salesforce Get Deleted");
        mainForm.addRow(connection.getForm(Form.MAIN));
        mainForm.addRow(module.getForm(Form.REFERENCE));
        mainForm.addRow(startDate);
        mainForm.addRow(endDate);
    }

}
