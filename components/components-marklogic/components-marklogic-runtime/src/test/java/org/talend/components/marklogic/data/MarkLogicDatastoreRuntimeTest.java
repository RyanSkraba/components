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

package org.talend.components.marklogic.data;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.marklogic.connection.MarkLogicConnection;
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionProperties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

import com.marklogic.client.DatabaseClient;

public class MarkLogicDatastoreRuntimeTest {

    private MarkLogicDatastoreRuntime datastoreRuntime;

    @Before
    public void setup() {
        datastoreRuntime = new MarkLogicDatastoreRuntime();

        MarkLogicConnectionProperties datastore = new MarkLogicConnectionProperties("datastore");
        datastore.referencedComponent.componentInstanceId.setValue("reference");
        datastoreRuntime.initialize(null, datastore);
    }

    @Test
    public void testDoHealthChecks() {

        DatabaseClient client = Mockito.mock(DatabaseClient.class);
        RuntimeContainer container = Mockito.mock(RuntimeContainer.class);
        Mockito.when(container.getComponentData("reference", MarkLogicConnection.CONNECTION)).thenReturn(client);

        List<ValidationResult> healthChecks = (List<ValidationResult>) datastoreRuntime.doHealthChecks(container);
        Assert.assertEquals(1, healthChecks.size());
        Assert.assertEquals(ValidationResult.OK, healthChecks.get(0));
    }

    @Test
    public void testDoHealthChecksFailed() {

        RuntimeContainer container = Mockito.mock(RuntimeContainer.class);
        Mockito.when(container.getComponentData("reference", MarkLogicConnection.CONNECTION)).thenReturn(null);

        List<ValidationResult> healthChecks = (List<ValidationResult>) datastoreRuntime.doHealthChecks(container);
        Assert.assertEquals(1, healthChecks.size());
        Assert.assertEquals(Result.ERROR, healthChecks.get(0).getStatus());
    }
}
