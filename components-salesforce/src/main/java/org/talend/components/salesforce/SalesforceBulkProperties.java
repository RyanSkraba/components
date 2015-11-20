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

import static org.talend.components.api.schema.SchemaFactory.*;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.schema.SchemaElement;

public class SalesforceBulkProperties extends ComponentProperties {

    public static final String CONCURRENCY_PARALLEL = "Parallel";

    public static final String CONCURRENCY_SERIAL = "Serial";

    public SchemaElement concurrencyMode = newEnum("concurrencyMode", CONCURRENCY_PARALLEL, CONCURRENCY_SERIAL);

    public SchemaElement bytesToCommit = newInteger("bytesToCommit", "10485760");

    public SchemaElement rowsToCommit = newInteger("rowsToCommit", "10000");

    public SchemaElement waitTimeCheckBatchState = newInteger("waitTimeCheckBatchState");

    public SalesforceBulkProperties(String name) {
        super(name);
    }

    @Override
    public SalesforceBulkProperties init() {
        super.init();
        return this;
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = Form.create(this, Form.MAIN, "Salesforce Bulk Properties");
        mainForm.addRow(concurrencyMode);
        mainForm.addRow(rowsToCommit);
        mainForm.addRow(bytesToCommit);
        mainForm.addRow(waitTimeCheckBatchState);
    }

}
