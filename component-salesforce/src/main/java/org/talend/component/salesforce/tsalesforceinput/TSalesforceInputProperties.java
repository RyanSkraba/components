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
import org.talend.component.properties.layout.Layout;
import org.talend.component.salesforce.tsalesforceconnect.TSalesforceConnectProperties;

/**
 * created by sgandon on 21 août 2015 Detailled comment
 *
 */
public class TSalesForceInputProperties extends ComponentProperties {

    Property<TSalesforceConnectProperties> connection = new Property<TSalesforceConnectProperties>("connection", "Connection")
            .setRequired(true).setValue(new TSalesforceConnectProperties());

    Property<String> module = new Property<String>("module", "Module").setRequired(true);

    public TSalesForceInputProperties() {
        setupLayout();
    }

    /**
     * DOC sgandon Comment method "setupLayout".
     */
    public void setupLayout() {
        connection.setLayout(Layout.create().setGroup(PAGE_MAIN).setRow(1));
        module.setLayout(Layout.create().setGroup(PAGE_MAIN).setRow(2));
    }
}
