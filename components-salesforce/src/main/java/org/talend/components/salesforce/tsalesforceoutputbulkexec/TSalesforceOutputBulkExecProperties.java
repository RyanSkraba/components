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
package org.talend.components.salesforce.tsalesforceoutputbulkexec;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.salesforce.SalesforceBulkProperties;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.SalesforceModuleProperties;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;

import static org.talend.components.api.properties.presentation.Widget.widget;

public class TSalesforceOutputBulkExecProperties extends TSalesforceOutputProperties {

    @Override
    public ComponentProperties init() {
        TSalesforceOutputProperties.setupUpsertRelation(upsertRelation, TSalesforceOutputProperties.POLY);
        super.init();
        return this;
    }

    public SalesforceBulkProperties bulkProperties = new SalesforceBulkProperties();

    @Override public void setupLayout() {
        super.setupLayout();
        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(widget(bulkProperties.getForm(Form.MAIN).setName("bulkProperties")));
    }
}
