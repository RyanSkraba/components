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

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode.SOAP;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.IncludeExcludeFieldsSOAP.ChangeDataValue;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.IncludeExcludeFieldsSOAP.NewLead;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadKeyTypeSOAP.EMAIL;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadSelector.LastUpdateAtSelector;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadSelector.LeadKeySelector;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadSelector.StaticListSelector;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.ListParam.STATIC_LIST_ID;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.ListParam.STATIC_LIST_NAME;
import static org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.InputOperation.getLead;
import static org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.InputOperation.getLeadActivity;
import static org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.InputOperation.getLeadChanges;
import static org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.InputOperation.getMultipleLeads;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.talend.components.marketo.runtime.MarketoSource;
import org.talend.components.marketo.runtime.client.rest.type.SyncStatus;
import org.talend.components.marketo.runtime.client.type.ListOperationParameters;
import org.talend.components.marketo.runtime.client.type.MarketoException;
import org.talend.components.marketo.runtime.client.type.MarketoRecordResult;
import org.talend.components.marketo.runtime.client.type.MarketoSyncResult;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadKeyTypeSOAP;
import org.talend.components.marketo.tmarketolistoperation.TMarketoListOperationProperties;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.OutputOperation;

import com.marketo.mktows.LeadRecord;

public class MarketoSOAPClientTestIT extends MarketoClientTestIT {

    TMarketoInputProperties inputProperties;

    TMarketoListOperationProperties listProperties;

    TMarketoOutputProperties outProperties;

    private transient static final Logger LOG = getLogger(MarketoSOAPClientTestIT.class);

    @Before
    public void setUp() throws Exception {
        inputProperties = new TMarketoInputProperties("test");
        inputProperties.connection.setupProperties();
        inputProperties.connection.endpoint.setValue(ENDPOINT_SOAP);
        inputProperties.connection.clientAccessId.setValue(USERID_SOAP);
        inputProperties.connection.secretKey.setValue(SECRETKEY_SOAP);
        inputProperties.schemaInput.setupProperties();
        inputProperties.mappingInput.setupProperties();
        inputProperties.includeTypes.setupProperties();
        inputProperties.setupProperties();
        inputProperties.connection.apiMode.setValue(SOAP);
        inputProperties.includeTypes.type.setValue(new ArrayList<String>());
        inputProperties.excludeTypes.setupProperties();
        inputProperties.excludeTypes.type.setValue(new ArrayList<String>());
        inputProperties.connection.setupLayout();
        inputProperties.schemaInput.setupLayout();
        inputProperties.setupLayout();
        //
        listProperties = new TMarketoListOperationProperties("test");
        listProperties.connection.setupProperties();
        listProperties.connection.endpoint.setValue(ENDPOINT_SOAP);
        listProperties.connection.clientAccessId.setValue(USERID_SOAP);
        listProperties.connection.secretKey.setValue(SECRETKEY_SOAP);
        listProperties.schemaInput.setupProperties();
        listProperties.setupProperties();
        listProperties.connection.apiMode.setValue(SOAP);
        listProperties.connection.setupLayout();
        listProperties.schemaInput.setupLayout();
        listProperties.setupLayout();
        //
        outProperties = new TMarketoOutputProperties("test");
        outProperties.connection.setupProperties();
        outProperties.connection.endpoint.setValue(ENDPOINT_SOAP);
        outProperties.connection.clientAccessId.setValue(USERID_SOAP);
        outProperties.connection.secretKey.setValue(SECRETKEY_SOAP);
        outProperties.schemaInput.setupProperties();
        outProperties.setupProperties();
        outProperties.connection.apiMode.setValue(SOAP);
        outProperties.connection.setupLayout();
        outProperties.schemaInput.setupLayout();
        outProperties.setupLayout();
    }

    @Test(expected = IOException.class)
    public void testBadConnectionString() throws Exception {
        inputProperties.connection.endpoint.setValue("htps://marketo.com/rest/v1");
        MarketoSource source = new MarketoSource();
        source.initialize(null, inputProperties);
        MarketoClientService client = source.getClientService(null);
        fail("Shouldn't be here");
    }

    @Test
    public void testGetLead() throws Exception {
        inputProperties.inputOperation.setValue(getLead);
        inputProperties.leadKeyTypeSOAP.setValue(EMAIL);
        inputProperties.afterInputOperation();
        //
        String email = EMAIL_UNDX00;
        inputProperties.leadKeyValue.setValue(email);
        MarketoSource source = new MarketoSource();
        source.initialize(null, inputProperties);
        MarketoClientService client = source.getClientService(null);
        //
        MarketoRecordResult result = client.getLead(inputProperties, null);
        LOG.debug("{}", result);
        List<IndexedRecord> records = result.getRecords();
        assertNotEquals(emptyList(), records);
        IndexedRecord record = records.get(0);
        assertNotNull(record);
        assertNotNull(record.get(0));
        assertEquals(email, record.get(1));
    }

