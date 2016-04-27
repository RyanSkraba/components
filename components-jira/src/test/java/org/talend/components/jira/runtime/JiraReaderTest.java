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

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.avro.generic.IndexedRecord;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.runtime.Reader;

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
     * 
     * @throws IOException in case of exception
     */
    @Ignore
    @Test
    public void startTest() throws IOException {

        String testUrl = "http://localhost:8080/";
        String testResource = "rest/api/2/issue/TP-1";
        Reader<IndexedRecord> jiraReader = new JiraReader(null, testUrl, testResource, USER, PASS);

        boolean started = jiraReader.start();
        assertTrue(started);
    }

}
