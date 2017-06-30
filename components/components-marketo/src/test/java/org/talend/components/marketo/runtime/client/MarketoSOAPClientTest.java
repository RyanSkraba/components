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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode.SOAP;

import java.util.Arrays;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.runtime.client.type.ListOperationParameters;
import org.talend.components.marketo.runtime.client.type.MarketoRecordResult;
import org.talend.components.marketo.runtime.client.type.MarketoSyncResult;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadKeyTypeSOAP;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadSelector;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.ListParam;
import org.talend.components.marketo.tmarketolistoperation.TMarketoListOperationProperties;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.InputOperation;

import com.marketo.mktows.ActivityRecord;
import com.marketo.mktows.ArrayOfActivityRecord;
import com.marketo.mktows.ArrayOfAttribute;
import com.marketo.mktows.ArrayOfLeadChangeRecord;
import com.marketo.mktows.ArrayOfLeadRecord;
import com.marketo.mktows.ArrayOfLeadStatus;
import com.marketo.mktows.ArrayOfSyncStatus;
import com.marketo.mktows.AuthenticationHeader;
import com.marketo.mktows.LeadActivityList;
import com.marketo.mktows.LeadChangeRecord;
import com.marketo.mktows.LeadKey;
import com.marketo.mktows.LeadKeyRef;
import com.marketo.mktows.LeadRecord;
import com.marketo.mktows.LeadStatus;
import com.marketo.mktows.LeadSyncStatus;
import com.marketo.mktows.MktowsContextHeader;
import com.marketo.mktows.MktowsPort;
import com.marketo.mktows.ObjectFactory;
import com.marketo.mktows.ParamsGetLead;
import com.marketo.mktows.ParamsGetLeadActivity;
import com.marketo.mktows.ParamsGetLeadChanges;
import com.marketo.mktows.ParamsGetMultipleLeads;
import com.marketo.mktows.ParamsListMObjects;
import com.marketo.mktows.ParamsListOperation;
import com.marketo.mktows.ParamsSyncLead;
import com.marketo.mktows.ParamsSyncMultipleLeads;
import com.marketo.mktows.ResultGetLead;
import com.marketo.mktows.ResultGetLeadChanges;
import com.marketo.mktows.ResultGetMultipleLeads;
import com.marketo.mktows.ResultListOperation;
import com.marketo.mktows.ResultSyncLead;
import com.marketo.mktows.ResultSyncMultipleLeads;
import com.marketo.mktows.StreamPosition;
import com.marketo.mktows.SuccessGetLead;
import com.marketo.mktows.SuccessGetLeadActivity;
import com.marketo.mktows.SuccessGetLeadChanges;
import com.marketo.mktows.SuccessGetMultipleLeads;
import com.marketo.mktows.SuccessListOperation;
import com.marketo.mktows.SuccessSyncLead;
import com.marketo.mktows.SuccessSyncMultipleLeads;
import com.marketo.mktows.SyncStatus;

public class MarketoSOAPClientTest {

    MarketoSOAPClient client;

    MktowsPort port;

    ObjectFactory objectFactory;

    TMarketoInputProperties iprops;

    TMarketoOutputProperties oprops;

    TMarketoListOperationProperties lprops;

    MarketoRecordResult mktoRR;

    MarketoSyncResult mktoSR;

    public SuccessGetLead getGetLeadResult() {
        SuccessGetLead result = new SuccessGetLead();
        ResultGetLead res = new ResultGetLead();
        res.setCount(1);
        ArrayOfLeadRecord leadrecords = new ArrayOfLeadRecord();
        LeadRecord r = new LeadRecord();
        r.setId(objectFactory.createLeadRecordId(12345));
        r.setEmail(objectFactory.createLeadRecordEmail("t@t.com"));
        r.setForeignSysPersonId(objectFactory.createLeadRecordForeignSysPersonId(""));
        r.setForeignSysType(objectFactory.createLeadRecordForeignSysType(null));
        leadrecords.getLeadRecords().add(r);
        QName qname = new QName("http://www.marketo.com/mktows/", "leadAttributeList");
        JAXBElement<ArrayOfLeadRecord> attrList = new JAXBElement(qname, LeadRecord.class, leadrecords);
        res.setLeadRecordList(attrList);
        result.setResult(res);

        return result;
    }

