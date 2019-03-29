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
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.runtime.client.type.MarketoRecordResult;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.properties.ValidationResult.Result;

public class MarketoSourceOrSinkTest extends MarketoRuntimeTestBase {

    MarketoSourceOrSink sos;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        sos = new MarketoSourceOrSink();
    }

    @Test
    public void testMergeDynamicSchemas() throws Exception {
        Schema lead = MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads();
        Schema co = MarketoConstants.getCustomObjectDescribeSchema();
        Schema merged = MarketoSourceOrSink.mergeDynamicSchemas(lead, co);
        assertNotNull(merged);
        assertEquals(10, merged.getFields().size());
        merged = MarketoSourceOrSink.mergeDynamicSchemas(lead, getLeadDynamicSchema());
        assertEquals(2, merged.getFields().size());
    }

    @Test
    public void testGetSchemaFieldsList() throws Exception {
        List<Field> fields = MarketoSourceOrSink
                .getSchemaFieldsList(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads());
        assertNotNull(fields);
        assertEquals(6, fields.size());
    }

    @Test
    public void testValidate() throws Exception {
        TMarketoInputProperties props = new TMarketoInputProperties("test");
        sos.initialize(null, props);
        assertEquals(Result.ERROR, sos.validate(null).getStatus());
        props.connection.endpoint.setValue("http://fake.io");
        sos.initialize(null, props);
        assertEquals(Result.ERROR, sos.validate(null).getStatus());
        props.connection.clientAccessId.setValue("client");
        sos.initialize(null, props);
        assertEquals(Result.ERROR, sos.validate(null).getStatus());
        props.connection.secretKey.setValue("sekret");
        sos.initialize(null, props);
        assertEquals(Result.ERROR, sos.validate(null).getStatus());
    }

    public TMarketoInputProperties getTestProps() {
        TMarketoInputProperties props = new TMarketoInputProperties("test");
        props.connection.endpoint.setValue("http://fake.io");
        props.connection.clientAccessId.setValue("client");
        props.connection.secretKey.setValue("sekret");
        return props;
    }

    public List<NamedThing> getCustomObjects() {
        List<NamedThing> co = new ArrayList<>();
        SimpleNamedThing snt = new SimpleNamedThing("car_c", "car_c");
        co.add(snt);
        snt = new SimpleNamedThing("smartphone_c", "smartphone_c");
        co.add(snt);
        return co;
    }

    public MarketoRecordResult getDescribeCO() {
        MarketoRecordResult mkto = new MarketoRecordResult();
        mkto.setSuccess(true);
        IndexedRecord record = new Record(MarketoConstants.getCustomObjectDescribeSchema());
        record.put(0, "car_c");
        record.put(1, "car_c");
        record.put(6, "[\"model\"]"); // dedupe
        record.put(8, "[{\"displayName\":\"CreatedAt\",\"dataType\":\"datetime\",\"name\":\"createdAt\","
                + "\"updateable\":false},{\"displayName\":\"MarketoGUID\",\"dataType\":\"string\",\"length\":36,\"name\":\"marketoGUID\",\"updateable\":false}]"); // fields
        mkto.setRecords(Arrays.asList(record));
        return mkto;
    }

    public List<Field> getAllLeadFieldsFake() {
        List<Field> fields = new ArrayList<>();

        return fields;
    }

    @Test
    public void testGetSchemaNames() throws Exception {
        MarketoSourceOrSink spy = spy(sos);
        spy.initialize(null, getTestProps());
        doReturn(client).when(spy).getClientService(any());
        doReturn(getLeadRecordResult(false)).when(client).listCustomObjects(any(TMarketoInputProperties.class));
        assertNotNull(spy.getSchemaNames(null));
    }

    @Test
    public void testGetEnpointSchema() throws Exception {
        MarketoSourceOrSink spy = spy(sos);
        spy.initialize(null, getTestProps());
        doReturn(client).when(spy).getClientService(any());
        doReturn(getFailedRecordResult("REST", "", "error")).when(client)
                .describeCustomObject(any(TMarketoInputProperties.class));
        assertNull(spy.getEndpointSchema(null, "car_c"));
        doReturn(getDescribeCO()).when(client).describeCustomObject(any(TMarketoInputProperties.class));
        doReturn(getDescribeCO()).when(client).describeCompanies(any(TMarketoInputProperties.class));
        doReturn(getDescribeCO()).when(client).describeOpportunity(any(TMarketoInputProperties.class));
        assertNotNull(spy.getEndpointSchema(null, "car_c"));
        assertNull(spy.getSchemaForCustomObject(""));
        assertNotNull(spy.getSchemaForCustomObject("car_c"));
        doReturn(getFailedRecordResult("REST", "", "error")).when(client)
                .describeCustomObject(any(TMarketoInputProperties.class));
        assertNotNull(spy.getSchemaForCompany());
        assertNotNull(spy.getSchemaForOpportunity());
        assertNotNull(spy.getSchemaForOpportunityRole());
    }

    @Test
    public void testGetCompoundKeyFields() throws Exception {
        MarketoSourceOrSink spy = spy(sos);
        spy.initialize(null, getTestProps());
        doReturn(client).when(spy).getClientService(any());
        doReturn(getFailedRecordResult("REST", "", "error")).when(client)
                .describeCustomObject(any(TMarketoInputProperties.class));
        assertNull(spy.getCompoundKeyFields("car_c"));
        doReturn(getDescribeCO()).when(client).describeCustomObject(any(TMarketoInputProperties.class));
        assertNotNull(spy.getCompoundKeyFields("car_c"));
    }

    @Test
    public void testGetDynamicSchema() throws Exception {
        MarketoSourceOrSink spy = spy(sos);
        spy.initialize(null, getTestProps());
        doReturn(client).when(spy).getClientService(any());
        doReturn(Collections.emptyList()).when(client).getAllLeadFields();
        doReturn(getDescribeCO()).when(client).describeCustomObject(any(TMarketoInputProperties.class));
        assertEquals(4, spy.getDynamicSchema("car_c", getLeadDynamicSchema()).getFields().size());
        doReturn(getAllLeadFieldsFake()).when(client).getAllLeadFields();
        doReturn(getDescribeCO()).when(client).describeCustomObject(any(TMarketoInputProperties.class));
        assertNotNull(spy.getDynamicSchema("car_c", getLeadDynamicSchema()));
        assertNotNull(spy.getDynamicSchema("", getLeadDynamicSchema()));
    }

}
