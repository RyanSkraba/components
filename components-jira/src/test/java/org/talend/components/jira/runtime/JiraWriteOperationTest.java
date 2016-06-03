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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.container.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.jira.Action;
import org.talend.components.jira.runtime.result.DataCountResult;
import org.talend.components.jira.runtime.writer.JiraDeleteWriter;
import org.talend.components.jira.runtime.writer.JiraInsertWriter;
import org.talend.components.jira.runtime.writer.JiraUpdateWriter;
import org.talend.components.jira.runtime.writer.JiraWriter;
import org.talend.components.jira.tjiraoutput.TJiraOutputProperties;

/**
 * Unit-tests for {@link JiraWriteOperation} class
 */
public class JiraWriteOperationTest {

    /**
     * Mocked instance of {@link JiraSink}
     */
    private JiraSink sink;
    
    /**
     * Instance of {@link RuntimeContainer} used in tests
     */
    private RuntimeContainer container;

    @Before
    public void setupMocks() {
        sink = mock(JiraSink.class);
        
        container = new DefaultComponentRuntimeContainerImpl() {
            
            @Override
            public String getCurrentComponentId() {
                return "tJIRAOutput";
            }
        };
    }

    /**
     * Checks {@link JiraWriteOperation#getSink()} returns {@link JiraSink} without any changes
     */
    @Test
    public void testGetSink() {
        JiraWriteOperation writeOperation = new JiraWriteOperation(sink);

        JiraSink actualSink = writeOperation.getSink();

        assertEquals(sink, actualSink);
    }

    /**
     * Checks {@link JiraWriteOperation#createWriter()} returns {@link JiraDeleteWriter}, when Delete action specified
     */
    @Test
    public void testCreateWriterDelete() {
        when(sink.getAction()).thenReturn(Action.DELETE);
        JiraWriteOperation writeOperation = new JiraWriteOperation(sink);

        JiraWriter writer = writeOperation.createWriter(container);

        assertThat(writer, is(instanceOf(JiraDeleteWriter.class)));
    }

    /**
     * Checks {@link JiraWriteOperation#createWriter()} returns {@link JiraInsertWriter}, when Insert action specified
     */
    @Test
    public void testCreateWriterInsert() {
        when(sink.getAction()).thenReturn(Action.INSERT);
        JiraWriteOperation writeOperation = new JiraWriteOperation(sink);

        JiraWriter writer = writeOperation.createWriter(container);

        assertThat(writer, is(instanceOf(JiraInsertWriter.class)));
    }

    /**
     * Checks {@link JiraWriteOperation#createWriter()} returns {@link JiraUpdateWriter}, when Update action specified
     */
    @Test
    public void testCreateWriterUpdate() {
        when(sink.getAction()).thenReturn(Action.UPDATE);
        JiraWriteOperation writeOperation = new JiraWriteOperation(sink);

        JiraWriter writer = writeOperation.createWriter(container);

        assertThat(writer, is(instanceOf(JiraUpdateWriter.class)));
    }
    
    /**
     * Checks {@link JiraWriteOperation#finalize()} computes total of output results and set them to container
     */
    @Test
    public void testFinalize() {
        DataCountResult result1 = new DataCountResult("id-1", 15, 10, 5);
        DataCountResult result2 = new DataCountResult("id-1", 25, 20, 5);
        List<DataCountResult> results = Arrays.asList(result1, result2);
        JiraWriteOperation writeOperation = new JiraWriteOperation(sink);

        writeOperation.finalize(results, container);

        assertEquals(40, container.getComponentData("tJIRAOutput", TJiraOutputProperties.NB_LINE));
        assertEquals(30, container.getComponentData("tJIRAOutput", TJiraOutputProperties.NB_SUCCESS));
        assertEquals(10, container.getComponentData("tJIRAOutput", TJiraOutputProperties.NB_REJECT));
    }
}
