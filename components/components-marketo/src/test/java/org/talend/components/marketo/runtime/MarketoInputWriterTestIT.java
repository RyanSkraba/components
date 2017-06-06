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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode.REST;
import static org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode.SOAP;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.InputOperation.getMultipleLeads;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadKeyTypeSOAP.EMAIL;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadKeyTypeREST;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadSelector;
import org.talend.daikon.avro.SchemaConstants;

public class MarketoInputWriterTestIT extends MarketoBaseTestIT {

    TMarketoInputProperties propsSOAP;

    TMarketoInputProperties propsREST;

    MarketoSink sink;

    @Before
    public void setUp() throws Exception {
        propsSOAP = new TMarketoInputProperties("test");
        propsSOAP.connection.setupProperties();
        propsSOAP.connection.endpoint.setValue(ENDPOINT_SOAP);
        propsSOAP.connection.clientAccessId.setValue(USERID_SOAP);
        propsSOAP.connection.secretKey.setValue(SECRETKEY_SOAP);
        propsSOAP.connection.apiMode.setValue(SOAP);
        propsSOAP.connection.setupLayout();
        propsSOAP.mappingInput.setupProperties();
        propsSOAP.setupProperties();
        propsSOAP.schemaInput.setupProperties();
        propsSOAP.schemaInput.setupLayout();
        propsSOAP.includeTypes.setupProperties();
        propsSOAP.includeTypes.type.setValue(new ArrayList<String>());
        propsSOAP.excludeTypes.setupProperties();
        propsSOAP.excludeTypes.type.setValue(new ArrayList<String>());
        propsSOAP.setupLayout();
        propsSOAP.inputOperation.setValue(getMultipleLeads);
        propsSOAP.leadSelectorSOAP.setValue(LeadSelector.LeadKeySelector);
        propsSOAP.leadKeyTypeSOAP.setValue(EMAIL);
        propsSOAP.afterInputOperation();
        propsSOAP.batchSize.setValue(1);
        propsSOAP.leadKeyValues.setValue("email");
        // REST
        propsREST = new TMarketoInputProperties("test");
        propsREST.connection.setupProperties();
        propsREST.connection.endpoint.setValue(ENDPOINT_REST);
        propsREST.connection.clientAccessId.setValue(USERID_REST);
        propsREST.connection.secretKey.setValue(SECRETKEY_REST);
        propsREST.connection.apiMode.setValue(REST);
        propsREST.connection.setupLayout();
        propsREST.mappingInput.setupProperties();
        propsREST.setupProperties();
        propsREST.schemaInput.setupProperties();
        propsREST.schemaInput.setupLayout();
        propsREST.includeTypes.setupProperties();
        propsREST.includeTypes.type.setValue(new ArrayList<String>());
        propsREST.excludeTypes.setupProperties();
        propsREST.excludeTypes.type.setValue(new ArrayList<String>());
        propsREST.setupLayout();
        propsREST.inputOperation.setValue(getMultipleLeads);
        propsREST.leadSelectorREST.setValue(LeadSelector.LeadKeySelector);
        propsREST.leadKeyTypeREST.setValue(LeadKeyTypeREST.email);
        propsREST.afterInputOperation();
        propsREST.batchSize.setValue(1);
        propsREST.leadKeyValues.setValue("email");
        //
        sink = new MarketoSink();
    }

    @Test
    public void testGetMultipleLeadsLeadKeyWithEmailSOAP() throws Exception {
        sink.initialize(null, propsSOAP);
        Writer tmpWriter = sink.createWriteOperation().createWriter(null);
        assertTrue(tmpWriter instanceof MarketoInputWriter);
        MarketoInputWriter writer = (MarketoInputWriter) tmpWriter;
        // create an input IndexedRecord
        Schema s = SchemaBuilder.builder().record("input").fields().name("email").type().stringType().noDefault().name("dummy")
                .type().stringType().noDefault().endRecord();
        IndexedRecord input = new GenericData.Record(s);
        input.put(0, "undx@undx.net");
        input.put(1, "dummy value");
        //
        writer.open("SOAPTests");
        writer.write(input);
        List<IndexedRecord> successes = writer.getSuccessfulWrites();
        assertNotNull(successes);
        IndexedRecord record = successes.get(0);
        assertNotNull(record);
        LOG.debug("record = {}.", record);
        Schema sout = record.getSchema();
        assertEquals("Id", sout.getFields().get(0).name());
        assertNotNull(record.get(0));
        assertEquals("Email", sout.getFields().get(1).name());
        assertNotNull(record.get(1));
        assertEquals("ForeignSysPersonId", sout.getFields().get(2).name());
        assertEquals("ForeignSysType", sout.getFields().get(3).name());
    }

    @Test
    public void testGetMultipleLeadsLeadKeyWithEmailREST() throws Exception {
        sink.initialize(null, propsREST);
        Writer tmpWriter = sink.createWriteOperation().createWriter(null);
        assertTrue(tmpWriter instanceof MarketoInputWriter);
        MarketoInputWriter writer = (MarketoInputWriter) tmpWriter;
        // create an input IndexedRecord
        Schema s = SchemaBuilder.builder().record("input").fields().name("email").type().stringType().noDefault().name("dummy")
                .type().stringType().noDefault().endRecord();
        IndexedRecord input = new GenericData.Record(s);
        input.put(0, "undx@undx.net");
        input.put(1, "dummy value");
        //
        writer.open("RESTTests");
        writer.write(input);
        List<IndexedRecord> successes = writer.getSuccessfulWrites();
        assertNotNull(successes);
        IndexedRecord record = successes.get(0);
        assertNotNull(record);
        LOG.debug("record = {}.", record);
        Schema sout = record.getSchema();
        assertEquals("id", sout.getFields().get(0).name());
        assertNotNull(record.get(0));
        assertEquals("email", sout.getFields().get(1).name());
        assertNotNull(record.get(1));
        assertEquals("firstName", sout.getFields().get(2).name());
        assertEquals("lastName", sout.getFields().get(3).name());
        assertEquals("createdAt", sout.getFields().get(4).name());
        assertEquals("updatedAt", sout.getFields().get(5).name());
    }

    @Test
    public void testGetMultipleLeadsLeadKeyWithEmailDynamicREST() throws Exception {
        sink.initialize(null, propsREST);
        Writer tmpWriter = sink.createWriteOperation().createWriter(null);
        assertTrue(tmpWriter instanceof MarketoInputWriter);
        MarketoInputWriter writer = (MarketoInputWriter) tmpWriter;
        // create an input IndexedRecord
        Schema s = SchemaBuilder.builder().record("input").fields().name("email").type().stringType().noDefault().name("dummy")
                .type().stringType().noDefault().endRecord();
        propsREST.schemaInput.schema.setValue(
                SchemaBuilder.builder().record("test").prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true").fields().endRecord());
        IndexedRecord input = new GenericData.Record(s);
        input.put(0, "undx@undx.net");
        input.put(1, "dummy value");
        //
        writer.open("RESTTests");
        writer.write(input);
        List<IndexedRecord> successes = writer.getSuccessfulWrites();
        assertNotNull(successes);
        IndexedRecord record = successes.get(0);
        assertNotNull(record);
        LOG.debug("record = {}.", record);
        Schema sout = record.getSchema();
    }

}
