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
package org.talend.components.marketo.runtime.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode.REST;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.runtime.MarketoBaseTestIT;
import org.talend.components.marketo.runtime.MarketoSource;
import org.talend.components.marketo.runtime.client.rest.type.SyncStatus;
import org.talend.components.marketo.runtime.client.type.MarketoError;
import org.talend.components.marketo.runtime.client.type.MarketoRecordResult;
import org.talend.components.marketo.runtime.client.type.MarketoSyncResult;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties.CustomObjectDeleteBy;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties.CustomObjectSyncAction;

public class MarketoClientCustomObjectsTestIT extends MarketoBaseTestIT {

    private transient static final Logger LOG = LoggerFactory.getLogger(MarketoRESTClientTestIT.class);

    public static final String TEST_CO_NAME_SMARTPHONE = "smartphone_c";

    public static final String FIELD_CO_SMARTPHONE_MODEL = "model";

    public static final String FIELD_CO_SMARTPHONE_BRAND = "brand";

    public static final String FIELD_CO_SMARTPHONE_CUSTOMER_ID = "customerId";

    public static final String STATUS_UPDATED = "updated";

    public static final String STATUS_CREATED = "created";

    public static final String STATUS_SKIPPED = "skipped";

    public static final String STATUS_DELETED = "deleted";

    public static final String FIELD_CO_MARKETO_GUID = "marketoGUID";

    TMarketoInputProperties isProps;

    TMarketoInputProperties irProps;

    TMarketoOutputProperties oprops;

    public static final String TEST_SMARTPHONE_BRAND_SAMSUNG = "Samsung";

    public static final String[] TEST_SMARTPHONE_MODELS = new String[] { "XCover", "Galaxy S6", "Galaxy S7" };

    @Before
    public void setUp() throws Exception {
        irProps = new TMarketoInputProperties("test");
        irProps.connection.setupProperties();
        irProps.connection.endpoint.setValue(ENDPOINT_REST);
        irProps.connection.clientAccessId.setValue(USERID_REST);
        irProps.connection.secretKey.setValue(SECRETKEY_REST);
        irProps.connection.apiMode.setValue(REST);
        irProps.schemaInput.setupProperties();
        irProps.mappingInput.setupProperties();
        irProps.setupProperties();
        irProps.includeTypes.setupProperties();
        irProps.includeTypes.type.setValue(new ArrayList<String>());
        irProps.excludeTypes.setupProperties();
        irProps.excludeTypes.type.setValue(new ArrayList<String>());
        irProps.connection.setupLayout();
        irProps.schemaInput.setupLayout();
        irProps.setupLayout();
        //
        oprops = new TMarketoOutputProperties("test");
        oprops.connection.setupProperties();
        oprops.connection.apiMode.setValue(REST);
        oprops.connection.endpoint.setValue(ENDPOINT_REST);
        oprops.connection.clientAccessId.setValue(USERID_REST);
        oprops.connection.secretKey.setValue(SECRETKEY_REST);
        oprops.schemaInput.setupProperties();
        oprops.setupProperties();
        oprops.connection.setupLayout();
        oprops.schemaInput.setupLayout();
        oprops.setupLayout();
    }

    @BeforeClass
    public static void setupCOs() throws Exception {

    }

    @Test
    public void testDescribeFields() throws Exception {
        MarketoSource source = new MarketoSource();
        source.initialize(null, irProps);
        MarketoRESTClient client = (MarketoRESTClient) source.getClientService(null);
        List<Field> fieldList = client.getAllLeadFields();
        assertTrue(fieldList.size() > 500);
        for (Field f : fieldList) {
            // LOG.debug("f [{}] = {} {}.", f.doc(), f, f.getObjectProps());
            assertNotNull(f.doc());
            assertNotNull(f.schema().getType());
        }
    }

    /*
     *
     * Custom Objects
     *
     */
    public void checkCustomObject(IndexedRecord r, Boolean isDescribe) {
        Schema s = MarketoConstants.getCustomObjectDescribeSchema();
        assertNotNull(r.get(s.getField("name").pos()));
        assertNotNull(r.get(s.getField("createdAt").pos()));
        assertEquals("java.util.Date", r.get(s.getField("createdAt").pos()).getClass().getCanonicalName());
        assertTrue(r.get(s.getField("createdAt").pos()) instanceof Date);
        assertNotNull(r.get(s.getField("updatedAt").pos()));
        assertTrue(r.get(s.getField("updatedAt").pos()) instanceof Date);
        assertNotNull(r.get(s.getField("idField").pos()));
        assertNotNull(r.get(s.getField("dedupeFields").pos()));
        assertNotNull(r.get(s.getField("searchableFields").pos()));
        assertNotNull(r.get(s.getField("relationships").pos()));
        assertEquals(true, r.get(s.getField("fields").pos()) != null);
    }

