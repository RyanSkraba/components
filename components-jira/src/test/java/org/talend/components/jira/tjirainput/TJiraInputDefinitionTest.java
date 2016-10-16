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
package org.talend.components.jira.tjirainput;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.jira.JiraDefinition;
import org.talend.components.jira.runtime.JiraSource;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

/**
 * Unit-tests for {@link TJiraInputDefinition} class
 */
public class TJiraInputDefinitionTest {

    /**
     * Check {@link TJiraInputDefinition#getFamilies()} returns string array, which contains "Business/JIRA"
     */
    @Test
    public void testGetFamilies() {
        JiraDefinition definition = new TJiraInputDefinition();
        String[] families = definition.getFamilies();
        assertThat(families, arrayContaining("Business/JIRA"));
    }

    /**
     * Check {@link TJiraInputDefinition#getName()} returns "tJIRAInput"
     */
    @Test
    public void testGetName() {
        JiraDefinition definition = new TJiraInputDefinition();
        String componentName = definition.getName();
        assertEquals(componentName, "tJIRAInput");
    }

    /**
     * Check {@link TJiraInputDefinition#getPropertyClass()} returns class, which canonical name is
     * "org.talend.components.jira.tjirainput.TJiraInputProperties"
     */
    @Test
    public void testGetPropertyClass() {
        TJiraInputDefinition definition = new TJiraInputDefinition();
        Class<?> propertyClass = definition.getPropertyClass();
        String canonicalName = propertyClass.getCanonicalName();
        assertThat(canonicalName, equalTo("org.talend.components.jira.tjirainput.TJiraInputProperties"));
    }

    /**
     * Check {@link TJiraInputDefinition#getRuntime()} returns instance of {@link JiraSource}
     */
    @Test
    public void testGetRuntime() {
        TJiraInputDefinition definition = new TJiraInputDefinition();
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(null, ConnectorTopology.OUTGOING);
        SandboxedInstance sandboxedInstance = RuntimeUtil.createRuntimeClass(runtimeInfo, definition.getClass().getClassLoader());
        Source source = (Source) sandboxedInstance.getInstance();
        assertThat(source, is(instanceOf(JiraSource.class)));
    }

    /**
     * Check {@link TJiraInputDefinition#isSchemaAutoPropagate()} returns <code>true</code>
     */
    @Test
    public void testIsSchemaAutoPropagate() {
        TJiraInputDefinition definition = new TJiraInputDefinition();
        boolean result = definition.isSchemaAutoPropagate();
        assertTrue(result);
    }
}