    public SuccessGetMultipleLeads getMultipleLeadResult() {
        SuccessGetMultipleLeads result = new SuccessGetMultipleLeads();
        ResultGetMultipleLeads res = new ResultGetMultipleLeads();
        res.setReturnCount(1);
        res.setRemainingCount(0);
        ArrayOfLeadRecord leadrecords = new ArrayOfLeadRecord();
        LeadRecord r = new LeadRecord();
        r.setId(objectFactory.createLeadRecordId(12345));
        r.setEmail(objectFactory.createLeadRecordEmail("t@t.com"));
        r.setForeignSysPersonId(objectFactory.createLeadRecordForeignSysPersonId(""));
        r.setForeignSysType(objectFactory.createLeadRecordForeignSysType(null));
        leadrecords.getLeadRecords().add(r);
        QName qname = new QName("http://www.marketo.com/mktows/", "leadAttributeList");
        JAXBElement<ArrayOfLeadRecord> attrList = new JAXBElement(qname, LeadRecord.class, leadrecords);
        res.setLeadRecordList(attrList);
        result.setResult(res);

        return result;
    }

    public SuccessGetLeadActivity getLeadActivityResult() {
        SuccessGetLeadActivity result = new SuccessGetLeadActivity();
        LeadActivityList res = new LeadActivityList();
        res.setReturnCount(1);
        res.setRemainingCount(0);
        StreamPosition sp = new StreamPosition();
        sp.setOffset(objectFactory.createStreamPositionOffset(""));
        res.setNewStartPosition(sp);
        ArrayOfActivityRecord arecords = new ArrayOfActivityRecord();
        ActivityRecord ar = new ActivityRecord();
        ar.setId(123456);
        ar.setMktgAssetName("asset");
        ar.setActivityDateTime(null);
        ar.setActivityType(null);
        ar.setCampaign(null);
        ar.setForeignSysId(null);
        ar.setForeignSysOrgId(null);
        ar.setPersonName(null);
        ar.setOrgName(null);
        ar.setActivityAttributes(objectFactory.createActivityRecordActivityAttributes(new ArrayOfAttribute()));
        arecords.getActivityRecords().add(ar);
        res.setActivityRecordList(objectFactory.createLeadActivityListActivityRecordList(arecords));
        result.setLeadActivityList(res);

        return result;
    }

    public SuccessGetLeadChanges getLeadChangeResult() {
        SuccessGetLeadChanges result = new SuccessGetLeadChanges();
        ResultGetLeadChanges res = new ResultGetLeadChanges();
        res.setReturnCount(1);
        res.setRemainingCount(0);
        StreamPosition sp = new StreamPosition();
        sp.setOffset(objectFactory.createStreamPositionOffset(""));
        res.setNewStartPosition(sp);

        ArrayOfLeadChangeRecord lcr = new ArrayOfLeadChangeRecord();
        LeadChangeRecord lc = new LeadChangeRecord();
        lc.setId(123456);
        lc.setMktgAssetName(objectFactory.createLeadChangeRecordMktgAssetName("asset"));
        lc.setActivityDateTime(null);
        lc.setActivityType(null);
        lc.setCampaign(null);
        lc.setMktPersonId(null);
        lc.setActivityAttributes(objectFactory.createActivityRecordActivityAttributes(new ArrayOfAttribute()));

        lcr.getLeadChangeRecords().add(lc);
        res.setLeadChangeRecordList(objectFactory.createResultGetLeadChangesLeadChangeRecordList(lcr));
        result.setResult(res);

        return result;
    }

    public SuccessListOperation getListOperation(boolean isSuccess, String status) {
        SuccessListOperation result = new SuccessListOperation();
        ResultListOperation rlo = new ResultListOperation();
        if (isSuccess) {
            rlo.setSuccess(true);
            ArrayOfLeadStatus aols = new ArrayOfLeadStatus();
            LeadStatus ls = new LeadStatus();
            LeadKey lk = new LeadKey();
            lk.setKeyType(LeadKeyRef.IDNUM);
            lk.setKeyValue("12345");
            ls.setLeadKey(lk);
            aols.getLeadStatuses().add(ls);
            rlo.setStatusList(objectFactory.createResultListOperationStatusList(aols));
            result.setResult(rlo);
        } else {
            rlo.setSuccess(false);
            rlo.setStatusList(objectFactory.createResultListOperationStatusList(null));
            result.setResult(rlo);
        }
        return result;
    }

