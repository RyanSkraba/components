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

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.presentation.Form;

public class SalesforceBulkProperties extends ComponentProperties {

    enum Concurrency {
        PARALLEL,
        SERIAL;
    }

    public static final String CONCURRENCY_PARALLEL = "Parallel";

    public static final String CONCURRENCY_SERIAL = "Serial";

    public Property<Concurrency> concurrencyMode = newEnum("concurrencyMode", new TypeLiteral<Concurrency>() {// empty
                                                                                                              // on
                                                                                                              // purpose
    });

    public Property<Integer> bytesToCommit = newInteger("bytesToCommit", "10485760");

    public Property<Integer> rowsToCommit = newInteger("rowsToCommit", "10000");

    public Property<Integer> waitTimeCheckBatchState = newInteger("waitTimeCheckBatchState");

    public SalesforceBulkProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        waitTimeCheckBatchState.setValue(10000);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = Form.create(this, Form.MAIN);
        mainForm.addRow(concurrencyMode);
        mainForm.addRow(rowsToCommit);
        mainForm.addColumn(bytesToCommit);
        mainForm.addRow(waitTimeCheckBatchState);
    }

}