    @Test
    public void testGetLeadMany() throws Exception {
        inputProperties.inputOperation.setValue(getMultipleLeads);
        inputProperties.leadKeyTypeSOAP.setValue(LeadKeyTypeSOAP.IDNUM);
        inputProperties.leadKeyValues.setValue(createdLeads.toString().replaceAll("\\]|\\[|\\s", ""));
        inputProperties.afterInputOperation();
        MarketoSource source = new MarketoSource();
        source.initialize(null, inputProperties);
        MarketoClientService client = source.getClientService(null);
        //
        MarketoRecordResult result = client.getMultipleLeads(inputProperties, null);
        LOG.debug("{}", result);
        List<IndexedRecord> records = result.getRecords();
        int count = 0;
        assertNotEquals(emptyList(), records);
        for (IndexedRecord record : records) {
            count++;
            assertNotNull(record);
            assertNotNull(record.get(0));// id
            assertTrue(record.get(1).toString().startsWith(EMAIL_PREFIX));
        }
        assertEquals(50, count);
    }

    @Test
    public void testGetLeadFail() throws Exception {
        inputProperties.inputOperation.setValue(getLead);
        inputProperties.leadKeyTypeSOAP.setValue(EMAIL);
        inputProperties.afterInputOperation();
        //
        inputProperties.leadKeyValue.setValue(EMAIL_INEXISTANT);
        MarketoSource source = new MarketoSource();
        source.initialize(null, inputProperties);
        MarketoClientService client = source.getClientService(null);
        //
        MarketoRecordResult result = client.getLead(inputProperties, null);
        List<IndexedRecord> records = result.getRecords();
        assertEquals(emptyList(), records);
        LOG.debug("record = " + records);
    }

    @Test(expected = IOException.class)
    public void testGetLeadFailWrongHost() throws Exception {
        inputProperties.inputOperation.setValue(getLead);
        inputProperties.leadKeyTypeSOAP.setValue(EMAIL);
        inputProperties.connection.endpoint.setValue(ENDPOINT_URL_INEXISTANT);
        inputProperties.afterInputOperation();
        //
        inputProperties.leadKeyValue.setValue(EMAIL_INEXISTANT);
        MarketoSource source = new MarketoSource();
        source.initialize(null, inputProperties);
        MarketoClientService client = source.getClientService(null);
        //
        MarketoRecordResult result = client.getLead(inputProperties, null);
        List<IndexedRecord> records = result.getRecords();
        assertEquals(emptyList(), records);
        LOG.debug("record = " + records);
    }

    @Test
    public void testGetMultipleLeadsLeadKey() throws Exception {
        inputProperties.inputOperation.setValue(getMultipleLeads);
        inputProperties.leadKeyTypeSOAP.setValue(EMAIL);
        inputProperties.afterInputOperation();
        inputProperties.batchSize.setValue(100);
        inputProperties.leadKeyValues.setValue("undx00@undx.net,undx10@undx.net,undx20@undx.net,undx30@undx.net");
        MarketoSource source = new MarketoSource();
        source.initialize(null, inputProperties);
        MarketoClientService client = source.getClientService(null);
        //
        MarketoRecordResult result = client.getMultipleLeads(inputProperties, null);
        LOG.debug("{}", result);
        List<IndexedRecord> records = result.getRecords();
        assertTrue(records.size() >= 4);
    }

    @Test
    public void testGetMultipleLeadsLeadKeyWithInputComponent() throws Exception {
        inputProperties.inputOperation.setValue(getMultipleLeads);
        inputProperties.leadKeyTypeSOAP.setValue(EMAIL);
        inputProperties.afterInputOperation();
        inputProperties.batchSize.setValue(100);
        inputProperties.leadKeyValues.setValue("undx00@undx.net,undx10@undx.net,undx20@undx.net,undx30@undx.net");
        MarketoSource source = new MarketoSource();
        source.initialize(null, inputProperties);
        MarketoClientService client = source.getClientService(null);
        //
        MarketoRecordResult result = client.getMultipleLeads(inputProperties, null);
        LOG.debug("{}", result);
        List<IndexedRecord> records = result.getRecords();
        assertEquals(4, records.size());
    }

