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

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.marklogic.connection.MarkLogicConnection;
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionProperties;
import org.talend.daikon.properties.ValidationResult.Result;

import com.marklogic.client.DatabaseClient;

public class MarkLogicDataSourceTest {

    private MarkLogicDataSource datasource;

    private MarkLogicConnectionProperties datastore;

    @Before
    public void setup() {
        datasource = new MarkLogicDataSource();

        datastore = new MarkLogicConnectionProperties("datastore");
        datastore.init();
        MarkLogicDatasetProperties dataset = new MarkLogicDatasetProperties("dataset");
        dataset.setDatastoreProperties(datastore);
        dataset.init();
        datasource.initialize(null, dataset);
    }

    @Test
    public void testGetScemaName() throws IOException {
        Assert.assertTrue(datasource.getSchemaNames(null).isEmpty());
    }

    @Test
    public void testValidate() {
        datasource.getMarkLogicConnectionProperties().referencedComponent.componentInstanceId.setValue("reference");
        DatabaseClient client = Mockito.mock(DatabaseClient.class);
        RuntimeContainer container = Mockito.mock(RuntimeContainer.class);
        Mockito.when(container.getComponentData("reference", MarkLogicConnection.CONNECTION)).thenReturn(client);

        Assert.assertEquals(Result.OK, datasource.validate(container).getStatus());
        Mockito.verify(container, Mockito.only()).getComponentData("reference", MarkLogicConnection.CONNECTION);
    }

    @Test
    public void testValidateFailed() {
        datasource.getMarkLogicConnectionProperties().referencedComponent.componentInstanceId.setValue("reference");
        RuntimeContainer container = Mockito.mock(RuntimeContainer.class);
        Mockito.when(container.getComponentData("reference", MarkLogicConnection.CONNECTION)).thenReturn(null);

        Assert.assertEquals(Result.ERROR, datasource.validate(container).getStatus());
        Mockito.verify(container, Mockito.only()).getComponentData("reference", MarkLogicConnection.CONNECTION);
    }

    @Test
    public void testGetEndpointSchema() throws IOException {
        Assert.assertNull(datasource.getEndpointSchema(null, null));
    }

    @Test
    public void testInvalidPropertiesInitialization() {
        Assert.assertEquals(Result.ERROR,
                datasource.initialize(null, new MarkLogicConnectionProperties("connection")).getStatus());
    }

    @Test
    public void testSplitIntoBundles() throws Exception {
        List<? extends BoundedSource> boundedSources = datasource.splitIntoBundles(0, null);
        Assert.assertEquals(1, boundedSources.size());
        Assert.assertEquals(datasource, boundedSources.get(0));
    }

    @Test
    public void testGetEstimatedSizeBytes() {
        Assert.assertEquals(0, datasource.getEstimatedSizeBytes(null));
    }

    @Test
    public void testProducesSortedKeys() {
        Assert.assertFalse(datasource.producesSortedKeys(null));
    }

    @Test
    public void testCreateReader() {
        Assert.assertTrue(datasource.createReader(null) instanceof MarkLogicInputReader);
    }

    @Test
    public void testGetMarkLogicConnectionProperties() {
        Assert.assertEquals(datastore, datasource.getMarkLogicConnectionProperties());
    }

}
