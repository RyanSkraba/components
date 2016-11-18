// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.components.jms;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.junit.Test;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.common.datastore.DatastoreProperties;
import org.talend.components.jms.runtime_1_1.JmsDatastoreRuntime;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jms.ConnectionFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JmsDatastoreRuntimeTestIT {

    private final JmsDatastoreRuntime datastoreRuntime = new JmsDatastoreRuntime();

    /**
     * Check {@link JmsDatastoreRuntime#doHealthChecks(RuntimeContainer)}
     * returns //TODO
     */
    @Test
    public void testDoHealthChecks() {
        JmsDatastoreProperties props = new JmsDatastoreProperties("test");
        props.serverUrl.setValue("tcp://localhost:61616");
        datastoreRuntime.initialize(null, props);
        Iterable<ValidationResult> healthResult = datastoreRuntime.doHealthChecks(null);
        assertEquals(Arrays.asList(ValidationResult.OK), healthResult);
    }

    /**
     * Check
     * Returns OK
     */
    @Test
    public void testInitialize() {
        ValidationResult result = datastoreRuntime.initialize(null, null);
        assertEquals(ValidationResult.OK, result);
    }

    /**
     * Check {@link JmsDatastoreRuntime#getConnectionFactory()}
     * Returns // TODO
     */
    @Test
    public void testGetConnectionFactory() {
        JmsDatastoreProperties props = new JmsDatastoreProperties("test");
        props.contextProvider.setValue("org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        props.version.setValue(JmsDatastoreProperties.JmsVersion.V_1_1);
        props.serverUrl.setValue("tcp://localhost:61616");
        datastoreRuntime.initialize(null, props);
        ConnectionFactory connectionFactory = datastoreRuntime.getConnectionFactory();
        assertNotNull(connectionFactory);
    }
}