    public void checkCustomObjectRecord(IndexedRecord r) {
        Schema s = MarketoConstants.getCustomObjectRecordSchema();
        assertNotNull(r.get(s.getField(FIELD_CO_MARKETO_GUID).pos()));
        assertNotNull(r.get(s.getField("seq").pos()));
        assertTrue(r.get(s.getField("seq").pos()) instanceof Integer);
        assertNotNull(r.get(s.getField("createdAt").pos()));
        assertEquals("java.util.Date", r.get(s.getField("createdAt").pos()).getClass().getCanonicalName());
        assertTrue(r.get(s.getField("createdAt").pos()) instanceof Date);
        assertNotNull(r.get(s.getField("updatedAt").pos()));
        assertTrue(r.get(s.getField("updatedAt").pos()) instanceof Date);
    }

    @Test
    public void testDescribeCustomObject() throws Exception {
        MarketoSource source = new MarketoSource();
        source.initialize(null, irProps);
        MarketoRESTClient client = (MarketoRESTClient) source.getClientService(null);
        irProps.customObjectName.setValue(TEST_CO_NAME_SMARTPHONE);
        MarketoRecordResult result = client.describeCustomObject(irProps);
        assertNotNull(result.getRecords());
        assertEquals(1, result.getRecords().size());
        LOG.debug("result = {}.", result.getRecords().get(0));
        checkCustomObject(result.getRecords().get(0), true);
    }

    @Test
    public void testListCustomObjects() throws Exception {
        MarketoSource source = new MarketoSource();
        source.initialize(null, irProps);
        MarketoClientServiceExtended client = (MarketoClientServiceExtended) source.getClientService(null);
        irProps.customObjectNames.setValue("smartphone_c,roadShow_c,car_c");
        MarketoRecordResult result = client.listCustomObjects(irProps);
        assertNotNull(result.getRecords());
        assertEquals(2, result.getRecords().size());
        for (IndexedRecord r : result.getRecords()) {
            checkCustomObject(r, false);
            LOG.debug("r = {}.", r);
        }
    }

    @Test
    public void testGetCustomObjects() throws Exception {
        MarketoSyncResult rs = createCustomObjectRecords("");
        MarketoSource source = new MarketoSource();
        source.initialize(null, irProps);
        MarketoRESTClient client = (MarketoRESTClient) source.getClientService(null);
        irProps.customObjectName.setValue(TEST_CO_NAME_SMARTPHONE);
        irProps.customObjectFilterType.setValue(FIELD_CO_SMARTPHONE_MODEL);
        irProps.customObjectFilterValues.setValue(MarketoRESTClient.csvString(TEST_SMARTPHONE_MODELS));
        irProps.batchSize.setValue(500);
        irProps.schemaInput.schema.setValue(MarketoConstants.getCustomObjectRecordSchema());
        MarketoRecordResult result = client.getCustomObjects(irProps, null);
        assertNotNull(result.getRecords());
        assertEquals(TEST_SMARTPHONE_MODELS.length, result.getRecords().size());
        LOG.debug("result = {}.", result.getRecords().get(0));
        checkCustomObjectRecord(result.getRecords().get(0));
    }

    @Test
    public void testGetCustomObjectsFail() throws Exception {
        MarketoSource source = new MarketoSource();
        source.initialize(null, irProps);
        MarketoRESTClient client = (MarketoRESTClient) source.getClientService(null);
        irProps.customObjectName.setValue(TEST_CO_NAME_SMARTPHONE);
        irProps.customObjectFilterType.setValue(FIELD_CO_SMARTPHONE_BRAND); // cannot search by brand, must be a dedupe
                                                                            // field.
        irProps.customObjectFilterValues.setValue(TEST_SMARTPHONE_BRAND_SAMSUNG);
        irProps.batchSize.setValue(500);
        irProps.schemaInput.schema.setValue(MarketoConstants.getCustomObjectRecordSchema());
        MarketoRecordResult result = client.getCustomObjects(irProps, null);
        assertFalse(result.isSuccess());
        assertEquals(0, result.getRecordCount());
        assertNotNull(result.getErrors());
        Object err = result.getErrors().get(0);
        assertTrue(err instanceof MarketoError);
        assertEquals("REST", ((MarketoError) err).getApiMode());
        assertEquals("1003", ((MarketoError) err).getCode());
        assertEquals("Invalid filterType 'brand'", ((MarketoError) err).getMessage());
    }

