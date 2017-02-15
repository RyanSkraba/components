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
package org.talend.components.azurestorage.queue;

import java.util.Collections;
import java.util.Set;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.azurestorage.AzureStorageProvideConnectionProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class AzureStorageQueueProperties extends FixedConnectorsComponentProperties
        implements AzureStorageProvideConnectionProperties {

    private static final long serialVersionUID = 858597956478343860L;

    public TAzureStorageConnectionProperties connection = new TAzureStorageConnectionProperties("connection");

    protected transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "schema");

    public SchemaProperties schema = new SchemaProperties("schema");

    public Property<String> queueName = PropertyFactory.newString("queueName");

    public Property<Boolean> dieOnError = PropertyFactory.newBoolean("dieOnError");

    /**
     * The maximum time to allow the message to be in the queue. A value of zero will set the time-to-live to the
     * service default value of seven days.
     */
    public Property<Integer> timeToLiveInSeconds = PropertyFactory.newInteger("timeToLiveInSeconds");

    /**
     * The length of time during which the message will be invisible, starting when it is added to the queue, or 0 to
     * make the message visible immediately. This value must be greater than or equal to zero and less than or equal to
     * the time-to-live value.
     */
    public Property<Integer> initialVisibilityDelayInSeconds = PropertyFactory.newInteger("initialVisibilityDelayInSeconds");

    public static final String FIELD_MESSAGE_ID = "MessageId";

    public static final String FIELD_MESSAGE_CONTENT = "MessageContent";

    public static final String FIELD_INSERTION_TIME = "InsertionTime";

    public static final String FIELD_EXPIRATION_TIME = "ExpirationTime";

    public static final String FIELD_DEQUEUE_COUNT = "DequeueCount";

    public static final String FIELD_SIZE = "Size";

    public static final String FIELD_POP_RECEIPT = "PopReceipt";

    public static final String FIELD_NEXT_VISIBLE_TIME = "NextVisibleTime";

    public AzureStorageQueueProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        queueName.setValue("");
        timeToLiveInSeconds.setValue(0);
        initialVisibilityDelayInSeconds.setValue(0);
        dieOnError.setValue(true);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(connection.getForm(Form.REFERENCE));
        mainForm.addRow(queueName);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputComponent) {
        if (isOutputComponent) {
            return Collections.singleton(MAIN_CONNECTOR);
        }
        return Collections.emptySet();
    }

    @Override
    public TAzureStorageConnectionProperties getConnectionProperties() {
        return this.connection.getConnectionProperties();
    }
}
