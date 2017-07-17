// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.snowflake.test;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.talend.daikon.properties.presentation.Form.MAIN;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.container.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.snowflake.SnowflakeConnectionProperties;
import org.talend.components.snowflake.SnowflakeTableProperties;
import org.talend.components.snowflake.runtime.SnowflakeCloseSourceOrSink;
import org.talend.components.snowflake.runtime.SnowflakeSourceOrSink;
import org.talend.components.snowflake.tsnowflakeclose.TSnowflakeCloseDefinition;
import org.talend.components.snowflake.tsnowflakeclose.TSnowflakeCloseProperties;
import org.talend.components.snowflake.tsnowflakeconnection.TSnowflakeConnectionDefinition;
import org.talend.components.snowflake.tsnowflakeinput.TSnowflakeInputDefinition;
import org.talend.components.snowflake.tsnowflakeinput.TSnowflakeInputProperties;
import org.talend.components.snowflake.tsnowflakeoutput.TSnowflakeOutputDefinition;
import org.talend.components.snowflake.tsnowflakeoutput.TSnowflakeOutputProperties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.test.PropertiesTestUtils;

/**
 * created by dmytro.chmyga on Mar 6, 2017
 */
public class SnowflakeReadersTestIT extends SnowflakeRuntimeIOTestIT {

    private static boolean setUp = false;

    @Before
    public void prepareData() throws Throwable {
        if (!setUp) {
            populateOutput(100);
            setUp = true;
        }
        resetUser();
    }

    @Test
    public void testInputCondition() throws Throwable {
        TSnowflakeInputProperties props = (TSnowflakeInputProperties) new TSnowflakeInputDefinition().createProperties();
        setupProps(props.getConnectionProperties());
        Form f = props.getForm(MAIN);
        props.manualQuery.setValue(false);
        props = (TSnowflakeInputProperties) PropertiesTestUtils.checkAndAfter(getComponentService(), f,
                props.manualQuery.getName(), props);

        props.condition.setValue("ID > 80");
        setupTableWithStaticValues(props);
        List<IndexedRecord> rows = readRows(props);
        assertEquals(19, rows.size());
        assertEquals(BigDecimal.valueOf(81), rows.get(0).get(0));
        assertEquals("foo_81", rows.get(0).get(1));
    }

    @Test
    public void testInputManual() throws Throwable {
        TSnowflakeInputProperties props = (TSnowflakeInputProperties) new TSnowflakeInputDefinition().createProperties();
        setupProps(props.getConnectionProperties());
        Form f = props.getForm(MAIN);
        props.manualQuery.setValue(true);
        props = (TSnowflakeInputProperties) PropertiesTestUtils.checkAndAfter(getComponentService(), f,
                props.manualQuery.getName(), props);

        props.query.setValue("select ID, C7 from " + testTable + " where ID > 80");
        setupTableWithStaticValues(props);
        List<IndexedRecord> rows = readRows(props);
        assertEquals(19, rows.size());
        Schema schema = rows.get(0).getSchema();
        LOGGER.debug(schema.toString());
        assertEquals(BigDecimal.valueOf(81), rows.get(0).get(0));
        assertThat((String) rows.get(0).get(1), containsString("\"bar\": 81"));
    }

    @Test(expected = IOException.class)
    public void testSameConnectionForSeveralReaders() throws Throwable {
        SnowflakeConnectionProperties connProps = (SnowflakeConnectionProperties) getComponentService()
                .getComponentProperties(TSnowflakeConnectionDefinition.COMPONENT_NAME);
        setupProps(connProps);

        final String currentComponentName = TSnowflakeConnectionDefinition.COMPONENT_NAME + "_1";
        RuntimeContainer connContainer = new DefaultComponentRuntimeContainerImpl() {

            @Override
            public String getCurrentComponentId() {
                return currentComponentName;
            }
        };

        SnowflakeSourceOrSink SnowflakeSourceOrSink = new SnowflakeSourceOrSink();
        SnowflakeSourceOrSink.initialize(connContainer, connProps);
        assertEquals(ValidationResult.Result.OK, SnowflakeSourceOrSink.validate(connContainer).getStatus());

        TSnowflakeOutputProperties props = (TSnowflakeOutputProperties) getComponentService()
                .getComponentProperties(TSnowflakeOutputDefinition.COMPONENT_NAME);
        setupProps(props.getConnectionProperties());
        setupTableWithStaticValues(props);

        props.connection.referencedComponent.componentInstanceId.setValue(currentComponentName);

        List<IndexedRecord> rows = readRows(props, connContainer);
        assertEquals(100, rows.size());

        // Read second time with the same properties but with new reader.
        rows = readRows(props, connContainer);
        assertEquals(100, rows.size());

        TSnowflakeCloseProperties closeProps = (TSnowflakeCloseProperties) getComponentService()
                .getComponentProperties(TSnowflakeCloseDefinition.COMPONENT_NAME);
        closeProps.referencedComponent.componentInstanceId.setValue(currentComponentName);

        SnowflakeCloseSourceOrSink snowflakeCloseSourceOrSink = new SnowflakeCloseSourceOrSink();
        snowflakeCloseSourceOrSink.initialize(connContainer, closeProps);
        assertEquals(ValidationResult.Result.OK, snowflakeCloseSourceOrSink.validate(connContainer).getStatus());

        // After close, exception should be thrown by the reader, if we try to read with the same connection.
        rows = readRows(props, connContainer);
    }

