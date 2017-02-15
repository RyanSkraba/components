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
package org.talend.components.azurestorage.blob.runtime;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.azurestorage.AzureStorageBaseTestIT;
import org.talend.components.azurestorage.blob.runtime.AzureStorageSourceOrSink;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties;

/**
 *
 */
public class AzureStorageSourceOrSinkTestIT extends AzureStorageBaseTestIT {

    public AzureStorageSourceOrSinkTestIT() {
        super("source-or-sink");
    }

    @Before
    public void setUp() {

    }

    @Test(expected = Exception.class)
    public void testInvalidConnection() throws Throwable {
        // fail("Not yet implemented");
        AzureStorageSourceOrSink sos = new AzureStorageSourceOrSink();
        TAzureStorageConnectionProperties properties = new TAzureStorageConnectionProperties("tests");
        properties.setupProperties();
        sos.initialize(runtime, properties);
        sos.getServiceClient(runtime);
    }
}
