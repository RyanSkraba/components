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
package org.talend.components.jira.runtime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.container.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.container.RuntimeContainer;

/**
 * Integration tests for {@link JiraReader}
 */
public class JiraReaderTestIT {

    private static final Logger LOG = LoggerFactory.getLogger(JiraReaderTestIT.class);

    /**
     * Jira server host and port
     */
    private static final String HOST_PORT = "http://192.168.99.100:8080/";

    /**
     * Jira server user id
     */
    private static final String USER = "root";

    /**
     * Empty user constant
     */
    private static final String EMPTY_USER = "";

    /**
     * Jira server user id
     */
    private static final String PASS = "123456";

    /**
     * Runtime container instance for tests
     */
    private RuntimeContainer container;

    /**
     * Instantiates instances used for tests
     */
    @Before
    public void setUp() {
        container = new DefaultComponentRuntimeContainerImpl();
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
    public void anonymousUserTest() throws IOException {
        String resource = "rest/api/2/project";

        JiraProjectReader jiraReader = new JiraProjectReader(null, HOST_PORT, resource, EMPTY_USER, PASS, Collections.EMPTY_MAP,
                null, container);

        List<Object> entities = new ArrayList<>();
        for (boolean hasNext = jiraReader.start(); hasNext; hasNext = jiraReader.advance()) {
            Object entity = jiraReader.getCurrent().get(0);
            LOG.debug(entity.toString());
            entities.add(entity);
        }

        jiraReader.close();

        assertThat(entities, hasSize(2));
        assertThat(entities.get(0).toString(), containsString("Public Project 1"));
        assertThat(entities.get(1).toString(), containsString("Public Project 2"));
    }
    
    /**
     * Checks {@link JiraReader} supports read project by ID feature.
     * Jira server has 3 projects. This test checks only 1 project retrieved.
     * 
     * @throws IOException in case of any exception
     */
    @Ignore
    @Test
    public void readProjectByIdTest() throws IOException {
        String id = "/TP";
        String resource = "rest/api/2/project" + id;

        JiraProjectIdReader jiraReader = new JiraProjectIdReader(null, HOST_PORT, resource, USER, PASS, Collections.EMPTY_MAP,
                null, container);

        List<Object> entities = new ArrayList<>();
        for (boolean hasNext = jiraReader.start(); hasNext; hasNext = jiraReader.advance()) {
            Object entity = jiraReader.getCurrent().get(0);
            LOG.debug(entity.toString());
            entities.add(entity);
        }

        jiraReader.close();

        assertThat(entities, hasSize(1));
        assertThat(entities.get(0).toString(), containsString("Test Project"));
    }

}