    @Test
    public void testGetMultipleLeadsLeadKeyFail() throws Exception {
        inputProperties.inputOperation.setValue(getMultipleLeads);
        inputProperties.leadKeyTypeSOAP.setValue(EMAIL);
        inputProperties.afterInputOperation();
        inputProperties.batchSize.setValue(100);
        inputProperties.leadKeyValues.setValue("i-dont-exist@mail.com,bad-email@dot.net");
        MarketoSource source = new MarketoSource();
        source.initialize(null, inputProperties);
        MarketoClientService client = source.getClientService(null);
        MarketoRecordResult result = client.getMultipleLeads(inputProperties, null);
        LOG.debug("{}", result);
        assertTrue(result.isSuccess()); // but no leads
        assertEquals(0, result.getRecordCount());
        assertEquals(0, result.getRemainCount());
    }

    @Test
    public void testGetMultipleLeadsListName() throws Exception {
        inputProperties.inputOperation.setValue(getMultipleLeads);
        inputProperties.leadSelectorSOAP.setValue(StaticListSelector);
        inputProperties.updateSchemaRelated();
        inputProperties.batchSize.setValue(4);
        inputProperties.listParam.setValue(STATIC_LIST_NAME);
        inputProperties.listParamListName.setValue(UNDX_TEST_LIST_SMALL);
        MarketoSource source = new MarketoSource();
        source.initialize(null, inputProperties);
        MarketoClientService client = source.getClientService(null);
        //
        MarketoRecordResult result = client.getMultipleLeads(inputProperties, null);
        LOG.debug("{}", result);
        assertTrue(result.isSuccess());
        assertTrue(result.getRecordCount() > 3);
        assertTrue(result.getRemainCount() > 0);
        assertNotNull(result.getStreamPosition());
    }

    @Test
    public void testGetMultipleLeadsListNamePagination() throws Exception {
        inputProperties.inputOperation.setValue(getMultipleLeads);
        inputProperties.leadSelectorSOAP.setValue(StaticListSelector);
        inputProperties.afterInputOperation();
        inputProperties.batchSize.setValue(4);
        inputProperties.listParam.setValue(STATIC_LIST_NAME);
        inputProperties.listParamListName.setValue(UNDX_TEST_LIST_SMALL);
        MarketoSource source = new MarketoSource();
        source.initialize(null, inputProperties);
        MarketoClientService client = source.getClientService(null);
        //
        MarketoRecordResult result = client.getMultipleLeads(inputProperties, null);
        int counted = result.getRecordCount();
        assertTrue(result.getRecordCount() > 0);
        while (result.getRemainCount() > 0) {
            result = client.getMultipleLeads(inputProperties, result.getStreamPosition());
            assertNotNull(result.getRecords().get(0).get(0));
            LOG.debug("{}", result);
            counted += result.getRecordCount();
        }
        LOG.debug(result.getRecords().get(0).getSchema().toString());
        assertEquals("int", result.getRecords().get(0).getSchema().getField("Id").schema().getTypes().get(0).getName());
        assertTrue(counted > 4);
    }

    @Test
    public void testGetMultipleLeadsListNameFail() throws Exception {
        inputProperties.inputOperation.setValue(getMultipleLeads);
        inputProperties.leadSelectorSOAP.setValue(StaticListSelector);
        inputProperties.updateSchemaRelated();
        inputProperties.batchSize.setValue(200);
        //
        inputProperties.listParam.setValue(STATIC_LIST_NAME);
        inputProperties.listParamListName.setValue("undx_test_list******");
        MarketoSource source = new MarketoSource();
        source.initialize(null, inputProperties);
        MarketoClientService client = source.getClientService(null);
        //
        MarketoRecordResult result = client.getMultipleLeads(inputProperties, null);
        LOG.debug("{}", result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getErrors());
        assertEquals(0, result.getRecordCount());
        assertEquals(0, result.getRemainCount());
        assertEquals(emptyList(), client.getMultipleLeads(inputProperties, null).getRecords());
    }

    @Test
    public void testGetMultipleLeadsListId() throws Exception {
        inputProperties.inputOperation.setValue(getMultipleLeads);
        inputProperties.leadSelectorSOAP.setValue(StaticListSelector);
        inputProperties.updateSchemaRelated();
        inputProperties.batchSize.setValue(10);
        //
        inputProperties.listParam.setValue(STATIC_LIST_ID);
        inputProperties.listParamListId.setValue(UNDX_TEST_LIST_SMALL_ID);
        MarketoSource source = new MarketoSource();
        source.initialize(null, inputProperties);
        MarketoClientService client = source.getClientService(null);
        //
        MarketoRecordResult result = client.getMultipleLeads(inputProperties, null);
        LOG.debug("{}", result);
        assertTrue(result.isSuccess());
        assertTrue(result.getErrors().isEmpty());
        assertNotEquals(0, result.getRecordCount());
        assertNotEquals(0, result.getRemainCount());
        assertTrue(result.getRecordCount() > 4);
    }

