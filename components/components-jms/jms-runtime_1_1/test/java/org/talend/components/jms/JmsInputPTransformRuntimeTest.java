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

package org.talend.components.jms;

import org.apache.beam.sdk.values.PBegin;
import org.junit.Test;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.jms.input.JmsInputProperties;
import org.talend.components.jms.runtime_1_1.JmsInputPTransformRuntime;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;

import static org.junit.Assert.assertEquals;

public class JmsInputPTransformRuntimeTest {

    private final JmsInputPTransformRuntime jmsInputPTransformRuntime = new JmsInputPTransformRuntime();

    /**
     * Check {@link JmsInputPTransformRuntime#initialize(RuntimeContainer, Properties)}
     * returns //TODO
     */
    @Test
    public void testInitialize() {
        ValidationResult result = jmsInputPTransformRuntime.initialize(null, null);
        assertEquals(ValidationResult.OK, result);
    }

    /**
     * Check {@link JmsInputPTransformRuntime#getMessageType()}
     * returns //TODO
     */
    @Test
    public void testGetMessageType() {
        JmsDatasetProperties jmsDatasetProperties = new JmsDatasetProperties("jmsDatasetProperties");
        jmsDatasetProperties.msgType.setValue(JmsMessageType.QUEUE);
        JmsInputProperties inputProperties = new JmsInputProperties("jmsInputPTransformRuntime");
        inputProperties.setDatasetProperties(jmsDatasetProperties);
        jmsInputPTransformRuntime.initialize(null, inputProperties);
        jmsInputPTransformRuntime.setMessageType();
        assertEquals(JmsMessageType.QUEUE, jmsInputPTransformRuntime.getMessageType());
    }

    /**
     * Check {@link JmsInputPTransformRuntime#apply(PBegin)}
     * returns //TODO
     */
    @Test
    public void testApply() {

    }
}
