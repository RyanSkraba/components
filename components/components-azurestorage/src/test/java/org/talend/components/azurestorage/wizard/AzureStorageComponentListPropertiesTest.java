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
package org.talend.components.azurestorage.wizard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.azurestorage.queue.AzureStorageQueueProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.service.Repository;

public class AzureStorageComponentListPropertiesTest extends AzureStorageConnectionWizardTest {

    private AzureStorageComponentListProperties properties;

    private final List<RepoProps> repoProps = new ArrayList<>();

    private Repository repo = new TestRepository(repoProps);

    Schema schemaQueue = SchemaBuilder.builder().record("Main").fields()//
            .name(AzureStorageQueueProperties.FIELD_MESSAGE_ID).prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true")
            .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "100").type(AvroUtils._string()).noDefault()//
            .name(AzureStorageQueueProperties.FIELD_MESSAGE_CONTENT).type(AvroUtils._string()).noDefault() //
            .name(AzureStorageQueueProperties.FIELD_INSERTION_TIME)
            .prop(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd hh:mm:ss").type(AvroUtils._date()).noDefault() //
            .name(AzureStorageQueueProperties.FIELD_EXPIRATION_TIME)
            .prop(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd hh:mm:ss").type(AvroUtils._date()).noDefault() //
            .name(AzureStorageQueueProperties.FIELD_NEXT_VISIBLE_TIME)
            .prop(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd hh:mm:ss").type(AvroUtils._date()).noDefault() //
            .name(AzureStorageQueueProperties.FIELD_DEQUEUE_COUNT).type(AvroUtils._int()).noDefault() //
            .name(AzureStorageQueueProperties.FIELD_POP_RECEIPT).type(AvroUtils._string()).noDefault() //
            .endRecord();

    Schema schemaContainer = SchemaBuilder.builder().record("Main").fields()//
            .name("containerName").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true")
            .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "100").type(AvroUtils._string()).noDefault()//
            .endRecord();

    @Before
    public void setUp() throws Exception {
        properties = new AzureStorageComponentListProperties("test");
        // final List<RepoProps> repoProps = new ArrayList<>();
        // Repository repo = new TestRepository(repoProps);
        // properties.setConnection(null);
    }

    /**
     *
     * @see org.talend.components.azurestorage.wizard.AzureStorageComponentListProperties#getContainerSchema()
     */
    @Test
    public void getContainerSchema() {
        Schema containerschema = properties.getContainerSchema();
        assertNotNull("containerschema cannot be null", containerschema);
    }

    /**
     *
     * @see org.talend.components.azurestorage.wizard.AzureStorageComponentListProperties#setConnection(TAzureStorageConnectionProperties)
     */
    @Test
    public void testSetConnection() {
        TAzureStorageConnectionProperties connection = new TAzureStorageConnectionProperties(null);
        AzureStorageComponentListProperties result = properties.setConnection(connection);
        assertNotNull("result cannot be null", result);
    }

    /**
     *
     * @see org.talend.components.azurestorage.wizard.AzureStorageComponentListProperties#getQueueSchema()
     */
    @Test
    public void testGetQueueSchema() {
        Schema queueschema = properties.getQueueSchema();
        assertNotNull("queueschema cannot be null", queueschema);
        assertEquals(schemaQueue, queueschema);
    }

    /**
     *
     * @see org.talend.components.azurestorage.wizard.AzureStorageComponentListProperties#getContainerSchema()
     */
    @Test
    public void testGetContainerSchema() {
        Schema containerschema = properties.getContainerSchema();
        assertNotNull("containerschema cannot be null", containerschema);
        assertEquals(schemaContainer, containerschema);
    }

    /**
     *
     * @see org.talend.components.azurestorage.wizard.AzureStorageComponentListProperties#getConnectionProperties()
     */
    @Test
    public void testGetConnectionProperties() {
        assertNotNull(properties.getConnectionProperties());
        properties.setConnection(null);
        assertNull(properties.getConnectionProperties());
    }

    /**
     *
     * @see org.talend.components.azurestorage.wizard.AzureStorageComponentListProperties#beforeFormPresentContainer()
     */
    @Test(expected = Exception.class)
    public void testBeforeFormPresentContainer() throws Exception {
        properties.beforeFormPresentContainer();
        fail("Should have failed...");
    }

    /**
     *
     * @see org.talend.components.azurestorage.wizard.AzureStorageComponentListProperties#beforeFormPresentQueue()
     */
    @Test(expected = Exception.class)
    public void testBeforeFormPresentQueue() throws Exception {
        properties.beforeFormPresentQueue();
        fail("Should have failed...");
    }

    /**
     *
     * @see org.talend.components.azurestorage.wizard.AzureStorageComponentListProperties#beforeFormPresentTable()
     */
    @Test(expected = Exception.class)
    public void testBeforeFormPresentTable() throws Exception {
        properties.beforeFormPresentTable();
        fail("Should have failed...");
    }

    /**
     *
     * @see org.talend.components.azurestorage.wizard.AzureStorageComponentListProperties#afterFormFinishTable(Repository<Properties>)
     */
    @Test
    public void afterFormFinishTable() throws Exception {
        ValidationResult result = properties.afterFormFinishTable(repo);
        assertNotNull("result cannot be null", result);
    }

}
