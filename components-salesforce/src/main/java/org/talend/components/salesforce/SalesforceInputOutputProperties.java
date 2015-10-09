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

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.schema.Schema;

/**
 * Properties common to input and output Salesforce components.
 */
public class SalesforceInputOutputProperties extends ComponentProperties {

    // Collections
    //
    public SalesforceConnectionProperties connection = new SalesforceConnectionProperties("connection"); //$NON-NLS-1$

    public SalesforceModuleProperties module;

    @Override
    public ComponentProperties init() {
        // Allow for subclassing
        module = new SalesforceModuleProperties("module", connection);
        return super.init();
    }

    public Schema getSchema() {
        return (Schema) module.schema.getValue(module.schema.schema);
    }

    @Override
    public void setupLayout() {
        Form mainForm = Form.create(this, Form.MAIN, "Salesforce Input/Output");
        mainForm.addRow(connection.getForm(Form.MAIN));
        mainForm.addRow(module.getForm(Form.REFERENCE));
    }

}
