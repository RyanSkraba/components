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
package org.talend.component.salesforce.tsalesforceinput;

import org.talend.component.ComponentProperties;
import org.talend.component.properties.Property;
import org.talend.component.properties.presentation.Layout;
import org.talend.component.salesforce.SalesforceConnectionProperties;
import org.talend.component.salesforce.tsalesforceconnect.TSalesforceConnectProperties;

// FIXME - this breaks the boot hack because there are multiple sets of properties
public class TSalesforceInputProperties /*extends ComponentProperties*/ {

    Property<SalesforceConnectionProperties> connection = new Property<SalesforceConnectionProperties>("connection", "Connection")
            .setRequired(true).setValue(new TSalesforceConnectProperties());

    Property<String> module = new Property<String>("module", "Module").setRequired(true);

    public TSalesforceInputProperties() {
        setupLayout();
    }

    public void setupLayout() {
        connection.setLayout(Layout.create().setRow(1));
        module.setLayout(Layout.create().setRow(2));
    }
}
