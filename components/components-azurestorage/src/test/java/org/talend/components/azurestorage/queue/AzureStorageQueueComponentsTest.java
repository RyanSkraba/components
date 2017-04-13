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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.azurestorage.queue.runtime.AzureStorageQueueSourceOrSink;
import org.talend.components.azurestorage.queue.tazurestoragequeuecreate.TAzureStorageQueueCreateProperties;
import org.talend.components.azurestorage.queue.tazurestoragequeueinput.TAzureStorageQueueInputDefinition;
import org.talend.components.azurestorage.queue.tazurestoragequeueinput.TAzureStorageQueueInputProperties;
import org.talend.components.azurestorage.queue.tazurestoragequeuelist.TAzureStorageQueueListDefinition;
import org.talend.components.azurestorage.queue.tazurestoragequeuelist.TAzureStorageQueueListProperties;
import org.talend.components.azurestorage.queue.tazurestoragequeueoutput.TAzureStorageQueueOutputDefinition;
import org.talend.components.azurestorage.queue.tazurestoragequeueoutput.TAzureStorageQueueOutputProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

public class AzureStorageQueueComponentsTest {

    private AzureStorageQueueProperties props;

    private AzureStorageQueueDefinition def;
    
    private static final I18nMessages i18nMessages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(AzureStorageQueueSourceOrSink.class);

    @Before
    public void setup() {
        def = (AzureStorageQueueDefinition) new TAzureStorageQueueListDefinition();
        props = new AzureStorageQueueProperties("test");
        props.setupProperties();
        props.connection.setupProperties();
        props.connection.setupLayout();
    }

    /**
     *
     * @see org.talend.components.azurestorage.queue.AzureStorageQueueProperties#setupLayout()
     */
    @Test
    public void setupLayout() {
        props.setupLayout();
    }

    /**
     *
     * @see org.talend.components.azurestorage.queue.AzureStorageQueueProperties#getAllSchemaPropertiesConnectors(boolean)
     */
    @Test
    public void getAllSchemaPropertiesConnectors() {
        assertEquals(Collections.singleton(props.MAIN_CONNECTOR), props.getAllSchemaPropertiesConnectors(true));
        assertEquals(Collections.emptySet(), props.getAllSchemaPropertiesConnectors(false));
    }

    /**
     *
     * @see org.talend.components.azurestorage.queue.AzureStorageQueueProperties#getConnectionProperties()
     */
    @Test
    public void getConnectionProperties() {
        TAzureStorageConnectionProperties connectionproperties = props.getConnectionProperties();
        assertNotNull("connectionproperties cannot be null", connectionproperties);
    }

    @Test
    public void testQueueNameValidation() {
        ValidationResult vrEmpty = new ValidationResult().setStatus(Result.ERROR).setMessage(i18nMessages.getMessage("error.NameEmpty"));
        ValidationResult vrSize = new ValidationResult().setStatus(Result.ERROR)
                .setMessage(i18nMessages.getMessage("error.LengthError"));
        ValidationResult vrDash = new ValidationResult().setStatus(Result.ERROR)
                .setMessage(i18nMessages.getMessage("error.TwoDashError"));
        ValidationResult vrName = new ValidationResult().setStatus(Result.ERROR)
                .setMessage(i18nMessages.getMessage("error.QueueNameError"));
        //
        TAzureStorageQueueCreateProperties properties = new TAzureStorageQueueCreateProperties("test");
        properties.connection.accountName.setValue("dummy");
        properties.connection.accountKey.setValue("dummy");
        properties.setupProperties();
        AzureStorageQueueSourceOrSink sos = new AzureStorageQueueSourceOrSink();
        // empty queue name
        sos.initialize(null, properties);
        assertEquals(vrEmpty.message, sos.validate(null).getMessage());
        // invalid queue size
        properties.queueName.setValue("in");
        sos.initialize(null, properties);
        assertEquals(vrSize.getMessage(), sos.validate(null).getMessage());
        properties.queueName.setValue("a-too-long-queue-name-a-too-long-queue-name-a-too-long-queue-name");
        sos.initialize(null, properties);
        assertEquals(vrSize.getMessage(), sos.validate(null).getMessage());
        // invalid queue name dashes
        properties.queueName.setValue("in--in");
        sos.initialize(null, properties);
        assertEquals(vrDash.getMessage(), sos.validate(null).getMessage());
        // invalid queue name
        properties.queueName.setValue("a-wrongQueueName");
        sos.initialize(null, properties);
        assertEquals(vrName.getMessage(), sos.validate(null).getMessage());
        // a good queue name
        properties.queueName.setValue("a-good-queue-name");
        sos.initialize(null, properties);
        assertEquals(ValidationResult.OK.getStatus(), sos.validate(null).getStatus());
    }