    @Test
    public void testGetMultipleLeadsListIdFail() throws Exception {
        inputProperties.inputOperation.setValue(getMultipleLeads);
        inputProperties.leadSelectorSOAP.setValue(StaticListSelector);
        inputProperties.updateSchemaRelated();
        inputProperties.batchSize.setValue(200);
        //
        inputProperties.listParam.setValue(STATIC_LIST_ID);
        inputProperties.listParamListName.setValue("-666");
        MarketoSource source = new MarketoSource();
        source.initialize(null, inputProperties);
        MarketoClientService client = source.getClientService(null);
        //
        MarketoRecordResult result = client.getMultipleLeads(inputProperties, null);
        LOG.debug("{}", result);
        assertFalse(result.isSuccess()); // but not leads
        assertNotNull(result.getErrors());
        assertEquals(0, result.getRecordCount());
        assertEquals(0, result.getRemainCount());
        assertFalse(result.getRecordCount() > 0);
        assertEquals(emptyList(), result.getRecords());
    }

    @Test
    public void testGetMultipleLeadsLastUpdate() throws Exception {
        inputProperties.inputOperation.setValue(getMultipleLeads);
        inputProperties.leadSelectorSOAP.setValue(LastUpdateAtSelector);
        inputProperties.updateSchemaRelated();
        inputProperties.batchSize.setValue(10);
        //
        inputProperties.oldestUpdateDate.setValue(DATE_OLDEST_UPDATE);
        inputProperties.latestUpdateDate.setValue(DATE_LATEST_UPDATE);
        MarketoSource source = new MarketoSource();
        source.initialize(null, inputProperties);
        MarketoClientService client = source.getClientService(null);
        //
        assertTrue(client.getMultipleLeads(inputProperties, null).getRecords().size() > 0);
    }

    @Test
    public void testGetLeadActivity() throws Exception {
        inputProperties.inputOperation.setValue(getLeadActivity);
        inputProperties.leadKeyTypeSOAP.setValue(EMAIL);
        inputProperties.leadSelectorSOAP.setValue(LeadKeySelector);
        inputProperties.afterInputOperation();
        inputProperties.beforeMappingInput();
        inputProperties.batchSize.setValue(11);
        //
        inputProperties.leadKeyValue.setValue(EMAIL_LEAD_MANY_INFOS);

        MarketoSource source = new MarketoSource();
        source.initialize(null, inputProperties);
        MarketoClientService client = source.getClientService(null);
        //
        MarketoRecordResult result = client.getLeadActivity(inputProperties, null);
        LOG.debug("{}", result);
        assertTrue(result.isSuccess());
        List<IndexedRecord> records = result.getRecords();
        assertTrue(records.size() > 0);
        for (IndexedRecord r : records) {
            assertNotNull(r.get(0));
            assertTrue(r.get(0) instanceof Long);
        }
    }

    @Test
    public void testGetLeadActivityPagination() throws Exception {
        inputProperties.inputOperation.setValue(getLeadActivity);
        inputProperties.leadKeyTypeSOAP.setValue(EMAIL);
        inputProperties.leadSelectorSOAP.setValue(LeadKeySelector);
        inputProperties.updateSchemaRelated();
        inputProperties.batchSize.setValue(3);
        //
        inputProperties.leadKeyValue.setValue(EMAIL_LEAD_MANY_INFOS);
        MarketoSource source = new MarketoSource();
        source.initialize(null, inputProperties);
        MarketoClientService client = source.getClientService(null);
        //
        MarketoRecordResult result = client.getLeadActivity(inputProperties, null);
        int totalRecords = result.getRecordCount() + result.getRemainCount();
        int counted = result.getRecordCount();
        while (result.getRemainCount() > 0) {
            result = client.getLeadActivity(inputProperties, result.getStreamPosition());
            counted += result.getRecordCount();
        }
        assertEquals(totalRecords, counted);
    }

