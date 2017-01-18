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
package org.talend.components.jira.avro;

import static org.junit.Assert.assertEquals;
import static org.talend.daikon.avro.SchemaConstants.TALEND_IS_LOCKED;

import java.util.Collections;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field.Order;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.daikon.avro.AvroRegistry;

/**
 * Unit-tests for {@link IssueIndexedRecord} class
 */
public class IssueIndexedRecordTest {

    /**
     * {@link Schema} used as test argument
     */
    private static Schema testSchema;

    /**
     * Used as test argument
     */
    private static String testJson;

    /**
     * Initializes test arguments before tests
     */
    @BeforeClass
    public static void setUp() {
        AvroRegistry registry = new AvroRegistry();
        Schema stringSchema = registry.getConverter(String.class).getSchema();
        Schema.Field jsonField = new Schema.Field("json", stringSchema, null, null, Order.ASCENDING);
        testSchema = Schema.createRecord("jira", null, null, false, Collections.singletonList(jsonField));
        testSchema.addProp(TALEND_IS_LOCKED, "true");
        
        testJson = "{\"startAt\":0,\"maxResults\":2,\"total\":1,\"issues\":[]}";
    }

    /**
     * Checks {@link IssueIndexedRecord#getSchema()} returns schema without changes
     */
    @Test
    public void testGetSchema() {
        IssueIndexedRecord indexedRecord = new IssueIndexedRecord(testJson, testSchema);
        assertEquals(testSchema, indexedRecord.getSchema());
    }

    /**
     * Checks {@link IssueIndexedRecord#get()} returns json field value, when 0 is passed as index argument
     */
    @Test
    public void testGet() {
        IssueIndexedRecord indexedRecord = new IssueIndexedRecord(testJson, testSchema);
        assertEquals(testJson, indexedRecord.get(0));
    }

    /**
     * Checks {@link IssueIndexedRecord#get()} throws {@link IndexOutOfBoundsException}, when any other than 0 is passed
     * as index argument
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetIndexOutOfBoundException() {
        IssueIndexedRecord indexedRecord = new IssueIndexedRecord(testJson, testSchema);
        indexedRecord.get(3);
    }

    /**
     * Checks {@link IssueIndexedRecord#put()} put field value correctly when 0 is passed as index
     */
    @Test
    public void testPut() {
        IssueIndexedRecord indexedRecord = new IssueIndexedRecord(testJson, null);
        indexedRecord.put(0, testJson);
        assertEquals(testJson, indexedRecord.get(0));
    }

    /**
     * Checks {@link IssueIndexedRecord#put()} throws {@link IndexOutOfBoundsException}, when any other than 0 is passed
     * as index argument
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testPutIndexOutOfBoundException() {
        IssueIndexedRecord indexedRecord = new IssueIndexedRecord(testJson, null);
        indexedRecord.put(3, testJson);
    }
}
