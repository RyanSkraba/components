// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.marketo.runtime.MarketoBaseTestIT;
import org.talend.components.marketo.runtime.MarketoSink;
import org.talend.components.marketo.runtime.MarketoSource;
import org.talend.components.marketo.runtime.client.rest.type.SyncStatus;
import org.talend.components.marketo.runtime.client.type.MarketoRecordResult;
import org.talend.components.marketo.runtime.client.type.MarketoSyncResult;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.StandardAction;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties.CustomObjectDeleteBy;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.CustomObjectSyncAction;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.InputOperation;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.OutputOperation;

public class MarketoOpportunitiesClientTestIT extends MarketoBaseTestIT {

    public static final String OOP_1 = "19UYA31581L000000";

    public static final String OOP_2 = "29UYA31581L000000";

    TMarketoInputProperties iprops;

    TMarketoOutputProperties oprops;

    @Before
    public void setUp() throws Exception {
        iprops = new TMarketoInputProperties("test");
        iprops.connection.setupProperties();
        iprops.connection.endpoint.setValue(ENDPOINT_REST);
        iprops.connection.clientAccessId.setValue(USERID_REST);
        iprops.connection.secretKey.setValue(SECRETKEY_REST);
        iprops.schemaInput.setupProperties();
        iprops.mappingInput.setupProperties();
        iprops.setupProperties();
        iprops.includeTypes.setupProperties();
        iprops.includeTypes.type.setValue(new ArrayList<String>());
        iprops.excludeTypes.setupProperties();
        iprops.excludeTypes.type.setValue(new ArrayList<String>());
        iprops.connection.setupLayout();
        iprops.schemaInput.setupLayout();
        iprops.setupLayout();
        //
        oprops = new TMarketoOutputProperties("test");
        oprops.connection.setupProperties();
        oprops.connection.endpoint.setValue(ENDPOINT_REST);
        oprops.connection.clientAccessId.setValue(USERID_REST);
        oprops.connection.secretKey.setValue(SECRETKEY_REST);
        oprops.schemaInput.setupProperties();
        oprops.setupProperties();
        oprops.connection.setupLayout();
        oprops.schemaInput.setupLayout();
        oprops.setupLayout();
    }

    private void checkDescribeOpportunities() throws IOException {
        MarketoSource source = new MarketoSource();
        source.initialize(null, iprops);
        MarketoRESTClient client = (MarketoRESTClient) source.getClientService(null);
        MarketoRecordResult opps = client.describeOpportunity(iprops);
        LOG.debug("opps = {}.", opps);
        assertNotNull(opps.getRecords());
        assertNotNull(opps.getRecords().get(0));
        IndexedRecord record = opps.getRecords().get(0);
        assertNotNull(record.get(record.getSchema().getField("idField").pos()));
        assertNotNull(record.get(record.getSchema().getField("dedupeFields").pos()));
        assertNotNull(record.get(record.getSchema().getField("searchableFields").pos()));
        assertNotNull(record.get(record.getSchema().getField("fields").pos()));
    }

    private MarketoSyncResult createOpportunity(String... externalOIds) throws IOException {
        oprops.outputOperation.setValue(OutputOperation.syncOpportunities);
        oprops.afterOutputOperation();
        oprops.customObjectSyncAction.setValue(CustomObjectSyncAction.createOrUpdate);
        List<IndexedRecord> records = new ArrayList<>();
        for (String opp : externalOIds) {
            IndexedRecord r1 = new Record(oprops.schemaInput.schema.getValue());
            r1.put(0, opp);
            records.add(r1);
        }
        MarketoSink sink = new MarketoSink();
        sink.initialize(null, oprops);
        MarketoRESTClient client = (MarketoRESTClient) sink.getClientService(null);
        return client.syncOpportunities(oprops, records);
    }

    private MarketoSyncResult deleteOpportunity(String... opportunities) throws Exception {
        oprops.outputOperation.setValue(OutputOperation.deleteOpportunities);
        oprops.afterOutputOperation();
        oprops.customObjectDeleteBy.setValue(CustomObjectDeleteBy.dedupeFields);
        List<IndexedRecord> records = new ArrayList<>();
        for (String opp : opportunities) {
            IndexedRecord r1 = new Record(oprops.schemaInput.schema.getValue());
            r1.put(0, opp);
            records.add(r1);
        }
        MarketoSink sink = new MarketoSink();
        sink.initialize(null, oprops);
        MarketoRESTClient client = (MarketoRESTClient) sink.getClientService(null);
        return client.deleteOpportunities(oprops, records);
    }

    @Test
    public void testDescribeOpportunities() throws Exception {
        iprops.inputOperation.setValue(InputOperation.Opportunity);
        iprops.afterInputOperation();
        iprops.standardAction.setValue(StandardAction.describe);
        iprops.afterStandardAction();
        checkDescribeOpportunities();
    }

    @Test
    public void testDescribeOpportunitiesRole() throws Exception {
        iprops.inputOperation.setValue(InputOperation.OpportunityRole);
        iprops.afterInputOperation();
        iprops.standardAction.setValue(StandardAction.describe);
        iprops.afterStandardAction();
        checkDescribeOpportunities();
    }

