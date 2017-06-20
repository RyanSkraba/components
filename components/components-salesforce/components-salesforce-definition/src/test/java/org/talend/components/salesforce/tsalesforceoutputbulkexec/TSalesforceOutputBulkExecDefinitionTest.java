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

package org.talend.components.salesforce.tsalesforceoutputbulkexec;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.SalesforceModuleProperties;
import org.talend.components.salesforce.SalesforceTestBase;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 *
 */
public class TSalesforceOutputBulkExecDefinitionTest extends SalesforceTestBase {

    private TSalesforceOutputBulkExecDefinition definition;

    private TSalesforceOutputBulkExecProperties properties;

    @Before
    public void setUp() {
        definition = new TSalesforceOutputBulkExecDefinition();

        properties = new TSalesforceOutputBulkExecProperties("root");
        properties.init();
    }

    @Test
    public void testComponents() {
        ComponentDefinition inputDefinition = definition.getInputComponentDefinition();
        assertNotNull(inputDefinition);
        assertTrue(inputDefinition.getSupportedConnectorTopologies().contains(ConnectorTopology.INCOMING));

        ComponentDefinition outputDefinition = definition.getOutputComponentDefinition();
        assertNotNull(outputDefinition);
        assertTrue(outputDefinition.getSupportedConnectorTopologies().contains(ConnectorTopology.OUTGOING));
    }

    @Test
    public void testRuntimeInfo() {
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(
                ExecutionEngine.DI, properties, ConnectorTopology.INCOMING_AND_OUTGOING);
        assertThat(runtimeInfo, nullValue(RuntimeInfo.class));
    }

    @Test
    public void testSupportedConnectorTopologies() {
        Set<ConnectorTopology> topologySet = definition.getSupportedConnectorTopologies();
        assertThat(topologySet, hasItems(ConnectorTopology.INCOMING_AND_OUTGOING));
    }

    @Test
    public void testNestedCompatibleComponentPropertiesClass() {
        assertThat(definition.getNestedCompatibleComponentPropertiesClass().length, is(2));
        Collection<Class<? extends ComponentProperties>> properties = Arrays.asList(
                definition.getNestedCompatibleComponentPropertiesClass());
        assertTrue(properties.contains(SalesforceConnectionProperties.class));
        assertTrue(properties.contains(SalesforceModuleProperties.class));
    }

    @Test
    public void testNotStartable() {
        assertFalse(definition.isStartable());
    }

    @Test
    public void testConditionsInputs() {
        assertTrue(definition.isConditionalInputs());
    }

    @Test
    public void testSchemaAutoPropagate() {
        assertFalse(definition.isSchemaAutoPropagate());
    }

    @Test
    public void testPartitioning() {
        assertEquals(AbstractComponentDefinition.NONE, definition.getPartitioning());
    }
}
