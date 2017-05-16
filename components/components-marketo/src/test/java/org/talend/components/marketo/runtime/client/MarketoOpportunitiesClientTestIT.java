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

import static org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode.REST;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.marketo.runtime.MarketoBaseTestIT;
import org.talend.components.marketo.runtime.MarketoSource;
import org.talend.components.marketo.runtime.client.type.MarketoRecordResult;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties;

public class MarketoOpportunitiesClientTestIT extends MarketoBaseTestIT {

    TMarketoInputProperties iprops;

    TMarketoOutputProperties oprops;

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

    @Ignore
    @Test
    public void testDescribeOpportunities() throws Exception {
        MarketoSource source = new MarketoSource();
        source.initialize(null, iprops);
        MarketoRESTClient client = (MarketoRESTClient) source.getClientService(null);
        MarketoRecordResult opps = client.describeOpportunity(iprops);
        LOG.debug("opps = {}.", opps);
    }

}