    @Test
    public void testCloseExistingConnection() throws Throwable {
        SnowflakeConnectionProperties connProps = (SnowflakeConnectionProperties) getComponentService()
                .getComponentProperties(TSnowflakeConnectionDefinition.COMPONENT_NAME);
        setupProps(connProps);

        final String currentComponentName = TSnowflakeConnectionDefinition.COMPONENT_NAME + "_1";
        RuntimeContainer connContainer = new DefaultComponentRuntimeContainerImpl() {

            @Override
            public String getCurrentComponentId() {
                return currentComponentName;
            }
        };

        SnowflakeSourceOrSink snowflakeSourceOrSink = new SnowflakeSourceOrSink();
        snowflakeSourceOrSink.initialize(connContainer, connProps);
        assertEquals(ValidationResult.Result.OK, snowflakeSourceOrSink.validate(connContainer).getStatus());

        TSnowflakeCloseProperties closeProps = (TSnowflakeCloseProperties) getComponentService()
                .getComponentProperties(TSnowflakeCloseDefinition.COMPONENT_NAME);
        closeProps.referencedComponent.componentInstanceId.setValue(currentComponentName);

        SnowflakeCloseSourceOrSink snowflakeCloseSourceOrSink = new SnowflakeCloseSourceOrSink();
        snowflakeCloseSourceOrSink.initialize(connContainer, closeProps);
        assertEquals(ValidationResult.Result.OK, snowflakeCloseSourceOrSink.validate(connContainer).getStatus());
    }

    @Test
    public void testUseExistingConnection() throws Throwable {
        SnowflakeConnectionProperties connProps = (SnowflakeConnectionProperties) getComponentService()
                .getComponentProperties(TSnowflakeConnectionDefinition.COMPONENT_NAME);
        setupProps(connProps);

        final String currentComponentName = TSnowflakeConnectionDefinition.COMPONENT_NAME + "_1";
        RuntimeContainer connContainer = new DefaultComponentRuntimeContainerImpl() {

            @Override
            public String getCurrentComponentId() {
                return currentComponentName;
            }
        };

        SnowflakeSourceOrSink SnowflakeSourceOrSink = new SnowflakeSourceOrSink();
        SnowflakeSourceOrSink.initialize(connContainer, connProps);
        assertEquals(ValidationResult.Result.OK, SnowflakeSourceOrSink.validate(connContainer).getStatus());

        // Input component get connection from the tSnowflakeConnection
        TSnowflakeInputProperties inProps = (TSnowflakeInputProperties) getComponentService()
                .getComponentProperties(TSnowflakeInputDefinition.COMPONENT_NAME);
        inProps.connection.referencedComponent.componentInstanceId.setValue(currentComponentName);

        SnowflakeSourceOrSink SnowflakeInputSourceOrSink = new SnowflakeSourceOrSink();
        SnowflakeInputSourceOrSink.initialize(connContainer, inProps);
        assertEquals(ValidationResult.Result.OK, SnowflakeInputSourceOrSink.validate(connContainer).getStatus());
    }