    @Test
    public void testGetLeadActivityIncludeFilter() throws Exception {
        inputProperties.inputOperation.setValue(getLeadActivity);
        inputProperties.leadKeyTypeSOAP.setValue(EMAIL);
        inputProperties.leadSelectorSOAP.setValue(LeadKeySelector);
        inputProperties.updateSchemaRelated();
        inputProperties.batchSize.setValue(11);
        //
        inputProperties.leadKeyValue.setValue(EMAIL_LEAD_MANY_INFOS);
        inputProperties.setIncludeTypes.setValue(true);
        inputProperties.includeTypes.type.getValue().add(NewLead.toString());
        inputProperties.includeTypes.type.getValue().add(ChangeDataValue.toString());
        // inputProperties.includeTypes.type.getValue().add(TMarketoInputProperties.IncludeExcludeFieldsSOAP.VisitWebpage.toString());

        MarketoSource source = new MarketoSource();
        source.initialize(null, inputProperties);
        MarketoClientService client = source.getClientService(null);
        //
        MarketoRecordResult result = client.getLeadActivity(inputProperties, null);
        List<IndexedRecord> activities = result.getRecords();
        assertTrue(activities.size() > 0);
        for (IndexedRecord r : activities) {
            assertTrue("New Lead".equals(r.get(2)) || "Change Data Value".equals(r.get(2)));
        }
    }

    @Test
    public void testGetLeadActivityExcludeFilter() throws Exception {
        inputProperties.inputOperation.setValue(getLeadActivity);
        inputProperties.leadKeyTypeSOAP.setValue(EMAIL);
        inputProperties.leadSelectorSOAP.setValue(LeadKeySelector);
        inputProperties.updateSchemaRelated();
        inputProperties.batchSize.setValue(11);
        //
        inputProperties.leadKeyValue.setValue(EMAIL_LEAD_MANY_INFOS);
        inputProperties.setExcludeTypes.setValue(true);
        inputProperties.excludeTypes.type.getValue().add(NewLead.toString());
        inputProperties.excludeTypes.type.getValue().add(ChangeDataValue.toString());

        MarketoSource source = new MarketoSource();
        source.initialize(null, inputProperties);
        MarketoClientService client = source.getClientService(null);
        //
        MarketoRecordResult result = client.getLeadActivity(inputProperties, null);
        List<IndexedRecord> activities = result.getRecords();
        assertTrue(activities.size() > 0);
        for (IndexedRecord r : activities) {
            assertTrue(!"New Lead".equals(r.get(2)) && !"Change Data Value".equals(r.get(2)));
        }
    }

    @Test
    public void testGetLeadsChanges() throws Exception {
        inputProperties.inputOperation.setValue(getLeadChanges);
        inputProperties.afterInputOperation();
        inputProperties.beforeMappingInput();
        inputProperties.batchSize.setValue(1000);
        //
        inputProperties.oldestCreateDate.setValue(DATE_OLDEST_CREATE);
        inputProperties.latestCreateDate.setValue(DATE_LATEST_CREATE);
        MarketoSource source = new MarketoSource();
        source.initialize(null, inputProperties);
        MarketoClientService client = source.getClientService(null);
        //
        MarketoRecordResult result = client.getLeadChanges(inputProperties, null);
        List<IndexedRecord> changes = result.getRecords();
        assertTrue(changes.size() > 0);
        assertTrue(result.getRemainCount() > 0);
        List<IndexedRecord> records = result.getRecords();
        assertTrue(records.size() > 0);
        for (IndexedRecord r : records) {
            assertNotNull(r.get(0));
            assertTrue(r.get(0) instanceof Long);
        }
    }

    @Test
    public void testGetLeadsChangesPagination() throws Exception {
        inputProperties.inputOperation.setValue(getLeadChanges);
        inputProperties.updateSchemaRelated();
        inputProperties.batchSize.setValue(100);
        //
        inputProperties.oldestCreateDate.setValue(DATE_OLDEST_CREATE);
        inputProperties.latestCreateDate.setValue(DATE_LATEST_CREATE);
        MarketoSource source = new MarketoSource();
        source.initialize(null, inputProperties);
        MarketoClientService client = source.getClientService(null);
        //
        MarketoRecordResult result = null;
        List<IndexedRecord> changes = null;
        int totalRecords = 0;
        int counted = 0;
        result = client.getLeadChanges(inputProperties, null);
        totalRecords = result.getRecordCount() + result.getRemainCount();
        counted = result.getRecordCount();
        while (result.getRemainCount() > 0) {
            result = client.getLeadChanges(inputProperties, result.getStreamPosition());
            counted += result.getRecordCount();
            changes = result.getRecords();
            assertTrue(changes.size() > 0);
        }
        assertTrue(inputProperties.batchSize.getValue() < counted);
    }

