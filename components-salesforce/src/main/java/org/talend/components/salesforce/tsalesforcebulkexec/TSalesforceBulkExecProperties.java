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

import static org.talend.components.api.schema.SchemaFactory.newProperty;

import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;

import java.util.ArrayList;
import java.util.List;

public class TSalesforceBulkExecProperties extends TSalesforceOutputProperties {

    public static final String CONCURRENCY_PARALLEL = "Parallel";

    public static final String CONCURRENCY_SERIAL = "Serial";

    public SchemaElement bulkFilePath = newProperty("bulkFilePath");

    public SchemaElement concurrencyMode = newProperty(Type.ENUM, "concurrencyMode");

    public SchemaElement bytesToCommit = newProperty(Type.INT, "bytesToCommit");

    public SchemaElement waitTimeCheckBatchState = newProperty(Type.INT, "waitTimeCheckBatchState");

    @Override
    public TSalesforceOutputProperties init() {
        List<String> l = new ArrayList<>();
        l.add(CONCURRENCY_PARALLEL);
        l.add(CONCURRENCY_SERIAL);
        concurrencyMode.setPossibleValues(l);
        return this;
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(bulkFilePath);
        mainForm.addRow(concurrencyMode);
        mainForm.addRow(bytesToCommit);
        mainForm.addRow(waitTimeCheckBatchState);
    }

}
