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

import static org.talend.daikon.properties.PropertyFactory.*;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.presentation.Form;

public class SalesforceGetDeletedUpdatedProperties extends SalesforceConnectionModuleProperties {

    public Property startDate = newDate("startDate").setRequired();

    public Property endDate = newDate("endDate").setRequired();

    public SalesforceGetDeletedUpdatedProperties(String name) {
        super(name);
    }

    public void setupProperties() {
        super.setupProperties();
        startDate.setDefaultValue("");
        endDate.setDefaultValue("");
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(startDate);
        mainForm.addRow(endDate);
    }

}
