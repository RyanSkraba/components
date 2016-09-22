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
package org.talend.components.jira.runtime.reader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.talend.components.jira.testutils.JiraTestConstants.ANONYMOUS_USER;
import static org.talend.components.jira.testutils.JiraTestConstants.HOST_PORT;
import static org.talend.components.jira.testutils.JiraTestConstants.PASS;
import static org.talend.components.jira.testutils.JiraTestConstants.USER;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.internal.ComponentRegistry;
import org.talend.components.api.service.internal.ComponentServiceImpl;
import org.talend.components.jira.runtime.JiraSource;
import org.talend.components.jira.tjirainput.TJiraInputDefinition;
import org.talend.components.jira.tjirainput.TJiraInputProperties;

/**
 * Integration tests for {@link JiraReader}
 * Checks following features:
 * 1. Read single issue
 * 2. Read multiple issues
 * 3. Read single project
 * 4. Read all projects
 * 5. Anonymous user read
 */
public class JiraReaderTestIT {

    private static final Logger LOG = LoggerFactory.getLogger(JiraReaderTestIT.class);

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
        ComponentRegistry registry = new ComponentRegistry();
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
        properties.connection.hostUrl.setValue(HOST_PORT);
        properties.connection.basicAuthentication.userId.setValue(USER);
        properties.connection.basicAuthentication.password.setValue(PASS);
    }

    private void setupSource() {
        source = new JiraSource();
        source.initialize(container, properties);
    }

    private void changeUserTo(String user) {
        properties.connection.basicAuthentication.userId.setValue(user);
        setupSource();
    }

    private void changeJqlTo(String jql) {
        properties.jql.setValue(jql);
        setupSource();
    }

    /**
     * Checks {@link JiraReader} supports read projects feature with anonymous user. Jira server has 2 public projects:
     * 1. name: "Public Project 1", key: "PP1" 2. name: "Public Project 2", key: "PP2" and 1 project for logged users:
     * 1. name: "Test Project", key: "TP"
     * 
     * This test retrieves all projects available for anonymous user. {@link JiraReader} should return only 2 public
     * projects
     * 
     * @throws IOException in case of any exception
     */
    @Ignore
    @Test
    public void testAnonymousUser() throws IOException {
        changeUserTo(ANONYMOUS_USER);
        JiraProjectsReader jiraReader = new JiraProjectsReader(source);

        List<Object> entities = new ArrayList<>();
        for (boolean hasNext = jiraReader.start(); hasNext; hasNext = jiraReader.advance()) {
            Object entity = jiraReader.getCurrent().get(0);
            LOG.debug(entity.toString());
            entities.add(entity);
        }

        jiraReader.close();

        assertThat(entities, hasSize(2));
        assertThat(entities, containtEntityWithName("Public Project 1"));
        assertThat(entities, containtEntityWithName("Public Project 2"));
    }

    /**
     * Checks tJIRAInput component supports read project by ID feature.
     * Jira server has 3 projects. This test checks only 1 project of them retrieved.
     * 
     * @throws IOException in case of any exception
     */
    @Ignore
    @Test
    public void testReadProject() throws IOException {
        String key = "TP";
        JiraProjectIdReader jiraReader = new JiraProjectIdReader(source, key);

        List<Object> entities = new ArrayList<>();
        for (boolean hasNext = jiraReader.start(); hasNext; hasNext = jiraReader.advance()) {
            Object entity = jiraReader.getCurrent().get(0);
            LOG.debug(entity.toString());
            entities.add(entity);
        }

        jiraReader.close();

        assertThat(entities, hasSize(1));
        assertThat(entities, containtEntityWithName("Test Project"));
        assertThat(entities.get(0).toString(), not(startsWith("[")));
    }

    /**
     * Checks tJIRAInput component supports read all projects
     * Jira server has 3 projects. This test checks component retrieves 3 projects
     * 
     * @throws IOException in case of any exception
     */
    @Ignore
    @Test
    public void testReadProjects() throws IOException {
        JiraProjectsReader jiraReader = new JiraProjectsReader(source);

        List<Object> entities = new ArrayList<>();
        for (boolean hasNext = jiraReader.start(); hasNext; hasNext = jiraReader.advance()) {
            Object entity = jiraReader.getCurrent().get(0);
            LOG.debug(entity.toString());
            entities.add(entity);
        }

        jiraReader.close();

        assertThat(entities, hasSize(3));
        assertThat(entities, containtEntityWithName("Public Project 1"));
        assertThat(entities, containtEntityWithName("Public Project 2"));
        assertThat(entities, containtEntityWithName("Test Project"));
    }

    /**
     * Checks tJIRAInput component supports read single issue by key feature.
     * Jira docker image server has TP project with 2 issues: TP-1, TP-2
     * Summary of TP-1 issue is: "Test issue 1"
     * 
     * @throws IOException in case of any exception
     */
    @Ignore
    @Test
    public void testReadIssue() throws IOException {
        changeJqlTo("key=TP-1");
        JiraSearchReader jiraReader = new JiraSearchReader(source);

        List<Object> entities = new ArrayList<>();
        for (boolean hasNext = jiraReader.start(); hasNext; hasNext = jiraReader.advance()) {
            Object entity = jiraReader.getCurrent().get(0);
            LOG.debug(entity.toString());
            entities.add(entity);
        }
        jiraReader.close();

        assertThat(entities, hasSize(1));
        assertThat(entities, containtEntityWithName("Test issue 1"));
        assertThat(entities.get(0).toString(), not(startsWith("[")));
    }

    /**
     * Checks tJIRAInput component supports read multiple issues by JQL search query.
     * Jira docker image server has TP project with 2 issues: TP-1, TP-2
     * Summary of TP-1 issue is: "Test issue 1"
     * 
     * @throws IOException in case of any exception
     */
    @Ignore
    @Test
    public void testReadIssues() throws IOException {
        changeJqlTo("project=TP");
        JiraSearchReader jiraReader = new JiraSearchReader(source);

        List<Object> entities = new ArrayList<>();
        for (boolean hasNext = jiraReader.start(); hasNext; hasNext = jiraReader.advance()) {
            Object entity = jiraReader.getCurrent().get(0);
            LOG.debug(entity.toString());
            entities.add(entity);
        }
        jiraReader.close();

        assertThat(entities, hasSize(2));
        assertThat(entities, containtEntityWithName("Test issue 1"));
        assertThat(entities, containtEntityWithName("Test issue 2"));
    }

    /**
     * Checks {@link JiraReader#getCurrent()} throws {@link NoSuchElementException} in case of
     * {@link JiraReader#advance()} returned false, but despite this {@link JiraReader#getCurrent()}
     * was called
     * 
     * @throws IOException in case of any exception
     */
    @Ignore
    @Test(expected = NoSuchElementException.class)
    public void testGetCurrentNoMoreRecords() throws IOException {
        JiraProjectsReader jiraReader = new JiraProjectsReader(source);

        boolean hasNext = jiraReader.start();
        while (hasNext) {
            hasNext = jiraReader.advance();
        }

        assertFalse(jiraReader.advance());
        try {
            jiraReader.getCurrent();
        } finally {
            jiraReader.close();
        }
    }

    /**
     * Checks {@link JiraReader#getReturnValues()} returns map with 3 key-value pairs:
     * totalRecordCount = 3
     * successRecordCount = 0
     * rejectRecordCount = 0
     * 
     * @throws IOException in case of any exception
     */
    @Ignore
    @Test
    public void testGetReturnValues() throws IOException {
        JiraProjectsReader jiraReader = new JiraProjectsReader(source);

        boolean hasNext = jiraReader.start();
        while (hasNext) {
            hasNext = jiraReader.advance();
        }
        Map<String, Object> returnValues = jiraReader.getReturnValues();
        assertThat(returnValues.entrySet(), hasSize(3));
        assertEquals(3, returnValues.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));
        assertEquals(0, returnValues.get(ComponentDefinition.RETURN_SUCCESS_RECORD_COUNT));
        assertEquals(0, returnValues.get(ComponentDefinition.RETURN_REJECT_RECORD_COUNT));
        jiraReader.close();
    }

    /**
     * Returns {@link Matcher} which iterates over {@link Iterable} and checks whether element contains
     * specified string
     * 
     * @param name issue summary or project name
     * @return {@link Matcher}
     */
    public static <E> Matcher<Iterable<? extends E>> containtEntityWithName(String name) {
        return new IsIterableContainingEntityWithName<E>(name);
    }

    /**
     * Returns {@link Matcher} which iterates over {@link Iterable} and checks whether element contains
     * specified string
     */
    private static class IsIterableContainingEntityWithName<E> extends BaseMatcher<Iterable<? extends E>> {

        private String entityName;

        IsIterableContainingEntityWithName(String entityName) {
            this.entityName = entityName;
        }

        @Override
        public boolean matches(Object item) {
            Iterable<? extends E> iterable = (Iterable<? extends E>) item;
            for (E entity : iterable) {
                if (entity.toString().contains(entityName)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("JSON entity which contains " + entityName);
        }
    }

}
