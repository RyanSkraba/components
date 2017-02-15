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
package org.talend.components.filterrow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.filterrow.runtime.TFilterRowSink;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

public class TFilterRowDefinitionTest {

    /**
     * Check {@link TFilterRowDefinition#getFamilies()} returns string array, which contains "Processing"
     */
    @Test
    public void testGetFamilies() {
        TFilterRowDefinition definition = new TFilterRowDefinition();
        String[] families = definition.getFamilies();
        assertThat(families, arrayContaining("Processing"));
    }

    /**
     * Check {@link TFilterRowDefinition#getName()} returns "tFilterRow_POC"
     */
    @Test
    public void testGetName() {
        TFilterRowDefinition definition = new TFilterRowDefinition();
        String componentName = definition.getName();
        assertEquals(componentName, "tFilterRow_POC");
    }

    /**
     * Check {@link TFilterRowDefinition#getPropertyClass()} returns class, which canonical name is
     * "org.talend.components.filterrow.TFilterRowProperties"
     */
    @Test
    public void testGetPropertyClass() {
        TFilterRowDefinition definition = new TFilterRowDefinition();
        Class<?> propertyClass = definition.getPropertyClass();
        String canonicalName = propertyClass.getCanonicalName();
        assertThat(canonicalName, equalTo("org.talend.components.filterrow.TFilterRowProperties"));
    }

    /**
     * Check {@link TFilterRowDefinition#getRuntime()} returns instance of {@link JiraSource}
     */
    @Test
    public void testGetRuntime() {
        TFilterRowDefinition definition = new TFilterRowDefinition();
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.INCOMING_AND_OUTGOING);
        SandboxedInstance sandboxedInstance = RuntimeUtil.createRuntimeClass(runtimeInfo, definition.getClass().getClassLoader());
        Sink source = (Sink) sandboxedInstance.getInstance();
        assertThat(source, is(instanceOf(TFilterRowSink.class)));
    }

    /**
     * Check {@link TFilterRowDefinition#isSchemaAutoPropagate()} returns <code>true</code>
     */
    @Test
    public void testIsSchemaAutoPropagate() {
        TFilterRowDefinition definition = new TFilterRowDefinition();
        boolean result = definition.isSchemaAutoPropagate();
        assertTrue(result);
    }

    /**
     * Check {@link TFilterRowDefinition#isConditionalInputs()} returns <code>true</code>
     */
    @Test
    public void testIsConditionalInputs() {
        TFilterRowDefinition definition = new TFilterRowDefinition();
        boolean result = definition.isConditionalInputs();
        assertTrue(result);
    }
}
