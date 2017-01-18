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
package org.talend.components.jira.runtime.reader;

import static org.junit.Assert.assertEquals;
import static org.talend.components.jira.testutils.JiraTestConstants.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.common.DefinitionRegistry;
import org.talend.components.api.service.common.ComponentServiceImpl;
import org.talend.components.jira.runtime.JiraSource;
import org.talend.components.jira.tjirainput.TJiraInputDefinition;
import org.talend.components.jira.tjirainput.TJiraInputProperties;

/**
 * Unit tests for {@link JiraReader}
 */
public class JiraReaderTest {

    /**
     * {@link ComponentService} instance
     */
    private static ComponentService componentService;

    /**
     * Instances used in this test
     */
    private RuntimeContainer container;

    private JiraSource source;

    private TJiraInputProperties properties;

    /**
     * Creates {@link ComponentService} for tests
     */
    @BeforeClass
    public static void setupService() {
        DefinitionRegistry registry = new DefinitionRegistry();
        registry.registerDefinition(Arrays.asList(new TJiraInputDefinition()));
        componentService = new ComponentServiceImpl(registry);
    }

    @Before
    public void setup() {
        setupProperties();
        setupSource();
    }

    private void setupProperties() {
        properties = (TJiraInputProperties) componentService.getComponentProperties(TJiraInputDefinition.COMPONENT_NAME);
        properties.connection.hostUrl.setValue(INCORRECT_HOST_PORT);
        properties.connection.basicAuthentication.userId.setValue(USER);
        properties.connection.basicAuthentication.password.setValue(PASS);
    }

    private void setupSource() {
        source = new JiraSource();
        source.initialize(container, properties);
    }

    /**
     * Checks {@link JiraReader#advance()} throws {@link IOException} if {@link JiraReader#start()}
     * wasn't called
     * 
     * @throws IOException in case of any exception
     */
    @Test(expected = IOException.class)
    public void testAdvanceNotStarted() throws IOException {
        JiraProjectsReader jiraReader = new JiraProjectsReader(source);
        try {
            jiraReader.advance();
        } finally {
            jiraReader.close();
        }
    }

    /**
     * Checks {@link JiraReader#getCurrent()} throws {@link NoSuchElementException} if {@link JiraReader#start()}
     * wasn't called
     * 
     * @throws IOException in case of any exception
     */
    @Test(expected = NoSuchElementException.class)
    public void testGetCurrentNotStarted() throws IOException {
        JiraProjectsReader jiraReader = new JiraProjectsReader(source);
        try {
            jiraReader.getCurrent();
        } finally {
            jiraReader.close();
        }
    }

    /**
     * Checks {@link JiraReader#getCurrentSource()} returns {@link Source}, which was passed to constructor as argument
     * without any changes
     * 
     * @throws IOException in case of any exception
     */
    @Test
    public void testGetCurrentSource() throws IOException {
        JiraProjectsReader jiraReader = new JiraProjectsReader(source);
        JiraSource currentSource = jiraReader.getCurrentSource();
        jiraReader.close();
        assertEquals(source, currentSource);
    }
}
