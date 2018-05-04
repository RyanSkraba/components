// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.marketo.data;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.SupportedProduct;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.marketo.MarketoComponentDefinition;

public class MarketoInputDefinitionTest {

    MarketoInputDefinition definition;

    private MarketoInputProperties properties;

    @Before
    public void setUp() throws Exception {
        definition = new MarketoInputDefinition();
        properties = new MarketoInputProperties("test");
    }

    @Test
    public void testGetPropertyClass() throws Exception {
        assertEquals(MarketoInputProperties.class, definition.getPropertyClass());
    }

    @Test
    public void testGetRuntimeInfo() throws Exception {
        assertEquals(MarketoComponentDefinition.RUNTIME_DATASET_SOURCE,
                definition.getRuntimeInfo(ExecutionEngine.DI, properties, ConnectorTopology.OUTGOING).getRuntimeClassName());
    }

    @Test
    public void testGetSupportedConnectorTopologies() throws Exception {
        assertThat(definition.getSupportedConnectorTopologies(), contains(ConnectorTopology.OUTGOING));
    }

    @Test
    public void testGetSupportedProducts() throws Exception {
        assertThat(definition.getSupportedProducts(), contains(SupportedProduct.DATAPREP, SupportedProduct.DATASTREAMS));
    }
}