    public SuccessSyncLead getSyncLeadResult() {
        SuccessSyncLead resut = new SuccessSyncLead();

        ResultSyncLead rsl = new ResultSyncLead();
        rsl.setLeadId(12345);
        SyncStatus status = new SyncStatus();
        status.setLeadId(12345);
        status.setStatus(LeadSyncStatus.CREATED);
        status.setError(objectFactory.createSyncStatusError(null));
        rsl.setSyncStatus(status);
        LeadRecord r = objectFactory.createLeadRecord();
        r.setId(objectFactory.createLeadRecordId(12345));
        r.setEmail(objectFactory.createLeadRecordEmail("t@t.com"));
        r.setForeignSysPersonId(objectFactory.createLeadRecordForeignSysPersonId(""));
        r.setForeignSysType(objectFactory.createLeadRecordForeignSysType(null));
        rsl.setLeadRecord(objectFactory.createResultSyncLeadLeadRecord(r));
        resut.setResult(rsl);

        return resut;
    }

    public SuccessSyncMultipleLeads getSyncMultipleLeadsResult() {
        SuccessSyncMultipleLeads result = new SuccessSyncMultipleLeads();
        ResultSyncMultipleLeads rsml = new ResultSyncMultipleLeads();
        ArrayOfSyncStatus aoss = new ArrayOfSyncStatus();
        SyncStatus ss = new SyncStatus();
        ss.setLeadId(12345);
        ss.setStatus(LeadSyncStatus.CREATED);
        ss.setError(objectFactory.createSyncStatusError(null));
        aoss.getSyncStatuses().add(ss);
        rsml.setSyncStatusList(aoss);
        result.setResult(rsml);
        return result;
    }

    @Before
    public void setUp() throws Exception {
        iprops = new TMarketoInputProperties("test");
        iprops.schemaInput.setupProperties();
        iprops.schemaInput.setupLayout();
        iprops.connection.setupProperties();
        iprops.connection.setupLayout();
        iprops.connection.setupProperties();
        iprops.connection.apiMode.setValue(APIMode.SOAP);
        iprops.connection.endpoint.setValue("https://fake.io");
        iprops.connection.clientAccessId.setValue("clientaccess");
        iprops.connection.secretKey.setValue("sekret");
        iprops.setupProperties();
        iprops.setupLayout();

        oprops = new TMarketoOutputProperties("test");
        oprops.schemaInput.setupProperties();
        oprops.schemaInput.setupLayout();
        oprops.connection.setupProperties();
        oprops.connection.setupLayout();
        oprops.connection.setupProperties();
        oprops.connection.apiMode.setValue(APIMode.SOAP);
        oprops.connection.endpoint.setValue("https://fake.io");
        oprops.connection.clientAccessId.setValue("clientaccess");
        oprops.connection.secretKey.setValue("sekret");
        oprops.setupProperties();
        oprops.setupLayout();

        lprops = new TMarketoListOperationProperties("test");
        lprops.schemaInput.setupProperties();
        lprops.schemaInput.setupLayout();
        lprops.connection.setupProperties();
        lprops.connection.setupLayout();
        lprops.connection.setupProperties();
        lprops.connection.apiMode.setValue(APIMode.SOAP);
        lprops.connection.endpoint.setValue("https://fake.io");
        lprops.connection.clientAccessId.setValue("clientaccess");
        lprops.connection.secretKey.setValue("sekret");
        lprops.setupProperties();
        lprops.setupLayout();

        client = spy(new MarketoSOAPClient(iprops.connection));
        port = mock(MktowsPort.class);
        AuthenticationHeader header = mock(AuthenticationHeader.class);
        doReturn(port).when(client).getMktowsApiSoapPort();
        doReturn(header).when(client).getAuthentificationHeader();
        doReturn(null).when(port).listMObjects(any(ParamsListMObjects.class), any(AuthenticationHeader.class));
        client.connect();

        objectFactory = new ObjectFactory();
    }

    @Test
    public void testGetApi() throws Exception {
        assertEquals("SOAP", client.getApi());
    }

    @Test
    public void testToString() throws Exception {
        assertTrue(client.toString().contains("Marketo SOAP API Client"));
    }

