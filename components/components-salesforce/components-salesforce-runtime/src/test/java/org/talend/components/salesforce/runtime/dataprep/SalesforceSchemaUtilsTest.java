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

package org.talend.components.salesforce.runtime.dataprep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.util.Arrays;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.salesforce.dataset.SalesforceDatasetProperties;

public class SalesforceSchemaUtilsTest {

    private static final Schema SCHEMA_1 = SchemaBuilder.record("Account").fields()
            .name("Id").type().stringType().noDefault()
            .name("Name").type().stringType().noDefault()
            .name("AccountNumber").type().stringType().noDefault()
            .name("BillingCity").type().stringType().noDefault()
            .endRecord();

    private SalesforceDatasetProperties datasetProperties;

    private SalesforceDataprepSource dataprepSource;

    @Before
    public void setUp() {
        datasetProperties = new SalesforceDatasetProperties("dataset");
        datasetProperties.init();

        dataprepSource = spy(new SalesforceDataprepSource());
    }

    @Test
    public void testGetSchemaModuleSelection() throws IOException {
        datasetProperties.sourceType.setValue(SalesforceDatasetProperties.SourceType.MODULE_SELECTION);
        datasetProperties.moduleName.setValue("Account");
        datasetProperties.selectColumnIds.setValue(Arrays.asList(
                "Id", "Name", "AccountNumber", "BillingCity"));

        doReturn(SCHEMA_1).when(dataprepSource).guessSchema(anyString());

        Schema schema = SalesforceSchemaUtils.getSchema(datasetProperties, dataprepSource, null);
        assertNotNull(schema);
        assertEquals(SCHEMA_1, schema);
    }

    @Test(expected = RuntimeException.class)
    public void testGetSchemaModuleSelectionNoFields() throws IOException {
        datasetProperties.sourceType.setValue(SalesforceDatasetProperties.SourceType.MODULE_SELECTION);
        datasetProperties.moduleName.setValue("Account");

        SalesforceSchemaUtils.getSchema(datasetProperties, dataprepSource, null);
    }

    @Test(expected = RuntimeException.class)
    public void testGetSchemaModuleSelectionError() throws IOException {
        datasetProperties.sourceType.setValue(SalesforceDatasetProperties.SourceType.MODULE_SELECTION);
        datasetProperties.moduleName.setValue("Account");
        datasetProperties.selectColumnIds.setValue(Arrays.asList(
                "Id", "Name", "AccountNumber", "BillingCity"));

        doThrow(new IOException("Connection error")).when(dataprepSource).guessSchema(anyString());

        SalesforceSchemaUtils.getSchema(datasetProperties, dataprepSource, null);
    }

    @Test
    public void testGetSchemaQuery() throws IOException {
        datasetProperties.sourceType.setValue(SalesforceDatasetProperties.SourceType.SOQL_QUERY);
        String query = "select Id, Name, AccountNumber, BillingCity from Account";
        datasetProperties.query.setValue(query);

        doReturn(SCHEMA_1).when(dataprepSource).guessSchema(eq(query));

        Schema schema = SalesforceSchemaUtils.getSchema(datasetProperties, dataprepSource, null);
        assertNotNull(schema);
        assertEquals(SCHEMA_1, schema);
    }
}
