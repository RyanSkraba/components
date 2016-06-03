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
package org.talend.components.jira.runtime.writer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.WriterResult;
import org.talend.components.jira.connection.Rest;
import org.talend.components.jira.runtime.JiraSink;
import org.talend.components.jira.runtime.JiraWriteOperation;
import org.talend.components.jira.runtime.result.DataCountResult;

/**
 * Unit-tests for {@link JiraWriter} class
 */
public class JiraWriterTest {

    /**
     * {@link JiraSink} mock
     */
    private JiraSink sink;
    
    /**
     * {@link JiraWriteOperation} mock
     */
    private JiraWriteOperation writeOperation;
    
    /**
     * Sets up mocks used in tests
     */
    @Before
    public void setupMocks() {
        sink = mock(JiraSink.class);
        when(sink.getHostPort()).thenReturn("http://localhost:8080");
        when(sink.getUserId()).thenReturn("user");
        when(sink.getUserPassword()).thenReturn("password");
        
        writeOperation = mock(JiraWriteOperation.class);
        when(writeOperation.getSink()).thenReturn(sink);
    }
    
    /**
     * Checks {@link JiraWriter#open(String)} initializes connection instance
     */
    @Test
    public void testOpen() {
        JiraWriter writer = new JiraWriter(writeOperation);
        
        writer.open("uId");
        
        Rest connection = writer.getConnection();
        assertThat(connection, notNullValue());
    }
    
    /**
     * Checks {@link JiraWriter#getWriteOperation())} returns {@link WriteOperation} without any changes
     */
    @Test
    public void testGetWriteOperation() {
        JiraWriter writer = new JiraWriter(writeOperation);
        
        JiraWriteOperation actualWriteOperation = writer.getWriteOperation();
        
        assertEquals(writeOperation, actualWriteOperation);
    }
    
    /**
     * Checks {@link JiraWriter#close()} releases connection and returns {@link WriterResult}, with 0 data count
     */
    @Test
    public void testClose() {
        JiraWriter writer = new JiraWriter(writeOperation);
        writer.open("uId");

        DataCountResult result = writer.close();

        assertThat(writer.getConnection(), is(nullValue()));
        assertFalse(writer.opened);
        assertEquals("uId", result.getuId());
        assertEquals(0, result.getDataCount());
        assertEquals(0, result.getRejectCount());
        assertEquals(0, result.getSuccessCount());
    }
}
