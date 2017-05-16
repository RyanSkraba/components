package org.talend.components.marketo.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.avro.Schema;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

public class MarketoSourceOrSinkTestIT extends MarketoBaseTestIT {

    MarketoSourceOrSink sos;

    TMarketoInputProperties props;

    private transient static final Logger LOG = LoggerFactory.getLogger(MarketoSourceOrSinkTestIT.class);

    @Before
    public void setUp() throws Exception {
        sos = new MarketoSourceOrSink();
        props = new TMarketoInputProperties("test");
        props.connection.setupProperties();
        props.setupProperties();
        props.connection.endpoint.setValue(MarketoBaseTestIT.ENDPOINT_REST);
        props.connection.clientAccessId.setValue(MarketoBaseTestIT.USERID_REST);
        props.connection.secretKey.setValue(MarketoBaseTestIT.SECRETKEY_REST);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getSchemaNamesFail() throws Exception {
        assertNull(sos.getSchemaNames(null));
    }

    @Test
    public void testGetSchemaNames() throws Exception {
        TMarketoInputProperties props = new TMarketoInputProperties("test");
        props.connection.setupProperties();
        props.setupProperties();
        props.connection.endpoint.setValue(MarketoBaseTestIT.ENDPOINT_REST);
        props.connection.clientAccessId.setValue(MarketoBaseTestIT.USERID_REST);
        props.connection.secretKey.setValue(MarketoBaseTestIT.SECRETKEY_REST);
        List<NamedThing> cos = MarketoSourceOrSink.getSchemaNames(null, props.getConnectionProperties());
        LOG.debug("cos = {}.", cos);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getEndpointSchemaFail() throws Exception {
        assertNull(sos.getEndpointSchema(null, "schemaInput"));
    }

    @Test
    public void testGetEndpointSchema() throws Exception {
        TMarketoInputProperties props = new TMarketoInputProperties("test");
        props.connection.setupProperties();
        props.setupProperties();
        props.connection.endpoint.setValue(MarketoBaseTestIT.ENDPOINT_REST);
        props.connection.clientAccessId.setValue(MarketoBaseTestIT.USERID_REST);
        props.connection.secretKey.setValue(MarketoBaseTestIT.SECRETKEY_REST);
        Schema s = MarketoSourceOrSink.getEndpointSchema(null, "smartphone_c", props.getConnectionProperties());
        LOG.debug("s = {}.", s);
        assertTrue(s.getFields().size() > 0);
        Schema.Field f = s.getField("model");
        assertNotNull(f);
        assertEquals("true", f.getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));
        s = MarketoSourceOrSink.getEndpointSchema(null, "flight_C", props.getConnectionProperties());
    }

    @Test
    public void validate() throws Exception {
        TMarketoInputProperties props = new TMarketoInputProperties("test");
        sos.initialize(null, props);
        assertEquals(Result.ERROR, sos.validate(null).getStatus());
    }

    @Test
    public void initialize() throws Exception {
        TMarketoInputProperties props = new TMarketoInputProperties("test");
        assertEquals(ValidationResult.OK, sos.initialize(null, props));
    }

    @Test(expected = IOException.class)
    public void testGetClientService() throws Exception {
        TMarketoInputProperties props = new TMarketoInputProperties("test");
        props.setupProperties();
        props.connection.setupProperties();
        sos.initialize(null, props);
        assertEquals("Marketo REST API Client [].", sos.getClientService(null).toString());
        assertEquals("Marketo REST API Client [].", sos.getClientService(null).toString());// 2times for cache
        props.setupProperties();
        sos = new MarketoSourceOrSink();
        props.connection.apiMode.setValue(APIMode.SOAP);
        props.connection.endpoint.setValue("https://www.marketo.com");
        sos.initialize(null, props);
        assertEquals("Marketo SOAP API Client [null].", sos.getClientService(null).toString());
    }

    @Test
    public void testEndpointSchemaForResources() throws Exception {
        sos.initialize(null, props);
        Schema s = sos.getEndpointSchema(null, "car_c");
        assertNotNull(s);
        // TODO fails now, test when APIs enabled.
        s = sos.getSchemaForCompany();
        assertNull(s);
        s = sos.getSchemaForOpportunity();
        assertNull(s);
        s = sos.getSchemaForOpportunityRole();
        assertNull(s);
    }

    @Test
    public void testGetCompoundKeyFields() throws Exception {
        sos.initialize(null, props);
        List<String> keys = sos.getCompoundKeyFields("car_c");
        assertNotNull(keys);
        assertEquals(keys, Arrays.asList("customerId", "VIN"));// Arrays.asList("VIN", "customerId")));
        // TODO fails now, test when APIs enabled.
        keys = sos.getCompoundKeyFields(MarketoSourceOrSink.RESOURCE_COMPANY);
        assertNull(keys);
        keys = sos.getCompoundKeyFields(MarketoSourceOrSink.RESOURCE_OPPORTUNITY);
        assertNull(keys);
        keys = sos.getCompoundKeyFields(MarketoSourceOrSink.RESOURCE_OPPORTUNITY_ROLE);
        assertNull(keys);
    }
}
