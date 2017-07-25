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
package org.talend.components.jira.runtime.writer;

import java.io.IOException;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.talend.daikon.avro.SchemaConstants.TALEND_IS_LOCKED;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.exception.DataRejectException;
import org.talend.components.jira.connection.Rest;
import org.talend.components.jira.runtime.JiraSink;
import org.talend.components.jira.runtime.JiraWriteOperation;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

/**
 * Unit-tests for {@link JiraWriter} class
 */
public class JiraWriterTest {

    protected static final Schema TEST_INSERT_SCHEMA = SchemaBuilder.builder().record("jira").fields() //
            .name("json").type(AvroUtils._string()).noDefault() //
            .endRecord();

    protected static final Schema TEST_UPDATE_SCHEMA = SchemaBuilder.builder().record("jira").fields() //
            .name("id").type(AvroUtils._string()).noDefault() //
            .name("json").type(AvroUtils._string()).noDefault() //
            .endRecord();

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
     * Checks {@link JiraWriter#close()} releases connection and returns {@link Result}, with 0 data count
     */
    @Test
    public void testClose() {
        JiraWriter writer = new JiraWriter(writeOperation);
        writer.open("uId");

        Result result = writer.close();

        assertThat(writer.getConnection(), is(nullValue()));
        assertFalse(writer.opened);
        assertEquals("uId", result.getuId());
        assertEquals(0, result.getTotalCount());
        assertEquals(0, result.getRejectCount());
        assertEquals(0, result.getSuccessCount());
    }

    @Test
    public void testInsertWithNullJson() {
        JiraInsertWriter writer = new JiraInsertWriter(writeOperation);

        GenericRecord record = new GenericData.Record(TEST_INSERT_SCHEMA);

        testWriteRecordWithEmptyJson(writer, record);
    }

    @Test
    public void testInsertWithEmptyJson() {
        JiraInsertWriter writer = new JiraInsertWriter(writeOperation);

        GenericRecord record = new GenericData.Record(TEST_INSERT_SCHEMA);
        record.put("json", "");

        testWriteRecordWithEmptyJson(writer, record);
    }

    @Test
    public void testUpdateWithNullJson() {
        JiraUpdateWriter writer = new JiraUpdateWriter(writeOperation);

        GenericRecord record = new GenericData.Record(TEST_UPDATE_SCHEMA);
        record.put("id", "TP-1");

        testWriteRecordWithEmptyJson(writer, record);
    }

    @Test
    public void testUpdateWithEmptyJson() {
        JiraUpdateWriter writer = new JiraUpdateWriter(writeOperation);

        GenericRecord record = new GenericData.Record(TEST_UPDATE_SCHEMA);
        record.put("id", "TP-1");
        record.put("json", "");

        testWriteRecordWithEmptyJson(writer, record);
    }

    private void testWriteRecordWithEmptyJson(JiraWriter writer, IndexedRecord record) {
        writer.open("uId");

        try {
            writer.write(record);
            fail("IOException expected");
        } catch (IOException e) {
            // expected
        }

        Result result = writer.close();

        assertEquals(1, result.getTotalCount());
        assertEquals(1, result.getRejectCount());
    }

}
