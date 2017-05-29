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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode.REST;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.marketo.MarketoComponentProperties;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.runtime.MarketoBaseTestIT;
import org.talend.components.marketo.runtime.MarketoSource;
import org.talend.components.marketo.runtime.client.type.MarketoRecordResult;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.InputOperation;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.StandardAction;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties;

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
        iprops.connection.apiMode.setValue(REST);
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
        oprops.connection.apiMode.setValue(REST);
        oprops.connection.endpoint.setValue(ENDPOINT_REST);
        oprops.connection.clientAccessId.setValue(USERID_REST);
        oprops.connection.secretKey.setValue(SECRETKEY_REST);
        oprops.schemaInput.setupProperties();
        oprops.setupProperties();
        oprops.connection.setupLayout();
        oprops.schemaInput.setupLayout();
        oprops.setupLayout();
        //
        s = MarketoConstants.getCampaignSchema();
    }

    public MarketoRESTClient getClient(MarketoComponentProperties props) throws IOException {
        MarketoSource source = new MarketoSource();
        source.initialize(null, props);
        return (MarketoRESTClient) source.getClientService(null);
    }

    @Ignore
    @Test
    public void testGetCompanies() throws Exception {
        iprops.inputOperation.setValue(InputOperation.Company);
        iprops.standardAction.setValue(StandardAction.get);
        iprops.afterInputOperation();
        MarketoRESTClient client = getClient(iprops);
        MarketoRecordResult cmp = client.describeCompanies(iprops);
        assertNotNull(cmp.getRecords());
    }

    @Ignore
    @Test
    public void testDescribeCompanies() throws Exception {
        iprops.inputOperation.setValue(InputOperation.Company);
        iprops.standardAction.setValue(StandardAction.describe);
        iprops.afterInputOperation();
        int rCount = 0;
        MarketoRESTClient client = getClient(iprops);
        MarketoRecordResult cmp = client.describeCompanies(iprops);
        rCount = cmp.getRecordCount();
        for (IndexedRecord r : cmp.getRecords()) {
            LOG.debug("r = {}.", r);
        }
        while (cmp.getStreamPosition() != null) {
            // cmp = client.describeCompanies(iprops, cmp.getStreamPosition());
            for (IndexedRecord r : cmp.getRecords()) {
                LOG.debug("r = {}.", r);
            }
            rCount += cmp.getRecordCount();
        }
        assertTrue(rCount > 300);
    }

    @Test
    public void testSyncCompanies() throws Exception {
    }

    @Test
    public void testDeleteCompany() throws Exception {
    }

}
