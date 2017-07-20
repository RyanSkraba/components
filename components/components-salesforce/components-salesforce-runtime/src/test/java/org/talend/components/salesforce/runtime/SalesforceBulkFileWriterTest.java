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
package org.talend.components.salesforce.runtime;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.talend.components.salesforce.SalesforceOutputProperties.OutputAction;
import org.talend.components.salesforce.tsalesforceoutputbulk.TSalesforceOutputBulkProperties;
import org.talend.daikon.avro.SchemaConstants;

/**
 * Tests for SalesforceBulkFileWriter class
 */
public class SalesforceBulkFileWriterTest {

    private SalesforceBulkFileWriter writer;

    private Schema schema = SchemaBuilder.record("record").fields().name("Id")
            .prop(SchemaConstants.TALEND_COLUMN_IS_KEY, Boolean.TRUE.toString()).type().stringType().noDefault()
            .requiredString("Name").endRecord();

    @Before
    public void setup() {
        TSalesforceOutputBulkProperties salesforceBulkProperties = new TSalesforceOutputBulkProperties("foo");
        salesforceBulkProperties.outputAction.setValue(OutputAction.INSERT);
        writer = new SalesforceBulkFileWriter(Mockito.mock(SalesforceBulkFileWriteOperation.class), salesforceBulkProperties,
                null);
    }

    @Test
    public void testGetHeaders() {
        String[] expected = { "Id", "Name" };
        String[] actual = writer.getHeaders(schema);
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetHeadersForDeleteAction() {
        String[] expected = { "Id" };
        writer.getBulkProperties().outputAction.setValue(OutputAction.DELETE);
        String[] actual = writer.getHeaders(schema);
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetValues() {
        IndexedRecord record = new GenericRecordBuilder(schema).set("Id", "123456").set("Name", "Talend").build();
        List<String> expected = new ArrayList<>();
        expected.add("123456");
        expected.add("Talend");
        List<String> actual = writer.getValues(record);
        Assert.assertThat(actual, CoreMatchers.is(expected));
    }

    @Test
    public void testGetValuesForDeleteAction() {
        IndexedRecord record = new GenericRecordBuilder(schema).set("Id", "123456").set("Name", "Talend").build();
        List<String> expected = new ArrayList<>();
        expected.add("123456");
        writer.getBulkProperties().outputAction.setValue(OutputAction.DELETE);
        List<String> actual = writer.getValues(record);
        Assert.assertThat(actual, CoreMatchers.is(expected));
    }

}
