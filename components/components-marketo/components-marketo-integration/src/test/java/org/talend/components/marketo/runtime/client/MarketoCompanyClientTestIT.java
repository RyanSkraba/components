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
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.marketo.MarketoComponentProperties;
import org.talend.components.marketo.runtime.MarketoBaseTestIT;
import org.talend.components.marketo.runtime.MarketoSink;
import org.talend.components.marketo.runtime.MarketoSource;
import org.talend.components.marketo.runtime.client.type.MarketoRecordResult;
import org.talend.components.marketo.runtime.client.type.MarketoSyncResult;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.StandardAction;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.CustomObjectSyncAction;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.InputOperation;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.OutputOperation;

public class MarketoCompanyClientTestIT extends MarketoBaseTestIT {

    TMarketoInputProperties iprops;

    TMarketoOutputProperties oprops;

    Schema s;

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
        iprops.batchSize.setValue(300);
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
        //
        s = SchemaBuilder
                .record("sn")
                .fields()
                .name("externalCompanyId")
                .type()
                .stringType()
                .noDefault() //
                .name("company")
                .type()
                .stringType()
                .noDefault()//
                .name("website")
                .type()
                .intType()
                .noDefault()//
                .name("numberOfEmployees")
                .type()
                .intType()
                .noDefault()//
                .endRecord();
    }

    public MarketoRESTClient getClient(MarketoComponentProperties props) throws IOException {
        MarketoSource source = new MarketoSource();
        source.initialize(null, props);
        return (MarketoRESTClient) source.getClientService(null);
    }

    @Test
    public void testGetCompanies() throws Exception {
        iprops.inputOperation.setValue(InputOperation.Company);
        iprops.standardAction.setValue(StandardAction.get);
        iprops.afterInputOperation();
        iprops.customObjectFilterType.setValue("externalCompanyId");
        iprops.customObjectFilterValues.setValue("company0");
        MarketoRESTClient client = getClient(iprops);
        MarketoRecordResult cmp = client.getCompanies(iprops, null);
        assertNotNull(cmp.getRecords());
        LOG.debug("[testGetCompanies] {}.", cmp.getRecords());
    }

    @Test
    public void testGetCompaniesFail() throws Exception {
        iprops.inputOperation.setValue(InputOperation.Company);
        iprops.standardAction.setValue(StandardAction.get);
        iprops.afterInputOperation();
        iprops.customObjectFilterType.setValue("billingCountry");
        iprops.customObjectFilterValues.setValue("company0");
        MarketoRESTClient client = getClient(iprops);
        MarketoRecordResult cmp = client.getCompanies(iprops, null);
        assertFalse(cmp.isSuccess());
    }

    @Test
    public void testDescribeCompanies() throws Exception {
        iprops.inputOperation.setValue(InputOperation.Company);
        iprops.standardAction.setValue(StandardAction.describe);
        iprops.afterInputOperation();
        int rCount = 0;
        MarketoRESTClient client = getClient(iprops);
        MarketoRecordResult cmp = client.describeCompanies(iprops);
        assertEquals(1, cmp.getRecordCount());
        assertNotNull(cmp.getRecords().get(0));
        for (IndexedRecord r : cmp.getRecords()) {
            LOG.debug("r = {}.", r);
        }
    }

    @Test
    public void testSyncCompanies() throws Exception {
        oprops.outputOperation.setValue(OutputOperation.syncCompanies);
        oprops.customObjectSyncAction.setValue(CustomObjectSyncAction.createOrUpdate);
        List<IndexedRecord> records = new ArrayList<>();
        IndexedRecord r1;
        for (int i = 0; i < 3; i++) {
            r1 = new Record(s);
            r1.put(0, "company" + i);
            r1.put(1, "Talend Mega company " + i);
            r1.put(2, "https://talend.com");
            r1.put(3, i * 300);
            records.add(r1);
        }
        MarketoSink sink = new MarketoSink();
        sink.initialize(null, oprops);
        MarketoRESTClient client = (MarketoRESTClient) sink.getClientService(null);
        MarketoSyncResult rs = client.syncCompanies(oprops, records);
        LOG.debug("rs = {}.", rs);
    }

    @Test
    public void testDeleteCompany() throws Exception {

    }

}