    @Test
    public void testInputConnectionRef() throws Throwable {
        TSnowflakeInputProperties props = (TSnowflakeInputProperties) getComponentService()
                .getComponentProperties(TSnowflakeInputDefinition.COMPONENT_NAME);
        setupProps(props.connection);

        SnowflakeSourceOrSink SnowflakeSourceOrSink = new SnowflakeSourceOrSink();
        SnowflakeSourceOrSink.initialize(null, props);
        assertEquals(ValidationResult.Result.OK, SnowflakeSourceOrSink.validate(null).getStatus());

        // Referenced properties simulating Snowflake connect component
        SnowflakeConnectionProperties cProps = (SnowflakeConnectionProperties) getComponentService()
                .getComponentProperties(TSnowflakeConnectionDefinition.COMPONENT_NAME);
        setupProps(cProps);
        cProps.userPassword.password.setValue("xxx");

        String compId = "comp1";
        // Use the connection props of the Snowflake connect component
        props.connection.referencedComponent.referenceType
                .setValue(ComponentReferenceProperties.ReferenceType.COMPONENT_INSTANCE);
        props.connection.referencedComponent.componentInstanceId.setValue(compId);
        props.connection.referencedComponent.setReference(cProps);
        PropertiesTestUtils.checkAndAfter(getComponentService(), props.connection.getForm(Form.REFERENCE), "referencedComponent",
                props.connection);

        resetUser();

        SnowflakeSourceOrSink = new SnowflakeSourceOrSink();
        SnowflakeSourceOrSink.initialize(null, props);
        SnowflakeSourceOrSink.validate(null);
        assertEquals(ValidationResult.Result.ERROR, SnowflakeSourceOrSink.validate(null).getStatus());

        resetUser();
        setupProps(cProps);

        setupTableWithStaticValues(props);

        // Back to using the connection props of the Snowflake input component
        props.connection.referencedComponent.referenceType.setValue(ComponentReferenceProperties.ReferenceType.THIS_COMPONENT);
        props.connection.referencedComponent.componentInstanceId.setValue(null);
        props.connection.referencedComponent.setReference(null);
        // Check that the null referenced component works.
        PropertiesTestUtils.checkAndAfter(getComponentService(), props.connection.getForm(Form.REFERENCE), "referencedComponent",
                props.connection);

        resetUser();

        SnowflakeSourceOrSink = new SnowflakeSourceOrSink();
        SnowflakeSourceOrSink.initialize(null, props);
        ValidationResult result = SnowflakeSourceOrSink.validate(null);
        LOGGER.info(String.valueOf(result));
        assertEquals(ValidationResult.Result.OK, result.getStatus());
    }

    @Test
    public void testInputSchema() throws Throwable {
        TSnowflakeInputProperties props = (TSnowflakeInputProperties) getComponentService()
                .getComponentProperties(TSnowflakeInputDefinition.COMPONENT_NAME);
        setupProps(props.connection);

        Form f = props.table.getForm(Form.REFERENCE);
        SnowflakeTableProperties moduleProps = (SnowflakeTableProperties) f.getProperties();
        moduleProps = (SnowflakeTableProperties) PropertiesTestUtils.checkAndBeforeActivate(getComponentService(), f,
                moduleProps.tableName.getName(), moduleProps);
        moduleProps.tableName.setValue(testTable);
        moduleProps = (SnowflakeTableProperties) PropertiesTestUtils.checkAndAfter(getComponentService(), f,
                moduleProps.tableName.getName(), moduleProps);
        Schema schema = moduleProps.main.schema.getValue();
        LOGGER.debug(schema.toString());
        for (Schema.Field child : schema.getFields()) {
            LOGGER.debug(child.name());
        }
        assertEquals("ID", schema.getFields().get(0).name());
        LOGGER.debug("Table \"" + testTable + "\" column size:" + schema.getFields().size());
        assertTrue(schema.getFields().size() == NUM_COLUMNS);
    }

    @Test
    public void testTableNamesInput() throws Throwable {
        TSnowflakeInputProperties props = (TSnowflakeInputProperties) getComponentService()
                .getComponentProperties(TSnowflakeInputDefinition.COMPONENT_NAME);
        setupProps(props.getConnectionProperties());
        ComponentTestUtils.checkSerialize(props, errorCollector);
        checkAndSetupTable(props);
    }

    @Test(expected = IOException.class)
    public void testInputManualError() throws Throwable {
        TSnowflakeInputProperties props = (TSnowflakeInputProperties) new TSnowflakeInputDefinition().createProperties();
        setupProps(props.getConnectionProperties());
        Form f = props.getForm(MAIN);
        props.manualQuery.setValue(true);
        props = (TSnowflakeInputProperties) PropertiesTestUtils.checkAndAfter(getComponentService(), f,
                props.manualQuery.getName(), props);

        props.query.setValue("bad query");
        readRows(props);
    }

}
