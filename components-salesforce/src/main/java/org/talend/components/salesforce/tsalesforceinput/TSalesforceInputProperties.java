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
package org.talend.components.salesforce.tsalesforceinput;

import static org.talend.components.api.properties.presentation.Widget.widget;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.common.SchemaProperties;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.SalesforceModuleProperties;

public class TSalesforceInputProperties extends ComponentProperties {

    public SalesforceConnectionProperties connection = new SalesforceConnectionProperties();

    public SalesforceModuleProperties     module     = new SalesforceModuleProperties(connection);

    public SchemaProperties               schema     = new SchemaProperties();

    public TSalesforceInputProperties() {
        setupLayout();
    }

    public static final String MAIN     = "Main";

    public static final String ADVANCED = "Advanced";

    @Override
    public void setupLayout() {
        Form mainForm = Form.create(this, MAIN, "Salesforce Input");
        mainForm.addRow(connection.getForm(SalesforceConnectionProperties.MAIN));
        mainForm.addRow(module.getForm(SalesforceModuleProperties.REFERENCE));
        mainForm.addRow(schema.getForm(SchemaProperties.REFERENCE));
    }
}
