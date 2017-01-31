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
package org.talend.components.api.component;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.EnumSet;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.exception.error.ComponentsErrorCode;
import org.talend.components.api.testcomponent.TestComponentDefinition;
import org.talend.components.api.testcomponent.TestComponentProperties;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

public class ComponentDefinitionTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test() {
        TestComponentDefinition cd = new TestComponentDefinition();

        TestComponentProperties prop = (TestComponentProperties) cd.createRuntimeProperties();
        assertNotNull(prop.initLater);
        assertNull(prop.mainForm);
    }

    @Test
    public void testRuntimeEngine() {
        TestComponentDefinition cd = new TestComponentDefinition();
        assertThat(cd.getSupportedExecutionEngines(), containsInAnyOrder(ExecutionEngine.DI));
        assertTrue(cd.isSupportingExecutionEngines(ExecutionEngine.DI));

        // Nothing is returned, but there isn't any exception.
        RuntimeInfo ri = cd.getRuntimeInfo(ExecutionEngine.DI, null, null);
        assertThat(ri, nullValue());

        // Requesting a wrong execution engine causes an exception.
        thrown.expect(TalendRuntimeException.class);
        thrown.expect(hasProperty("code", is(ComponentsErrorCode.WRONG_EXECUTION_ENGINE)));
        thrown.expectMessage("WRONG_EXECUTION_ENGINE:{component=TestComponent, requested=BEAM, available=[DI]}");
        ri = cd.getRuntimeInfo(ExecutionEngine.BEAM, null, null);

        fail("An exception must have been thrown.");
    }

    @Test
    public void testConnectorTopologyEngine_ok() {
        TestComponentDefinition cd = new TestComponentDefinition() {

            @Override
            public Set<ConnectorTopology> getSupportedConnectorTopologies() {
                return EnumSet.of(ConnectorTopology.INCOMING, ConnectorTopology.OUTGOING);
            }
        };
        // The assert will no thorw an exception on these two cases
        cd.assertConnectorTopologyCompatibility(ConnectorTopology.INCOMING);
        cd.assertConnectorTopologyCompatibility(ConnectorTopology.OUTGOING);
    }

    @Test(expected = TalendRuntimeException.class)
    public void testConnectorTopologyEngine_Error1() {
        TestComponentDefinition cd = new TestComponentDefinition() {

            @Override
            public Set<ConnectorTopology> getSupportedConnectorTopologies() {
                return EnumSet.of(ConnectorTopology.INCOMING, ConnectorTopology.OUTGOING);
            }
        };
        // The assert will throw an exception on this cases
        cd.assertConnectorTopologyCompatibility(ConnectorTopology.INCOMING_AND_OUTGOING);
    }

    @Test(expected = TalendRuntimeException.class)
    public void testConnectorTopologyEngine_Error2() {
        TestComponentDefinition cd = new TestComponentDefinition() {

            @Override
            public Set<ConnectorTopology> getSupportedConnectorTopologies() {
                return EnumSet.of(ConnectorTopology.INCOMING, ConnectorTopology.OUTGOING);
            }
        };
        // The assert will throw an exception on this cases
        cd.assertConnectorTopologyCompatibility(ConnectorTopology.NONE);
    }

    @Test
    public void testAllExecutionEngine() {
        // A component that supports all execution engines.
        TestComponentDefinition cd = new TestComponentDefinition(true);
        for (ExecutionEngine engine : ExecutionEngine.values()) {
            assertThat(cd.getSupportedExecutionEngines(), hasItem(engine));
            assertTrue(cd.isSupportingExecutionEngines(engine));

            // Nothing is returned, but there isn't any exception.
            RuntimeInfo ri = cd.getRuntimeInfo(engine, null, null);
            assertThat(ri, nullValue());
        }
    }

    @Test
    public void testNoExecutionEngine() {
        // A component that supports no execution engines.
        TestComponentDefinition cd = new TestComponentDefinition(false);
        assertThat(cd.getSupportedExecutionEngines(), hasSize(0));
    }

    @Test
    public void testi18NForComponentDefintion() {
        TestComponentDefinition tcd = new TestComponentDefinition();
        assertEquals("Test Component", tcd.getDisplayName());
        assertEquals("Ze Test Component Title", tcd.getTitle());
    }

    @Test
    public void testReturnProperties() {
        TestComponentDefinition tcd = new TestComponentDefinition();
        Property[] props = tcd.getReturnProperties();
        assertEquals("return1", props[0].getName());
        assertEquals(5, props.length);

        // Make sure i18N works
        assertEquals("Error Message", props[1].getDisplayName());
        assertEquals("Number of line", props[2].getDisplayName());
        assertEquals("Number of success", props[3].getDisplayName());
        assertEquals("Number of reject", props[4].getDisplayName());
    }

}
