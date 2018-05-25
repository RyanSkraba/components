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
package org.talend.components.marketo.runtime;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.marketo.data.MarketoDatastoreProperties;
import org.talend.components.marketo.runtime.data.MarketoDatastoreRuntime;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

public class MarketoDatastoreRuntimeTestIT extends MarketoBaseTestIT {

    private MarketoDatastoreProperties datastore;

    private MarketoDatastoreRuntime runtime;

    @Before
    public void setUp() throws Exception {
        datastore = new MarketoDatastoreProperties("test");
        datastore.init();
        datastore.setupProperties();
        datastore.endpoint.setValue(ENDPOINT_REST);
        datastore.clientAccessId.setValue(USERID_REST);
        datastore.secretKey.setValue(SECRETKEY_REST);
        runtime = new MarketoDatastoreRuntime();
    }

    @Test
    public void testInitialize() throws Exception {
        ValidationResult vr = runtime.initialize(null, datastore);
        assertEquals(Result.OK, vr.getStatus());
    }

    @Test
    public void testDoHealthCheck() throws Exception {
        runtime.initialize(null, datastore);
        Iterable<ValidationResult> results = runtime.doHealthChecks(null);
        for (ValidationResult r : results) {
            assertEquals(Result.OK, r.getStatus());
        }
    }
}
