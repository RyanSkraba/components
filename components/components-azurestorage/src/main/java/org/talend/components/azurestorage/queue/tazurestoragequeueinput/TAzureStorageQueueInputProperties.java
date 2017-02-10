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
package org.talend.components.azurestorage.queue.tazurestoragequeueinput;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.talend.components.azurestorage.queue.AzureStorageQueueProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class TAzureStorageQueueInputProperties extends AzureStorageQueueProperties {

    private static final long serialVersionUID = 3741225803254131588L;

    /**
     * Just peek message. Don't change thevisibility (see visibilityTimeoutInSeconds) and don't increase dequeue count.
     */
    public Property<Boolean> peekMessages = PropertyFactory.newBoolean("peekMessages");

    /** Delete messages after retrieving them. Don't work with the peek option. */
    public Property<Boolean> deleteMessages = PropertyFactory.newBoolean("deleteMessages");

    /** Fetch n messages. n should be between 1 and 32. */
    public Property<Integer> numberOfMessages = PropertyFactory.newInteger("numberOfMessages");

    /** Set visibility timeout after retrieving messages. Default is 30s. */
    public Property<Integer> visibilityTimeoutInSeconds = PropertyFactory.newInteger("visibilityTimeoutInSeconds");

    public TAzureStorageQueueInputProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        Schema s = SchemaBuilder.builder().record("Main").fields()//
                .name(FIELD_MESSAGE_ID).prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true")
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "100").type(AvroUtils._string()).noDefault()//
                .name(FIELD_MESSAGE_CONTENT).type(AvroUtils._string()).noDefault() //
                .name(FIELD_INSERTION_TIME).prop(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd hh:mm:ss")
                .type(AvroUtils._date()).noDefault() //
                .name(FIELD_EXPIRATION_TIME).prop(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd hh:mm:ss")
                .type(AvroUtils._date()).noDefault() //
                .name(FIELD_NEXT_VISIBLE_TIME).prop(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd hh:mm:ss")
                .type(AvroUtils._date()).noDefault() //
                .name(FIELD_DEQUEUE_COUNT).type(AvroUtils._int()).noDefault() //
                .name(FIELD_POP_RECEIPT).type(AvroUtils._string()).noDefault() //
                .endRecord();
        schema.schema.setValue(s);
        numberOfMessages.setValue(32);
        peekMessages.setValue(true);
        deleteMessages.setValue(false);
        visibilityTimeoutInSeconds.setValue(30);
    }

    @Override
    public void setupLayout() {
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(connection.getForm(Form.REFERENCE));
        mainForm.addRow(queueName);
        mainForm.addRow(schema.getForm(Form.REFERENCE));
        //
        mainForm.addRow(numberOfMessages);
        mainForm.addColumn(peekMessages);
        mainForm.addColumn(deleteMessages);
        //
        mainForm.addRow(dieOnError);

        Form advanced = new Form(this, Form.ADVANCED);
        advanced.addRow(visibilityTimeoutInSeconds);
    }

    public void afterPeekMessages() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterDeleteMessages() {
        refreshLayout(getForm(Form.MAIN));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        if (peekMessages.getValue()) {
            deleteMessages.setValue(false);
        }
        if (deleteMessages.getValue()) {
            peekMessages.setValue(false);
        }
    }
}
