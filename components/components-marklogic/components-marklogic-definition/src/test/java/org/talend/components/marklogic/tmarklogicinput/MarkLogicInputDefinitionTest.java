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
package org.talend.components.marklogic.tmarklogicinput;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertTrue;
import static org.talend.components.api.component.ComponentDefinition.RETURN_ERROR_MESSAGE_PROP;
import static org.talend.components.api.component.ComponentDefinition.RETURN_TOTAL_RECORD_COUNT_PROP;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

public class MarkLogicInputDefinitionTest {

    @Rule public final ExpectedException thrown = ExpectedException.none();
    MarkLogicInputDefinition definition;

    @Before
    public void setUp() {
        definition = new MarkLogicInputDefinition();
    }
    @Test
    public void testGetFamilies() {
        String[] actual = definition.getFamilies();

        assertThat(Arrays.asList(actual), contains("Databases/MarkLogic", "Big Data/MarkLogic"));
    }

    @Test
    public void testGetPropertyClass() {
        Class<?> propertyClass = definition.getPropertyClass();
        String canonicalName = propertyClass.getCanonicalName();

        assertThat(canonicalName, equalTo("org.talend.components.marklogic.tmarklogicinput.MarkLogicInputProperties"));
    }

    @Test
    public void testGetReturnProperties() {
        Property[] returnProperties = definition.getReturnProperties();
        List<Property> propertyList = Arrays.asList(returnProperties);

        assertThat(propertyList, hasSize(2));
        assertTrue(propertyList.contains(RETURN_TOTAL_RECORD_COUNT_PROP));
        assertTrue(propertyList.contains(RETURN_ERROR_MESSAGE_PROP));
    }

    @Test
    public void testGetRuntimeInfoForOutgoingTopology() {
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.OUTGOING);
        String runtimeClassName = runtimeInfo.getRuntimeClassName();
        assertThat(runtimeClassName, equalTo("org.talend.components.marklogic.runtime.input.MarkLogicSource"));
    }

    @Test
    public void testGetRuntimeInfoForIncomingAndOutgoingTopology() {
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.INCOMING_AND_OUTGOING);
        String runtimeClassName = runtimeInfo.getRuntimeClassName();
        assertThat(runtimeClassName, equalTo("org.talend.components.marklogic.runtime.input.MarkLogicInputSink"));
    }

    @Test
    public void testGetRuntimeInfoWrongEngine() {
        thrown.expect(TalendRuntimeException.class);
        thrown.expectMessage(
                "WRONG_EXECUTION_ENGINE:{component=tMarkLogicInput, requested=DI_SPARK_STREAMING, available=[DI, BEAM]}");
        definition.getRuntimeInfo(ExecutionEngine.DI_SPARK_STREAMING, null, ConnectorTopology.OUTGOING);
    }

    @Test
    public void testGetRuntimeInfoWrongTopology() {
        thrown.expect(TalendRuntimeException.class);
        thrown.expectMessage("WRONG_CONNECTOR:{component=tMarkLogicInput}");
        definition.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.INCOMING);
    }

    @Test
    public void testGetSupportedConnectorTopologies() {
        Set<ConnectorTopology> connectorTopologies = definition.getSupportedConnectorTopologies();

        assertThat(connectorTopologies, contains(ConnectorTopology.OUTGOING, ConnectorTopology.INCOMING_AND_OUTGOING, ConnectorTopology.NONE));
        assertThat(connectorTopologies,
                not((contains(ConnectorTopology.INCOMING))));
    }

    @Test
    public void testSupportsProperties() {
        MarkLogicInputProperties inputProperties = new MarkLogicInputProperties("inputProps");
        boolean isPropsSupportedByDefault = definition.supportsProperties(inputProperties);
        boolean isComponentSupportedByWizard = definition.supportsProperties(inputProperties.connection);

        assertTrue(isPropsSupportedByDefault);
        assertTrue(isComponentSupportedByWizard);
    }

}