    @Test
    public void testGetLead() throws Exception {
        doReturn(getGetLeadResult()).when(port).getLead(any(ParamsGetLead.class), any(AuthenticationHeader.class));
        iprops.afterInputOperation();
        iprops.leadKeyTypeSOAP.setValue(LeadKeyTypeSOAP.IDNUM);
        mktoRR = client.getLead(iprops, null);
        assertNotNull(mktoRR);
        assertTrue(mktoRR.isSuccess());
        doThrow(new RuntimeException("error")).when(port).getLead(any(ParamsGetLead.class), any(AuthenticationHeader.class));
        mktoRR = client.getLead(iprops, null);
        assertNotNull(mktoRR);
        assertFalse(mktoRR.isSuccess());
    }

    @Test
    public void testGetMultipleLeads() throws Exception {
        doReturn(getMultipleLeadResult()).when(port).getMultipleLeads(any(ParamsGetMultipleLeads.class),
                any(AuthenticationHeader.class));
        iprops.inputOperation.setValue(InputOperation.getMultipleLeads);
        iprops.leadKeyValues.setValue("12345");
        iprops.leadKeyTypeSOAP.setValue(LeadKeyTypeSOAP.IDNUM);
        iprops.afterInputOperation();
        mktoRR = client.getMultipleLeads(iprops, null);
        assertNotNull(mktoRR);
        assertTrue(mktoRR.isSuccess());
        //
        iprops.leadSelectorSOAP.setValue(LeadSelector.LastUpdateAtSelector);
        iprops.oldestUpdateDate.setValue("2017-01-20 00:00:00 +0100");
        iprops.latestUpdateDate.setValue("2017-01-31 00:00:00 +0100");
        mktoRR = client.getMultipleLeads(iprops, null);
        assertNotNull(mktoRR);
        assertTrue(mktoRR.isSuccess());
        //
        iprops.leadSelectorSOAP.setValue(LeadSelector.StaticListSelector);
        iprops.listParam.setValue(ListParam.STATIC_LIST_NAME);
        iprops.listParamListName.setValue("test_list");
        mktoRR = client.getMultipleLeads(iprops, null);
        assertNotNull(mktoRR);
        assertTrue(mktoRR.isSuccess());
        //
        doReturn(null).when(port).getMultipleLeads(any(ParamsGetMultipleLeads.class), any(AuthenticationHeader.class));
        iprops.leadSelectorSOAP.setValue(LeadSelector.StaticListSelector);
        iprops.listParam.setValue(ListParam.STATIC_LIST_ID);
        iprops.listParamListId.setValue(666);
        mktoRR = client.getMultipleLeads(iprops, null);
        assertNotNull(mktoRR);
        assertTrue(mktoRR.isSuccess());
        //
        doThrow(new RuntimeException("error")).when(port).getMultipleLeads(any(ParamsGetMultipleLeads.class),
                any(AuthenticationHeader.class));
        mktoRR = client.getMultipleLeads(iprops, null);
        assertNotNull(mktoRR);
        assertFalse(mktoRR.isSuccess());
    }

    @Test
    public void testListOperations() throws Exception {
        doReturn(getListOperation(true, "added")).when(port).listOperation(any(ParamsListOperation.class),
                any(AuthenticationHeader.class));
        lprops.afterListOperation();
        ListOperationParameters parms = new ListOperationParameters();
        parms.setApiMode(SOAP.name());
        parms.setListKeyValue("test_list");
        parms.setLeadKeyValue(new String[] { "12345" });
        mktoSR = client.addToList(parms);
        assertNotNull(mktoSR);
        assertTrue(mktoSR.isSuccess());
        doReturn(getListOperation(false, "skipped")).when(port).listOperation(any(ParamsListOperation.class),
                any(AuthenticationHeader.class));
        mktoSR = client.removeFromList(parms);
        assertTrue(mktoSR.isSuccess());
        assertNotNull(mktoSR.getErrorsString());
        doReturn(getListOperation(true, "memberof")).when(port).listOperation(any(ParamsListOperation.class),
                any(AuthenticationHeader.class));
        mktoSR = client.isMemberOfList(parms);
        assertTrue(mktoSR.isSuccess());
        assertNotNull(mktoSR.getRecords().get(0));

        doThrow(new RuntimeException("error")).when(port).listOperation(any(ParamsListOperation.class),
                any(AuthenticationHeader.class));
        mktoSR = client.isMemberOfList(parms);
        assertFalse(mktoSR.isSuccess());
        assertNotNull(mktoSR.getRecords());
    }

