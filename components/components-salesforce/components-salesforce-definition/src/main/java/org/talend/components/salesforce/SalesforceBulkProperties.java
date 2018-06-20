// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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

import static org.talend.daikon.properties.property.PropertyFactory.newBoolean;
import static org.talend.daikon.properties.property.PropertyFactory.newEnum;
import static org.talend.daikon.properties.property.PropertyFactory.newInteger;

import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;

public class SalesforceBulkProperties extends ComponentPropertiesImpl {

    public Property<Concurrency> concurrencyMode = newEnum("concurrencyMode", Concurrency.class);

    public Property<Integer> bytesToCommit = newInteger("bytesToCommit", "10485760");

    public Property<Integer> rowsToCommit = newInteger("rowsToCommit", "10000");

    public Property<Integer> waitTimeCheckBatchState = newInteger("waitTimeCheckBatchState");

    public Property<Boolean> bulkApiV2 = newBoolean("bulkApiV2");

    public Property<ColumnDelimiter> columnDelimiter = newEnum("columnDelimiter", ColumnDelimiter.class);

    public Property<LineEnding> lineEnding = newEnum("lineEnding", LineEnding.class);

    public SalesforceBulkProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        waitTimeCheckBatchState.setValue(10000);
        columnDelimiter.setValue(ColumnDelimiter.COMMA);
        lineEnding.setValue(LineEnding.CRLF);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = Form.create(this, Form.MAIN);
        mainForm.addRow(bulkApiV2);
        mainForm.addRow(columnDelimiter);
        mainForm.addColumn(lineEnding);
        mainForm.addRow(concurrencyMode);
        mainForm.addRow(rowsToCommit);
        mainForm.addColumn(bytesToCommit);
        mainForm.addRow(waitTimeCheckBatchState);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        if (Form.MAIN.equals(form.getName())) {
            boolean useBulkApiV2 = bulkApiV2.getValue();
            form.getWidget(rowsToCommit.getName()).setVisible(!useBulkApiV2);
            form.getWidget(bytesToCommit.getName()).setVisible(!useBulkApiV2);
            form.getWidget(concurrencyMode.getName()).setVisible(!useBulkApiV2);
            form.getWidget(columnDelimiter.getName()).setVisible(useBulkApiV2);
            form.getWidget(lineEnding.getName()).setVisible(useBulkApiV2);
        }
    }

    public void afterBulkApiV2(){
        refreshLayout(getForm(Form.MAIN));
    }

    public enum Concurrency {
        Parallel,
        Serial
    }

    public enum ColumnDelimiter {
        BACKQUOTE,
        CARET,
        COMMA,
        PIPE,
        SEMICOLON,
        TAB,
    }

    public enum LineEnding {
        LF,
        CRLF
    }

}
