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
package org.talend.components.azurestorage.queue.tazurestoragequeuelist;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.talend.components.azurestorage.queue.AzureStorageQueueProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.presentation.Form;

public class TAzureStorageQueueListProperties extends AzureStorageQueueProperties {

    private static final long serialVersionUID = 3395812871526057697L;

    public TAzureStorageQueueListProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        Schema s = SchemaBuilder.record("QueueList").fields().name("QueueName").prop(SchemaConstants.TALEND_IS_LOCKED, "true")
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "63").type(AvroUtils._string()).noDefault().endRecord();
        schema.schema.setValue(s);
    }

    @Override
    public void setupLayout() {
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(connection.getForm(Form.REFERENCE));
        mainForm.addRow(schema.getForm(Form.REFERENCE));
        mainForm.addRow(dieOnError);
    }
}
