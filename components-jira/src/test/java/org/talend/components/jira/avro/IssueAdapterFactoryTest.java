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
 * Unit-tests for {@link IssueAdapterFactory} class
 */
public class IssueAdapterFactoryTest {

    /**
     * {@link Schema} used as test argument
     */
    private static Schema testSchema;
    
    /**
     * {@link IssueIndexedRecord} used as test argument
     */ 
    private static IssueIndexedRecord issueIndexedRecord;
    
    /**
     * Used as test argument
     */
    private static String testJson = "{\"key\":null}";

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
        
        issueIndexedRecord = new IssueIndexedRecord(testJson, testSchema);
    }

    /**
     * Checks {@link IssueAdapterFactory#getDatumClass()} returns String.class
     */
    @Test
    public void testGetDatumClass() {
        IssueAdapterFactory issueRecordConverter = new IssueAdapterFactory();
        Class<?> datumClass = issueRecordConverter.getDatumClass();
        String datumClassName = datumClass.getCanonicalName();
        assertEquals(datumClassName, "java.lang.String");
    }

    /**
     * Checks {@link IssueAdapterFactory#setSchema()} and {@link IssueAdapterFactory#getSchema()} sets and gets schema
     * without any changes
     */
    @Test
    public void testSetGetSchema() {
        IssueAdapterFactory issueRecordConverter = new IssueAdapterFactory();
        issueRecordConverter.setSchema(testSchema);
        assertEquals(testSchema, issueRecordConverter.getSchema());
    }
    
    /**
     * Checks {@link IssueAdapterFactory#convertToDatum()} converts IndexedRecord to String by retrieving String 
     * value from IndexedRecord
     */
    @Test
    public void testConvertToDatum() {
        IssueAdapterFactory issueRecordConverter = new IssueAdapterFactory();
        String json = issueRecordConverter.convertToDatum(issueIndexedRecord);
        assertEquals(testJson, json);
    }
    
    /**
     * Checks {@link IssueAdapterFactory#convertToAvro()} converts String to IndexedRecord by creating IssueIndexedRecord instance 
     * with String datum wrapped
     */
    @Test
    public void testConvertToAvro() {
        IssueAdapterFactory issueRecordConverter = new IssueAdapterFactory();
        IssueIndexedRecord record = issueRecordConverter.convertToAvro(testJson);
        assertEquals(testJson, record.get(0));
    }
}
