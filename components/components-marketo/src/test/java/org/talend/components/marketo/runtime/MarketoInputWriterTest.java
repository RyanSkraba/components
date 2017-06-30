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
package org.talend.components.marketo.runtime;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;

public class MarketoInputWriterTest extends MarketoRuntimeTestBase {

    MarketoInputWriter writer;

    MarketoWriteOperation wop;

    TMarketoInputProperties props;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        props = new TMarketoInputProperties("test");
        props.connection.setupProperties();
        props.setupProperties();
        props.leadKeyValues.setValue("email");
        props.updateSchemaRelated();
        when(sink.getProperties()).thenReturn(props);
        wop = new MarketoWriteOperation(sink);
        writer = new MarketoInputWriter(wop, null);
        writer.properties = props;
        assertTrue(writer instanceof MarketoInputWriter);
    }

    @Test
    public void testOpen() throws Exception {
        writer.open("test");
        assertNotNull(writer.close());
    }

    @Test
    public void testWrite() throws Exception {
        writer.open("test");
        writer.write(null);
        IndexedRecord record = new Record(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads());
        //
        record.put(1, "toto@toto.com");
        writer.write(record);
    }

    @Test
    public void testWriteDynamic() throws Exception {
        props.schemaInput.schema.setValue(getLeadDynamicSchema());
        when(sink.getProperties()).thenReturn(props);
        when(sink.getDynamicSchema(any(String.class), any(Schema.class)))
                .thenReturn(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads());

        writer.open("test");
        IndexedRecord record = new Record(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads());
        record.put(1, "toto@toto.com");
        writer.write(record);
    }

    @Test
    public void testFlush() throws Exception {
        // nop
        writer.flush();
    }

}
