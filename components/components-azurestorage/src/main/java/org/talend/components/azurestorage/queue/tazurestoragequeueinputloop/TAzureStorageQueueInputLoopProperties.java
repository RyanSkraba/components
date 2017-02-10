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
package org.talend.components.azurestorage.queue.tazurestoragequeueinputloop;

import org.talend.components.azurestorage.queue.tazurestoragequeueinput.TAzureStorageQueueInputProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class TAzureStorageQueueInputLoopProperties extends TAzureStorageQueueInputProperties {

    private static final long serialVersionUID = 3741225553254131588L;

    public Property<Integer> loopWaitTime = PropertyFactory.newInteger("loopWaitTime");

    public TAzureStorageQueueInputLoopProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        numberOfMessages.setValue(32);
        loopWaitTime.setValue(5);
    }

    @Override
    public void setupLayout() {
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(connection.getForm(Form.REFERENCE));
        mainForm.addRow(queueName);
        mainForm.addRow(schema.getForm(Form.REFERENCE));
        //
        mainForm.addRow(numberOfMessages);
        mainForm.addRow(loopWaitTime);
        //
        mainForm.addRow(dieOnError);
    }

}
