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
package org.talend.components.jira.tjiraoutput;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.talend.components.jira.JiraDefinition;

/**
 * Unit-tests for {@link TJiraOutputDefinition} class
 */
public class TJiraOutputDefinitionTest {

    /**
     * Check {@link TJiraOutputDefinition#getFamilies()} returns string array, which contains "Business/JIRA"
     */
    @Test
    public void testGetFamilies() {
        JiraDefinition definition = new TJiraOutputDefinition();
        String[] families = definition.getFamilies();
        assertThat(families, arrayContaining("Business/JIRA"));
    }

    /**
     * Check {@link TJiraOutputDefinition#getName()} returns "tJIRAOutput"
     */
    @Test
    public void testGetName() {
        JiraDefinition definition = new TJiraOutputDefinition();
        String componentName = definition.getName();
        assertEquals(componentName, "tJIRAOutput");
    }

    /**
     * Check {@link TJiraOutputDefinition#getPropertyClass()} returns class, which canonical name is
     * "org.talend.components.jira.tjiraoutput.TJiraOutputProperties"
     */
    @Test
    public void testGetPropertyClass() {
        TJiraOutputDefinition definition = new TJiraOutputDefinition();
        Class<?> propertyClass = definition.getPropertyClass();
        String canonicalName = propertyClass.getCanonicalName();
        assertThat(canonicalName, equalTo("org.talend.components.jira.tjiraoutput.TJiraOutputProperties"));
    }

    /**
     * Check {@link TJiraOutputDefinition#getRuntime()} returns instance of {@link JiraSource}
     */
    // @Test
    // public void testGetRuntime() {
    // TJiraOutputDefinition definition = new TJiraOutputDefinition();
    // RuntimeInfo runtimeInfo = definition.getRuntimeInfo(null, ConnectorTopology.OUTPUT);
    // SandboxedInstance sandboxedInstance = RuntimeServiceProvider.getRuntimeService().createRuntimeClass(runtimeInfo,
    // definition.getClass().getClassLoader());
    // Sink sink = (Sink) sandboxedInstance.getInstance();
    // assertThat(sink, is(instanceOf(JiraSink.class)));
    // }

    /**
     * Check {@link TJiraOutputDefinition#isSchemaAutoPropagate()} returns <code>true</code>
     */
    @Test
    public void testIsSchemaAutoPropagate() {
        TJiraOutputDefinition definition = new TJiraOutputDefinition();
        boolean result = definition.isSchemaAutoPropagate();
        assertTrue(result);
    }
}
