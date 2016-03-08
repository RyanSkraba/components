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
package org.talend.components.salesforce.tsalesforcebulkexec;

import static org.talend.daikon.properties.PropertyFactory.*;
import static org.talend.daikon.properties.presentation.Widget.*;

import org.talend.components.salesforce.SalesforceBulkProperties;
import org.talend.components.salesforce.SalesforceOutputProperties;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.presentation.Form;

public class TSalesforceBulkExecProperties extends SalesforceOutputProperties {

    public Property bulkFilePath = newProperty("bulkFilePath");

    public SalesforceBulkProperties bulkProperties = new SalesforceBulkProperties("bulkProperties");

    public TSalesforceBulkExecProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(bulkFilePath);

        Form advancedForm = getForm(Form.ADVANCED);
        advancedForm.addRow(widget(bulkProperties.getForm(Form.MAIN).setName("bulkProperties")));
    }

}
