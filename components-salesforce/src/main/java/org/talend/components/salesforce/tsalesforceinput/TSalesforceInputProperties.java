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

import org.talend.components.base.ComponentProperties;
import org.talend.components.base.properties.Property;
import org.talend.components.base.properties.presentation.Form;
import org.talend.components.base.properties.presentation.Layout;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.tsalesforceconnect.TSalesforceConnectProperties;

public class TSalesforceInputProperties extends ComponentProperties {

    SalesforceConnectionProperties connection = new SalesforceConnectionProperties();

    Property<String> module = new Property<String>("module", "Module").setRequired(true);

    public TSalesforceInputProperties() {
        setupLayout();
    }

    public static final String INPUT = "Input";

    public void setupLayout() {
        addForm(connection.getForm(SalesforceConnectionProperties.CONNECTION));

        Form inputForm = Form.create(this, INPUT, "Salesforce Input Module");
        inputForm.addChild(module, Layout.create().setRow(1));
    }
}
