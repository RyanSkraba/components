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

import org.talend.components.api.properties.ComponentProperties;
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

    @Override
    public void setupLayout() {
        addForm(connection.getForm(SalesforceConnectionProperties.MAIN));
        addForm(module.getForm(SalesforceModuleProperties.REFERENCE));
        addForm(schema.getForm(SchemaProperties.REFERENCE));
    }
}