    @Test
    public void testGetCustomObjectsAllRecords() throws Exception {
        MarketoSource source = new MarketoSource();
        source.initialize(null, irProps);
        MarketoRESTClient client = (MarketoRESTClient) source.getClientService(null);
        irProps.customObjectName.setValue(TEST_CO_NAME_SMARTPHONE);
        irProps.batchSize.setValue(500);
        irProps.schemaInput.schema.setValue(MarketoConstants.getCustomObjectRecordSchema());
        MarketoRecordResult result = client.getCustomObjects(irProps, null);
        assertFalse(result.isSuccess());
        assertEquals(0, result.getRecordCount());
        assertNotNull(result.getErrors());
        Object err = result.getErrors().get(0);
        assertTrue(err instanceof MarketoError);
        assertEquals("REST", ((MarketoError) err).getApiMode());
        assertEquals("1003", ((MarketoError) err).getCode());
        assertEquals("filterType not specified", ((MarketoError) err).getMessage());
    }

    @Test
    public void testGetCustomObjectsPagination() throws Exception {
        MarketoSyncResult rs = createCustomObjectRecords("");
        MarketoSource source = new MarketoSource();
        source.initialize(null, irProps);
        MarketoRESTClient client = (MarketoRESTClient) source.getClientService(null);
        irProps.customObjectName.setValue(TEST_CO_NAME_SMARTPHONE);
        irProps.batchSize.setValue(1);
        irProps.schemaInput.schema.setValue(MarketoConstants.getCustomObjectRecordSchema());
        irProps.customObjectFilterType.setValue(FIELD_CO_SMARTPHONE_MODEL);
        irProps.customObjectFilterValues.setValue(MarketoRESTClient.csvString(TEST_SMARTPHONE_MODELS));
        MarketoRecordResult result = client.getCustomObjects(irProps, null);
        assertNotNull(result.getRecords());
        assertTrue(result.getRemainCount() > 0);
        assertEquals(1, result.getRecords().size());
        LOG.debug("result = {}.", result.getRecords().get(0));
        result = client.getCustomObjects(irProps, result.getStreamPosition());
        assertNotNull(result.getRecords());
        assertEquals(1, result.getRecords().size());
        checkCustomObjectRecord(result.getRecords().get(0));
    }

    public MarketoSyncResult createCustomObjectRecords(String dedupeBy) throws Exception {
        oprops.customObjectName.setValue(TEST_CO_NAME_SMARTPHONE);
        oprops.customObjectSyncAction.setValue(CustomObjectSyncAction.createOrUpdate);
        oprops.customObjectDedupeBy.setValue(dedupeBy);
        Schema s = SchemaBuilder.record("sn").fields() //
                .name(FIELD_CO_SMARTPHONE_BRAND).type().stringType().noDefault() //
                .name(FIELD_CO_SMARTPHONE_MODEL).type().stringType().noDefault()//
                .name(FIELD_CO_SMARTPHONE_CUSTOMER_ID).type().intType().noDefault()//
                .endRecord();
        List<IndexedRecord> records = new ArrayList<>();
        IndexedRecord r1;
        for (String m : TEST_SMARTPHONE_MODELS) {
            r1 = new Record(s);
            r1.put(0, TEST_SMARTPHONE_BRAND_SAMSUNG);
            r1.put(1, m);
            r1.put(2, 3113479);
            records.add(r1);
        }
        MarketoSource source = new MarketoSource();
        source.initialize(null, irProps);
        MarketoRESTClient client = (MarketoRESTClient) source.getClientService(null);
        MarketoSyncResult rs = client.syncCustomObjects(oprops, records);
        return rs;
    }

    @Test
    public void testSyncCustomObjects() throws Exception {
        MarketoSyncResult rs = createCustomObjectRecords("");
        assertTrue(rs.isSuccess());
        List<SyncStatus> changes = rs.getRecords();
        assertEquals(TEST_SMARTPHONE_MODELS.length, changes.size());
        for (SyncStatus r : changes) {
            assertNotNull(r);
            assertNotNull(r.getSeq());
            assertNotNull(r.getMarketoGUID());
            assertTrue(r.getStatus().equals(STATUS_UPDATED) || r.getStatus().equals(STATUS_CREATED));
        }
    }

    @Test
    public void testSyncCustomObjectsWithDedupeByFail() throws Exception {
        MarketoSyncResult rs = createCustomObjectRecords("yoyoto");
        assertFalse(rs.isSuccess());
        LOG.debug("rs = {}.", rs);
        assertEquals("1003", rs.getErrors().get(0).getCode());
        assertEquals("Invalid dedupeBy 'yoyoto' value", rs.getErrors().get(0).getMessage());
    }