    @Test
    public void testGetLeadsChangesIncludeFilter() throws Exception {
        inputProperties.inputOperation.setValue(getLeadChanges);
        inputProperties.updateSchemaRelated();
        inputProperties.batchSize.setValue(1000);
        //
        inputProperties.oldestCreateDate.setValue(DATE_OLDEST_CREATE);
        inputProperties.latestCreateDate.setValue(DATE_LATEST_CREATE);
        inputProperties.setIncludeTypes.setValue(true);
        inputProperties.includeTypes.type.getValue().add(NewLead.toString());
        inputProperties.includeTypes.type.getValue().add(ChangeDataValue.toString());

        MarketoSource source = new MarketoSource();
        source.initialize(null, inputProperties);
        MarketoClientService client = source.getClientService(null);
        //
        MarketoRecordResult result = client.getLeadChanges(inputProperties, null);
        List<IndexedRecord> changes = result.getRecords();
        assertTrue(changes.size() > 0);
        for (IndexedRecord r : changes) {
            assertTrue("New Lead".equals(r.get(2)) || "Change Data Value".equals(r.get(2)));
        }
    }

    @Test
    public void testGetLeadsChangesExcludeFilter() throws Exception {
        inputProperties.inputOperation.setValue(getLeadChanges);
        inputProperties.updateSchemaRelated();
        inputProperties.batchSize.setValue(1000);
        //
        inputProperties.oldestCreateDate.setValue(DATE_OLDEST_CREATE);
        inputProperties.latestCreateDate.setValue(DATE_LATEST_CREATE);
        inputProperties.setExcludeTypes.setValue(true);
        inputProperties.excludeTypes.type.getValue().add(NewLead.toString());
        inputProperties.excludeTypes.type.getValue().add(ChangeDataValue.toString());

        MarketoSource source = new MarketoSource();
        source.initialize(null, inputProperties);
        MarketoClientService client = source.getClientService(null);
        //
        MarketoRecordResult result = client.getLeadChanges(inputProperties, null);
        List<IndexedRecord> changes = result.getRecords();
        assertTrue(changes.size() > 0);
        for (IndexedRecord r : changes) {
            assertTrue(!"New Lead".equals(r.get(2)) && !"Change Data Value".equals(r.get(2)));
        }
    }

    /*
     * 
     * 
     * ListOperations
     * 
     * 
     */
    @Test
    public void testAddToList() throws Exception {
        MarketoSource source = new MarketoSource();
        source.initialize(null, listProperties);
        MarketoClientService client = source.getClientService(null);
        //
        ListOperationParameters parms = new ListOperationParameters();
        parms.setApiMode(SOAP.name());
        parms.setListKeyValue(UNDX_TEST_LIST_SMALL);
        parms.setLeadKeyValue(new String[] { createdLeads.get(10).toString() });
        // first make sure to remove lead
        MarketoSyncResult result = client.removeFromList(parms);
        LOG.debug("result = {}.", result);
        List<SyncStatus> changes = result.getRecords();
        assertTrue(changes.size() > 0);
        for (SyncStatus r : changes) {
            assertNotNull(r);
            assertNotNull(r.getId());
            LOG.debug("r = {}.", r);
        }
        // then add it
        result = client.addToList(parms);
        LOG.debug("result = {}.", result);
        changes = result.getRecords();
        assertTrue(changes.size() > 0);
        for (SyncStatus r : changes) {
            assertNotNull(r);
            assertNotNull(r.getId());
            assertTrue(Boolean.parseBoolean(r.getStatus()));
            LOG.debug("r = {}.", r);
        }
    }

    @Test
    public void testAddToListFail() throws Exception {
        MarketoSource source = new MarketoSource();
        source.initialize(null, listProperties);
        MarketoClientService client = source.getClientService(null);
        //
        ListOperationParameters parms = new ListOperationParameters();
        parms.setApiMode(SOAP.name());
        parms.setListKeyValue(UNDX_TEST_LIST_SMALL);
        parms.setLeadKeyValue(new String[] { "12345" });
        // first make sure to remove lead
        MarketoSyncResult result;
        List<SyncStatus> changes;
        result = client.addToList(parms);
        LOG.debug("result = {}.", result);
        changes = result.getRecords();
        assertTrue(changes.size() > 0);
        for (SyncStatus r : changes) {
            assertNotNull(r);
            assertNotNull(r.getId());
            assertFalse(Boolean.parseBoolean(r.getStatus()));
            assertEquals("[20103] Lead Not Found.", r.getAvailableReason());
            LOG.debug("r = {}.", r);
        }
    }

    @Test
    public void testIsMemberOfList() throws Exception {
        MarketoSource source = new MarketoSource();
        source.initialize(null, listProperties);
        MarketoClientService client = source.getClientService(null);
        //
        ListOperationParameters parms = new ListOperationParameters();
        parms.setApiMode(SOAP.name());
        parms.setListKeyValue(UNDX_TEST_LIST_SMALL);
        parms.setStrict(true);
        parms.setLeadKeyValue(new String[] { createdLeads.get(0).toString(), createdLeads.get(1).toString(),
                createdLeads.get(2).toString(), "12345" });
        //
        MarketoSyncResult result = client.isMemberOfList(parms);
        LOG.debug("result = {}.", result);
        List<SyncStatus> changes = result.getRecords();
        assertTrue(changes.size() > 0);
        for (SyncStatus r : changes) {
            assertNotNull(r);
            assertNotNull(r.getId());
            assertNotNull(r.getStatus());
            LOG.debug("r = {}.", r);
        }
    }