    @Test
    public void testQueueInputProperties() {
        TAzureStorageQueueInputProperties properties = new TAzureStorageQueueInputProperties("test");
        properties.connection.accountName.setValue("dummy");
        properties.connection.accountKey.setValue("dummy");
        properties.setupProperties();
        properties.queueName.setValue("queueok");
        AzureStorageQueueSourceOrSink sos = new AzureStorageQueueSourceOrSink();
        // number of messages
        properties.numberOfMessages.setValue(-1);
        sos.initialize(null, properties);
        assertEquals(ValidationResult.Result.ERROR, sos.validate(null).getStatus());
        properties.numberOfMessages.setValue(0);
        sos.initialize(null, properties);
        assertEquals(ValidationResult.Result.ERROR, sos.validate(null).getStatus());
        properties.numberOfMessages.setValue(1001);
        sos.initialize(null, properties);
        assertEquals(ValidationResult.Result.ERROR, sos.validate(null).getStatus());
        properties.numberOfMessages.setValue(1);
        sos.initialize(null, properties);
        assertEquals(ValidationResult.OK.getStatus(), sos.validate(null).getStatus());
        properties.numberOfMessages.setValue(32);
        sos.initialize(null, properties);
        assertEquals(ValidationResult.OK.getStatus(), sos.validate(null).getStatus());
        // visibility timeout
        properties.visibilityTimeoutInSeconds.setValue(-1);
        sos.initialize(null, properties);
        assertEquals(ValidationResult.Result.ERROR, sos.validate(null).getStatus());
        properties.visibilityTimeoutInSeconds.setValue(1);
        sos.initialize(null, properties);
        assertEquals(ValidationResult.OK.getStatus(), sos.validate(null).getStatus());
    }

    @Test
    public void testQueueListProperties() {
        TAzureStorageQueueListProperties properties = new TAzureStorageQueueListProperties("test");
        properties.connection.accountName.setValue("dummy");
        properties.connection.accountKey.setValue("dummy");
        properties.setupProperties();
        AzureStorageQueueSourceOrSink sos = new AzureStorageQueueSourceOrSink();
        sos.initialize(null, properties);
        assertEquals(ValidationResult.OK.getStatus(), sos.validate(null).getStatus());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testQueueDefinitions() {
        assertEquals("Cloud/Azure Storage/Queue", def.getFamilies()[0]);
        assertEquals(TAzureStorageQueueListProperties.class, ((AzureStorageQueueDefinition) def).getPropertyClass());
        assertEquals(new Class[] { TAzureStorageConnectionProperties.class, AzureStorageQueueProperties.class },
                def.getNestedCompatibleComponentPropertiesClass());
        // runtime
        assertNotNull(def.getRuntimeInfo(null, null, ConnectorTopology.NONE));
        assertNotNull(def.getRuntimeInfo(null, null, ConnectorTopology.INCOMING));
        // connector topologies
        assertEquals(EnumSet.of(ConnectorTopology.OUTGOING), def.getSupportedConnectorTopologies());
        assertEquals(EnumSet.of(ConnectorTopology.INCOMING, ConnectorTopology.INCOMING_AND_OUTGOING),
                new TAzureStorageQueueOutputDefinition().getSupportedConnectorTopologies());
        // schema propagation
        assertTrue(def.isSchemaAutoPropagate());
        assertTrue(new TAzureStorageQueueInputDefinition().isSchemaAutoPropagate());
        assertTrue(new TAzureStorageQueueOutputDefinition().isSchemaAutoPropagate());
    }

    @Test
    public void testTAzureStorageQueueInputProperties() {
        TAzureStorageQueueInputProperties ip = new TAzureStorageQueueInputProperties("test");
        ip.connection.setupProperties();
        ip.setupProperties();
        ip.peekMessages.setValue(false);
        ip.afterPeekMessages();
        assertFalse(ip.deleteMessages.getValue());
        ip.deleteMessages.setValue(true);
        ip.afterDeleteMessages();
        assertTrue(ip.deleteMessages.getValue());
        assertFalse(ip.peekMessages.getValue());
    }

    @Test
    public void testTAzureStorageQueueOutputProperties() {
        TAzureStorageQueueOutputProperties op = new TAzureStorageQueueOutputProperties("test");
        op.setupProperties();
        HashSet<PropertyPathConnector> connectors = new HashSet<>();
        connectors.add(op.FLOW_CONNECTOR);
        assertEquals(connectors, op.getAllSchemaPropertiesConnectors(true));
        connectors.clear();
        connectors.add(op.MAIN_CONNECTOR);
        assertEquals(connectors, op.getAllSchemaPropertiesConnectors(false));

    }
}
