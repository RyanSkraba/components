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
package org.talend.components.azurestorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.EnumSet;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.azurestorage.blob.AzureStorageContainerProperties;
import org.talend.components.azurestorage.blob.runtime.AzureStorageContainerRuntime;
import org.talend.components.azurestorage.blob.tazurestoragecontainerlist.TAzureStorageContainerListProperties;
import org.talend.components.azurestorage.blob.tazurestoragelist.TAzureStorageListProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionDefinition;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.ValidationResult;

public class AzureStorageComponentsTest {// extends AzureStorageGenericBase {

    protected RuntimeContainer runtime;

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    @Test
    public void testFamily() {
        TAzureStorageConnectionDefinition aconn = new TAzureStorageConnectionDefinition();
        assertEquals(1, aconn.getFamilies().length);
        assertEquals("Cloud/Azure Storage", aconn.getFamilies()[0]);
    }

    public void checkContainer(AzureStorageContainerProperties props, String mycontainer) {
        props.container.setValue(mycontainer);
        assertEquals(mycontainer, props.container.getValue());
        assertTrue(true);
    }

    /**
     * Check the container's validation chain
     */
    public ValidationResult getContainerValidation(String container, AzureStorageContainerProperties properties) {
        properties.container.setValue(container);
        AzureStorageContainerRuntime sos = new AzureStorageContainerRuntime();
        return sos.initialize(runtime, properties);
    }


    @Test
    public void testBlobListSchema() {
        Schema s = SchemaBuilder.record("Main").fields().name("BlobName").prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "300")// $NON-NLS-3$
                .prop(SchemaConstants.TALEND_IS_LOCKED, "true").type(AvroUtils._string()).noDefault().endRecord();
        TAzureStorageListProperties props = new TAzureStorageListProperties("tests");
        props.setupProperties();
        Schema ps = props.schema.schema.getValue();
        assertEquals(s, ps);
    }

    @Test
    public void testContainerListSchema() {
        Schema s = SchemaBuilder.record("Main").fields().name("ContainerName").prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "50")// $NON-NLS-3$
                .prop(SchemaConstants.TALEND_IS_LOCKED, "true").type(AvroUtils._string()).noDefault().endRecord();
        TAzureStorageContainerListProperties props = new TAzureStorageContainerListProperties("tests");
        props.setupProperties();
        Schema ps = props.schema.schema.getValue();
        assertEquals(s, ps);
    }

    @Test
    public void testAzureStorageDefinition() {
        AzureStorageDefinition def = new TAzureStorageConnectionDefinition();
        assertEquals(EnumSet.of(ConnectorTopology.NONE), def.getSupportedConnectorTopologies());
        assertFalse(def.isStartable());
    }

    @Test
    public void testAzureStorageBaseProperties() {
        AzureStorageProperties p = new TAzureStorageContainerListProperties("test");
        assertEquals(Collections.emptySet(), p.getAllSchemaPropertiesConnectors(false));
        assertEquals(Collections.singleton(new PropertyPathConnector(Connector.MAIN_NAME, "schema")),
                p.getAllSchemaPropertiesConnectors(true));
    }

}