    @Test
    public void testRemoveFromList() throws Exception {
        MarketoSource source = new MarketoSource();
        source.initialize(null, listProperties);
        MarketoClientService client = source.getClientService(null);
        //
        ListOperationParameters parms = new ListOperationParameters();
        parms.setApiMode(SOAP.name());
        parms.setListKeyValue(UNDX_TEST_LIST_SMALL);
        parms.setLeadKeyValue(new String[] { createdLeads.get(20).toString() });
        // first subscribe lead
        MarketoSyncResult result = client.addToList(parms);
        List<SyncStatus> changes = result.getRecords();
        assertTrue(changes.size() > 0);
        for (SyncStatus r : changes) {
            assertNotNull(r);
            assertNotNull(r.getId());
            assertTrue(Boolean.parseBoolean(r.getStatus()));
            LOG.debug("r = {}.", r);
        }
        // then remove it
        result = client.removeFromList(parms);
        LOG.debug("result = {}.", result);
        changes = result.getRecords();
        assertTrue(changes.size() > 0);
        for (SyncStatus r : changes) {
            assertNotNull(r);
            assertNotNull(r.getId());
            assertTrue(Boolean.parseBoolean(r.getStatus()));
            LOG.debug("r = {}.", r);
        }
    }
    /*
     * 
     * SyncLead Operations
     * 
     * 
     */

    @Test
    public void testConvertToLeadRecord() throws Exception {
        outProperties.outputOperation.setValue(OutputOperation.syncLead);
        outProperties.updateSchemaRelated();
        outProperties.afterOutputOperation();
        outProperties.beforeMappingInput();
        MarketoSOAPClient client = new MarketoSOAPClient(outProperties.connection).connect();
        IndexedRecord record = new GenericData.Record(outProperties.schemaInput.schema.getValue());
        record.put(0, 10);
        record.put(1, "undx@undx.net");
        LeadRecord lr = client.convertToLeadRecord(record, outProperties.mappingInput.getNameMappingsForMarketo());
        assertNotNull(lr);
        assertEquals(new Integer(10), lr.getId().getValue());
        assertEquals("undx@undx.net", lr.getEmail().getValue());
        // test attributes
        List<Field> fields = new ArrayList<>();
        Field field = new Schema.Field("FirstName", Schema.create(Schema.Type.STRING), null, (Object) null);
        fields.add(field);
        field = new Schema.Field("LastName", Schema.create(Schema.Type.STRING), null, (Object) null);
        fields.add(field);
        Schema s = outProperties.newSchema(outProperties.schemaInput.schema.getValue(), "leadAttribute", fields);
        record = new GenericData.Record(s);
        record.put(0, 10);
        record.put(1, "undx@undx.net");
        record.put(2, "ForeignPersonSysId");
        record.put(3, "SFDC");// CUSTOM, SFDC, NETSUITE;
        record.put(5, "My firstName");
        record.put(6, "My lastName");
        outProperties.schemaInput.schema.setValue(s);
        outProperties.beforeMappingInput();
        lr = client.convertToLeadRecord(record, outProperties.mappingInput.getNameMappingsForMarketo());
        assertNotNull(lr);
        assertEquals(new Integer(10), lr.getId().getValue());
        assertEquals("undx@undx.net", lr.getEmail().getValue());
        assertEquals("ForeignPersonSysId", lr.getForeignSysPersonId().getValue());
        assertEquals("SFDC", lr.getForeignSysType().getValue().toString());
        assertEquals("My firstName", lr.getLeadAttributeList().getValue().getAttributes().get(0).getAttrValue().toString());
        assertEquals("My lastName", lr.getLeadAttributeList().getValue().getAttributes().get(1).getAttrValue().toString());
        // test mandatory field
        record.put(0, 10);
        record.put(1, null);
        record.put(2, null);
        lr = client.convertToLeadRecord(record, outProperties.mappingInput.getNameMappingsForMarketo());
        assertNotNull(lr);
        record.put(0, null);
        record.put(1, "toto@toto.org");
        record.put(2, null);
        lr = client.convertToLeadRecord(record, outProperties.mappingInput.getNameMappingsForMarketo());
        assertNotNull(lr);
        assertNotNull(lr);
        record.put(0, null);
        record.put(1, null);
        record.put(2, "SSDDSSDSD");
        lr = client.convertToLeadRecord(record, outProperties.mappingInput.getNameMappingsForMarketo());
        assertNotNull(lr);
        record.put(0, null);
        record.put(1, null);
        record.put(2, null);
        try {
            lr = client.convertToLeadRecord(record, outProperties.mappingInput.getNameMappingsForMarketo());
            assertNotNull(lr);
        } catch (MarketoException e) {
            assertEquals("syncLead error: Missing mandatory field for operation.", e.getMessage());
        }
    }

