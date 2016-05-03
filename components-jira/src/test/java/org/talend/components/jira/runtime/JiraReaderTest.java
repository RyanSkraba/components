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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.generic.IndexedRecord;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.jira.testutils.Utils;

/**
 * Unit-tests for {@link JiraReader}
 */
public class JiraReaderTest {

    /**
     * Local Jira instance specific user id
     */
    private static final String USER = "ivan";

    /**
     * Local Jira instance specific password
     */
    private static final String PASS = "12345";

    /**
     * Checks {@link JiraReader#start()} returns true in case of correct properties passed
     * Requires real Jira instance, so it is ignored
     * 
     * @throws IOException in case of any exception
     */
    @Ignore
    @Test
    public void startTest() throws IOException {
        String testUrl = "http://localhost:8080/";
        String testResource = "rest/api/2/issue/TP-1";
        Reader<IndexedRecord> jiraReader = new JiraReader(null, testUrl, testResource, USER, PASS, Collections.EMPTY_MAP, null);

        boolean started = jiraReader.start();
        jiraReader.close();

        assertTrue(started);
    }
    
    /**
     * Checks paging implementation
     * Requires real Jira instance, so it is ignored
     * 
     * @throws IOException in case of any exception
     */
    @Ignore
    @Test
    public void pagingTest() throws IOException {
        String testUrl = "http://localhost:8080";
        String testResource = "/rest/api/2/search";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("jql", "project=TP");
        parameters.put("maxResults", "10");
        
        Reader<IndexedRecord> jiraReader = new JiraReader(null, testUrl, testResource, USER, PASS, parameters, null);

        for (boolean hasNext = jiraReader.start(); hasNext; hasNext = jiraReader.advance()) {
            System.out.println(jiraReader.getCurrent().get(0));
        }
        
        jiraReader.close();
    }

    /**
     * Checks {@link JiraReader#getEntities(String)} returns correct number of entities
     * 
     * @throws Exception in case of any exception
     */
    @Test
    public void getEntitiesTest() throws Exception {
        JiraReader jiraReader = new JiraReader(null, null, null, null, null, Collections.EMPTY_MAP, null);
        String jsonFile = "src/test/resources/org/talend/components/jira/runtime/entities.json";
        String testJson = Utils.readFile(jsonFile);

        List<String> strs = jiraReader.getEntities(testJson);
        jiraReader.close();

        assertEquals(3, strs.size());
    }

}