    @Test
    public void testDeleteCustomObjectsFailWithWrongFields() throws Exception {
        MarketoSyncResult rs = createCustomObjectRecords("");
        assertTrue(rs.isSuccess());
        //
        oprops.customObjectName.setValue(TEST_CO_NAME_SMARTPHONE);
        Schema s = SchemaBuilder.record("sn").fields() //
                .name(FIELD_CO_SMARTPHONE_BRAND).type().stringType().noDefault() //
                .name(FIELD_CO_SMARTPHONE_MODEL).type().stringType().noDefault() //
                .name(FIELD_CO_SMARTPHONE_CUSTOMER_ID).type().intType().noDefault() //
                .endRecord();
        List<IndexedRecord> records = new ArrayList<>();
        IndexedRecord r1;
        for (String m : TEST_SMARTPHONE_MODELS) {
            r1 = new Record(s);
            r1.put(0, TEST_SMARTPHONE_BRAND_SAMSUNG);
            r1.put(1, m);
            r1.put(2, 3113479);
            records.add(r1);
        }

        MarketoSource source = new MarketoSource();
        source.initialize(null, irProps);
        MarketoRESTClient client = (MarketoRESTClient) source.getClientService(null);
        rs = client.deleteCustomObjects(oprops, records);
        assertTrue(rs.isSuccess());
        List<SyncStatus> changes = rs.getRecords();
        assertEquals(TEST_SMARTPHONE_MODELS.length, changes.size());
        for (SyncStatus r : changes) {
            assertNotNull(r);
            assertNotNull(r.getSeq());
            assertEquals(STATUS_SKIPPED, r.getStatus());
        }
    }

    @Test
    public void testDeleteCustomObjectsByDedupeFields() throws Exception {
        MarketoSyncResult rs = createCustomObjectRecords("");
        assertTrue(rs.isSuccess());
        //
        oprops.customObjectName.setValue(TEST_CO_NAME_SMARTPHONE);
        Schema s = SchemaBuilder.record("sn").fields() //
                .name(FIELD_CO_SMARTPHONE_MODEL).type().stringType().noDefault() //
                .endRecord();
        List<IndexedRecord> records = new ArrayList<>();
        IndexedRecord r1;
        for (String m : TEST_SMARTPHONE_MODELS) {
            r1 = new Record(s);
            r1.put(0, m);
            records.add(r1);
        }
        MarketoSource source = new MarketoSource();
        oprops.customObjectDeleteBy.setValue(CustomObjectDeleteBy.dedupeFields);
        source.initialize(null, oprops);
        MarketoRESTClient client = (MarketoRESTClient) source.getClientService(null);
        rs = client.deleteCustomObjects(oprops, records);
        assertTrue(rs.isSuccess());
        List<SyncStatus> changes = rs.getRecords();
        assertEquals(TEST_SMARTPHONE_MODELS.length, changes.size());
        for (SyncStatus r : changes) {
            assertNotNull(r);
            assertNotNull(r.getSeq());
            assertEquals(STATUS_DELETED, r.getStatus());
            LOG.debug("r = {}.", r);
        }
    }

    @Test
    public void testDeleteCustomObjectsByIdField() throws Exception {
        MarketoSyncResult rs = createCustomObjectRecords("");
        assertTrue(rs.isSuccess());
        List<String> mktoIds = new ArrayList<>();
        for (SyncStatus sr : rs.getRecords()) {
            mktoIds.add(sr.getMarketoGUID());
        }
        //
        oprops.customObjectName.setValue(TEST_CO_NAME_SMARTPHONE);
        Schema s = SchemaBuilder.record("sn").fields() //
                .name(FIELD_CO_MARKETO_GUID).type().stringType().noDefault() //
                .endRecord();
        List<IndexedRecord> records = new ArrayList<>();
        IndexedRecord r1;
        for (String m : mktoIds) {
            r1 = new Record(s);
            r1.put(0, m);
            records.add(r1);
        }
        MarketoSource source = new MarketoSource();
        oprops.customObjectDeleteBy.setValue(CustomObjectDeleteBy.idField);
        source.initialize(null, oprops);
        MarketoRESTClient client = (MarketoRESTClient) source.getClientService(null);
        rs = client.deleteCustomObjects(oprops, records);
        assertTrue(rs.isSuccess());
        List<SyncStatus> changes = rs.getRecords();
        assertEquals(TEST_SMARTPHONE_MODELS.length, changes.size());
        for (SyncStatus r : changes) {
            assertNotNull(r);
            assertNotNull(r.getSeq());
            assertEquals(STATUS_DELETED, r.getStatus());
            LOG.debug("r = {}.", r);
        }
    }
}
