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
package org.talend.components.snowflake;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.talend.components.snowflake.SnowflakeTestBase.MockRuntimeSourceOrSinkTestFixture;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.service.Repository;

/**
 * Unit Tests for {@link SnowflakeTableListProperties} class
 */
@RunWith(PowerMockRunner.class)
public class SnowflakeTableListPropertiesTest {

    @Mock
    private Repository<Properties> repo;

    private SnowflakeTableListProperties properties = new SnowflakeTableListProperties("table");

    @Test
    public void testSetAndGetConnectionProperties() {
        SnowflakeConnectionProperties connectionProperties = new SnowflakeConnectionProperties("connectionProperty");
        properties.setConnection(connectionProperties);
        Assert.assertEquals(connectionProperties, properties.getConnectionProperties());
    }

    @Test
    public void testSetupLayout() {
        Assert.assertNull(properties.getForm(Form.MAIN));
        properties.setupLayout();
        Assert.assertNotNull(properties.getForm(Form.MAIN));
        Assert.assertNotNull(properties.getForm(Form.MAIN).getWidget(properties.selectedTableNames.getName()));
    }

    @Test
    public void testBeforeFormPresentMain() throws Exception {
        List<NamedThing> tableNames = new ArrayList<>();
        tableNames.add(new SimpleNamedThing());

        try (MockRuntimeSourceOrSinkTestFixture testFixture = new MockRuntimeSourceOrSinkTestFixture()) {
            testFixture.setUp();

            Mockito.when(testFixture.runtimeSourceOrSink.getSchemaNames(null)).thenReturn(tableNames);

            properties.setupLayout();
            properties.beforeFormPresentMain();
            Assert.assertTrue(properties.getForm(Form.MAIN).isAllowBack());
            Assert.assertTrue(properties.getForm(Form.MAIN).isAllowFinish());
            Assert.assertEquals(tableNames, properties.selectedTableNames.getPossibleValues());
        }
    }

    @Test
    public void testAfterFormFinishMain() throws Exception {
        String tableId = "tableId";
        properties.setRepositoryLocation("repo");
        properties.setConnection(new SnowflakeConnectionProperties("connection"));
        properties.selectedTableNames.setValue(Arrays.asList(new NamedThing[] { new SimpleNamedThing(tableId) }));

        try (MockRuntimeSourceOrSinkTestFixture testFixture = new MockRuntimeSourceOrSinkTestFixture()) {
            testFixture.setUp();

            Mockito.when(testFixture.runtimeSourceOrSink.validateConnection(properties)).thenReturn(ValidationResult.OK);
            Schema schema = SchemaBuilder.record("record").fields().requiredString("id").endRecord();
            Mockito.when(testFixture.runtimeSourceOrSink.getEndpointSchema(null, tableId)).thenReturn(schema);
            Mockito.when(repo.storeProperties(Mockito.any(Properties.class), Mockito.anyString(), Mockito.anyString(),
                    Mockito.anyString())).thenReturn("connRepoLocation");

            Assert.assertEquals(ValidationResult.OK, properties.afterFormFinishMain(repo));
            Mockito.verify(repo, Mockito.times(2)).storeProperties(Mockito.any(Properties.class), Mockito.anyString(),
                    Mockito.anyString(), Mockito.anyString());
        }
    }

    @Test
    public void testFailureConnection() throws Exception {
        try (MockRuntimeSourceOrSinkTestFixture testFixture = new MockRuntimeSourceOrSinkTestFixture()) {
            testFixture.setUp();

            Mockito.when(testFixture.runtimeSourceOrSink.validateConnection(properties)).thenReturn(new ValidationResult(Result.ERROR));
            Assert.assertEquals(Result.ERROR, properties.afterFormFinishMain(repo).getStatus());
        }
    }

}