    @Test
    public void testGetLeadActivity() throws Exception {
        doReturn(getLeadActivityResult()).when(port).getLeadActivity(any(ParamsGetLeadActivity.class),
                any(AuthenticationHeader.class));
        iprops.afterInputOperation();
        iprops.leadKeyTypeSOAP.setValue(LeadKeyTypeSOAP.IDNUM);
        mktoRR = client.getLeadActivity(iprops, null);
        assertNotNull(mktoRR);
        assertTrue(mktoRR.isSuccess());
        //
        doThrow(new RuntimeException("error")).when(port).getLeadActivity(any(ParamsGetLeadActivity.class),
                any(AuthenticationHeader.class));
        mktoRR = client.getLeadActivity(iprops, null);
        assertNotNull(mktoRR);
        assertFalse(mktoRR.isSuccess());
    }

    @Test
    public void testGetLeadChanges() throws Exception {
        doReturn(getLeadChangeResult()).when(port).getLeadChanges(any(ParamsGetLeadChanges.class),
                any(AuthenticationHeader.class));
        iprops.afterInputOperation();
        iprops.leadKeyTypeSOAP.setValue(LeadKeyTypeSOAP.IDNUM);
        iprops.oldestCreateDate.setValue("2017-01-20 00:00:00 +0100");
        iprops.latestCreateDate.setValue("2017-01-31 00:00:00 +0100");
        mktoRR = client.getLeadChanges(iprops, null);
        assertNotNull(mktoRR);
        assertTrue(mktoRR.isSuccess());
        //
        doThrow(new RuntimeException("error")).when(port).getLeadChanges(any(ParamsGetLeadChanges.class),
                any(AuthenticationHeader.class));
        mktoRR = client.getLeadChanges(iprops, null);
        assertNotNull(mktoRR);
        assertFalse(mktoRR.isSuccess());
    }

    @Test
    public void testSyncLead() throws Exception {
        doReturn(getSyncLeadResult()).when(port).syncLead(any(ParamsSyncLead.class), any(AuthenticationHeader.class),
                any(MktowsContextHeader.class));
        oprops.afterOutputOperation();
        oprops.beforeMappingInput();
        mktoSR = client.syncLead(oprops, null);
        assertNotNull(mktoSR);
        assertFalse(mktoSR.isSuccess());
        IndexedRecord record = new Record(MarketoConstants.getSOAPOutputSchemaForSyncLead());
        record.put(0, 12345);
        record.put(1, "t@t.com");
        mktoSR = client.syncLead(oprops, record);
        assertNotNull(mktoSR);
        assertTrue(mktoSR.isSuccess());
        //
        doThrow(new RuntimeException("error")).when(port).syncLead(any(ParamsSyncLead.class), any(AuthenticationHeader.class),
                any(MktowsContextHeader.class));
        mktoSR = client.syncLead(oprops, record);
        assertNotNull(mktoSR);
        assertFalse(mktoSR.isSuccess());
    }

    @Test
    public void testSyncMultipleLeads() throws Exception {
        doReturn(getSyncMultipleLeadsResult()).when(port).syncMultipleLeads(any(ParamsSyncMultipleLeads.class),
                any(AuthenticationHeader.class));
        oprops.afterOutputOperation();
        oprops.beforeMappingInput();
        mktoSR = client.syncLead(oprops, null);
        assertNotNull(mktoSR);
        assertFalse(mktoSR.isSuccess());
        IndexedRecord record = new Record(MarketoConstants.getSOAPOutputSchemaForSyncLead());
        record.put(0, 12345);
        record.put(1, "t@t.com");
        mktoSR = client.syncMultipleLeads(oprops, Arrays.asList(record));
        assertNotNull(mktoSR);
        assertTrue(mktoSR.isSuccess());
        //
        doThrow(new RuntimeException("error")).when(port).syncMultipleLeads(any(ParamsSyncMultipleLeads.class),
                any(AuthenticationHeader.class));
        mktoSR = client.syncMultipleLeads(oprops, Arrays.asList(record));
        assertNotNull(mktoSR);
        assertFalse(mktoSR.isSuccess());
    }

}