    @Test
    public void testGetOpportunities() throws Exception {
        createOpportunity(OOP_1, OOP_2);

        iprops.inputOperation.setValue(InputOperation.Opportunity);
        iprops.afterInputOperation();
        iprops.standardAction.setValue(StandardAction.get);
        iprops.afterStandardAction();
        iprops.customObjectFilterType.setValue("externalOpportunityId");
        iprops.customObjectFilterValues.setValue(OOP_1 + "," + OOP_2);
        MarketoSource source = new MarketoSource();
        source.initialize(null, iprops);
        MarketoRESTClient client = (MarketoRESTClient) source.getClientService(null);
        MarketoRecordResult opps = client.getOpportunities(iprops, null);
        assertTrue(opps.isSuccess());
        assertEquals(2, opps.getRecordCount());
        for (IndexedRecord r : opps.getRecords()) {
            assertNotNull(r.get(r.getSchema().getField("marketoGUID").pos()));
            assertNotNull(r.get(r.getSchema().getField("externalOpportunityId").pos()));
        }
        LOG.debug("opps = {}.", opps);
        deleteOpportunity(OOP_1, OOP_2);
    }

    @Test
    public void testSyncOpportunities() throws Exception {
        MarketoSyncResult t = createOpportunity(OOP_1, OOP_2);
        assertTrue(t.isSuccess());
        assertEquals(2, t.getRecordCount());
        for (SyncStatus r : t.getRecords()) {
            assertEquals("created", r.getStatus());
        }
        deleteOpportunity(OOP_1, OOP_2);
    }

    @Test
    public void testDeleteOpportunities() throws Exception {
        createOpportunity(OOP_1);
        MarketoSyncResult t = deleteOpportunity(OOP_1);
        assertTrue(t.isSuccess());
        assertEquals(1, t.getRecordCount());
        for (SyncStatus r : t.getRecords()) {
            assertEquals("deleted", r.getStatus());
        }
    }

    @Test
    public void testOpportunitiesRole() throws Exception {
        createOpportunity(OOP_1, OOP_2);
        // create oppR
        oprops.outputOperation.setValue(OutputOperation.syncOpportunityRoles);
        oprops.afterOutputOperation();
        oprops.customObjectSyncAction.setValue(CustomObjectSyncAction.createOrUpdate);
        List<IndexedRecord> records = new ArrayList<>();
        List<String> oppr = Arrays.asList(OOP_1, OOP_2);
        for (String opp : oppr) {
            IndexedRecord r1 = new Record(oprops.schemaInput.schema.getValue());
            r1.put(0, opp);
            r1.put(1, 5);
            r1.put(2, "roly");
            records.add(r1);
        }
        MarketoSink sink = new MarketoSink();
        sink.initialize(null, oprops);
        MarketoRESTClient client = (MarketoRESTClient) sink.getClientService(null);
        MarketoSyncResult t = client.syncOpportunities(oprops, records);
        // get oppR
        iprops.inputOperation.setValue(InputOperation.OpportunityRole);
        iprops.afterInputOperation();
        iprops.standardAction.setValue(StandardAction.get);
        iprops.afterStandardAction();
        iprops.customObjectFilterType.setValue("externalOpportunityId");
        iprops.customObjectFilterValues.setValue(OOP_1 + "," + OOP_2);
        MarketoSource source = new MarketoSource();
        source.initialize(null, iprops);
        client = (MarketoRESTClient) source.getClientService(null);
        MarketoRecordResult opps = client.getOpportunities(iprops, null);
        assertTrue(opps.isSuccess());
        assertEquals(2, opps.getRecordCount());
        for (IndexedRecord r : opps.getRecords()) {
            assertNotNull(r.get(r.getSchema().getField("marketoGUID").pos()));
            assertNotNull(r.get(r.getSchema().getField("externalOpportunityId").pos()));
            assertNotNull(r.get(r.getSchema().getField("leadId").pos()));
            assertNotNull(r.get(r.getSchema().getField("role").pos()));
            assertEquals("roly", r.get(r.getSchema().getField("role").pos()));
        }
        // get oppR via compoundKey
        iprops.customObjectFilterType.setValue("");
        iprops.customObjectFilterValues.setValue("");
        iprops.useCompoundKey.setValue(true);
        iprops.afterUseCompoundKey();
        List<String> kn = Arrays.asList("externalOpportunityId", "leadId", "role");
        List<String> kv = Arrays.asList(OOP_1, "5", "roly");
        iprops.compoundKey.keyName.setValue(kn);
        iprops.compoundKey.keyValue.setValue(kv);
        source = new MarketoSource();
        source.initialize(null, iprops);
        client = (MarketoRESTClient) source.getClientService(null);
        opps = client.getOpportunities(iprops, null);
        LOG.warn("opps = {}.", opps);
        assertTrue(opps.isSuccess());
        assertEquals(1, opps.getRecordCount());
        LOG.warn("opps = {}.", opps);
        // delete oppR
        oprops.outputOperation.setValue(OutputOperation.deleteOpportunityRoles);
        oprops.afterOutputOperation();
        oprops.customObjectDeleteBy.setValue(CustomObjectDeleteBy.dedupeFields);
        records = new ArrayList<>();
        for (String opp : Arrays.asList(OOP_1, OOP_2)) {
            IndexedRecord r1 = new Record(oprops.schemaInput.schema.getValue());
            r1.put(0, opp);
            r1.put(1, 5);
            r1.put(2, "roly");
            records.add(r1);
        }
        sink = new MarketoSink();
        sink.initialize(null, oprops);
        client = (MarketoRESTClient) sink.getClientService(null);
        t = client.deleteOpportunities(oprops, records);
        LOG.debug("t = {}.", t);
        //
        deleteOpportunity(OOP_1, OOP_2);
    }

}