    @Test
    public void testSyncLead() throws Exception {
        MarketoSource source = new MarketoSource();
        outProperties.outputOperation.setValue(OutputOperation.syncLead);
        outProperties.updateSchemaRelated();
        source.initialize(null, listProperties);
        MarketoClientService client = source.getClientService(null);
        // test attributes
        List<Field> fields = new ArrayList<>();
        Field field = new Schema.Field("FirstName", Schema.create(Schema.Type.STRING), null, (Object) null);
        fields.add(field);
        field = new Schema.Field("LastName", Schema.create(Schema.Type.STRING), null, (Object) null);
        fields.add(field);
        field = new Schema.Field("AccountType", Schema.create(Schema.Type.STRING), null, (Object) null);
        fields.add(field);
        Schema s = outProperties.newSchema(outProperties.schemaInput.schema.getValue(), "leadAttribute", fields);
        IndexedRecord record = new GenericData.Record(s);
        record.put(0, null);
        record.put(1, "undx@undx.net");
        record.put(2, "ForeignPersonSysId");
        record.put(3, "SFDC");// CUSTOM, SFDC, NETSUITE;
        record.put(4, "My firstName");
        record.put(5, "My lastName");
        record.put(6, "Conservative customer");
        outProperties.schemaInput.schema.setValue(s);
        outProperties.beforeMappingInput();
        //
        MarketoSyncResult result = client.syncLead(outProperties, record);
        assertEquals("UPDATED", result.getRecords().get(0).getStatus());
    }

    @Test
    public void testSyncMultipleLeads() throws Exception {
        MarketoSource source = new MarketoSource();
        outProperties.outputOperation.setValue(OutputOperation.syncLead);
        outProperties.updateSchemaRelated();
        source.initialize(null, listProperties);
        MarketoClientService client = source.getClientService(null);
        // test attributes
        List<Field> fields = new ArrayList<>();
        Field field = new Schema.Field("AccountType", Schema.create(Schema.Type.STRING), null, (Object) null);
        fields.add(field);
        Schema s = outProperties.newSchema(outProperties.schemaInput.schema.getValue(), "leadAttribute", fields);
        outProperties.schemaInput.schema.setValue(s);
        outProperties.beforeMappingInput();
        List<IndexedRecord> records = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            IndexedRecord record = new GenericData.Record(s);
            record.put(0, null);
            record.put(1, "undx" + i + "@undx.net");
            record.put(2, "");
            record.put(3, "CUSTOM");// CUSTOM, SFDC, NETSUITE;
            record.put(4, "customer" + i);
            records.add(record);
        }
        //
        MarketoSyncResult result = client.syncMultipleLeads(outProperties, records);
        for (SyncStatus status : result.getRecords()) {
            assertEquals("CREATED", status.getStatus());
        }
        //
        outProperties.deDupeEnabled.setValue(true);
        result = client.syncMultipleLeads(outProperties, records);
        for (SyncStatus status : result.getRecords()) {
            assertEquals("UPDATED", status.getStatus());
        }
    }

    @Test(expected = IOException.class)
    public void testBUG_TDI38439_MarketoWizardConnectionFail() throws Exception {
        inputProperties.connection.endpoint.setValue(ENDPOINT_SOAP);
        inputProperties.connection.clientAccessId.setValue(USERID_SOAP);
        inputProperties.connection.secretKey.setValue("false");
        MarketoSource source = new MarketoSource();
        source.initialize(null, inputProperties);
        MarketoClientService client = source.getClientService(null);
        fail("Shouldn't be here");
    }

    @Test
    public void testBUG_TDI38439_MarketoWizardConnection() throws Exception {
        inputProperties.connection.endpoint.setValue(ENDPOINT_SOAP);
        inputProperties.connection.clientAccessId.setValue(USERID_SOAP);
        inputProperties.connection.secretKey.setValue(SECRETKEY_SOAP);
        MarketoSource source = new MarketoSource();
        source.initialize(null, inputProperties);
        MarketoClientService client = source.getClientService(null);
        assertNotNull(client);
    }

}
