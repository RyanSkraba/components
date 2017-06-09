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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.azurestorage.queue.AzureStorageQueueProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.service.Repository;

public class AzureStorageComponentListPropertiesTest extends AzureStorageConnectionWizardTest {

    private AzureStorageComponentListProperties properties;

    private final List<RepoProps> repoProps = new ArrayList<>();

    private Repository<Properties> repo = new TestRepository(repoProps);

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
        assertNotNull("result cannot be null", result.getConnectionProperties());
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
    @Test
    public void testBeforeFormPresentContainer() throws Exception {
        properties.setupLayout();
        properties.beforeFormPresentContainer();
        assertTrue("Shoud be ture",properties.getForm(AzureStorageComponentListProperties.FORM_CONTAINER).isAllowBack()&&
        properties.getForm(AzureStorageComponentListProperties.FORM_CONTAINER).isAllowForward()&&
        properties.getForm(AzureStorageComponentListProperties.FORM_CONTAINER).isAllowFinish());
    }

    /**
     *
     * @see org.talend.components.azurestorage.wizard.AzureStorageComponentListProperties#beforeFormPresentQueue()
     */
    @Test
    public void testBeforeFormPresentQueue() throws Exception {
        properties.setupLayout();
        properties.beforeFormPresentQueue();
        assertTrue("Shoud be ture",properties.getForm(AzureStorageComponentListProperties.FORM_QUEUE).isAllowBack()&&
        properties.getForm(AzureStorageComponentListProperties.FORM_QUEUE).isAllowForward()&&
        properties.getForm(AzureStorageComponentListProperties.FORM_QUEUE).isAllowFinish());
    }

    /**
     *
     * @see org.talend.components.azurestorage.wizard.AzureStorageComponentListProperties#beforeFormPresentTable()
     */
    @Test
    public void testBeforeFormPresentTable() throws Exception {
        properties.setupLayout();
        properties.beforeFormPresentTable();
        assertTrue("Shoud be ture",properties.getForm(AzureStorageComponentListProperties.FORM_TABLE).isAllowBack()&&
        properties.getForm(AzureStorageComponentListProperties.FORM_TABLE).isAllowFinish());
    }

    /**
     *
     * @see org.talend.components.azurestorage.wizard.AzureStorageComponentListProperties#afterFormFinishTable(Repository<Properties>)
     */
    @Test
    public void afterFormFinishTable() throws Exception {
        List<NamedThing> nameWithNumeric = new ArrayList<NamedThing>();
        nameWithNumeric.add(new SimpleNamedThing("2Name_with_numeric2", "2Name_with_numeric2"));
        properties.selectedContainerNames.setStoredValue(nameWithNumeric);
        properties.selectedQueueNames.setStoredValue(nameWithNumeric);
        ValidationResult result = properties.afterFormFinishTable(repo);
        assertEquals(Result.OK, result.getStatus());
    }
    
    @Test(expected = NoSuchElementException.class)
    public void afterFormFinishTable2() throws Exception {
        TAzureStorageConnectionProperties connection = new TAzureStorageConnectionProperties("test");
        connection.setupProperties();
        connection.setupLayout();
        connection.useSharedAccessSignature.setValue(true);
        connection.sharedAccessSignature.setValue("https://talendrd.blob.core.windows.net/?sv=2016-05-31&ss=f&srt=sco&sp=rwdlacup&se=2017-06-07T23:50:05Z&st=2017-05-24T15:50:05Z&spr=https&sig=fakeSASfakeSASfakeSASfakeSASfakeSASfakeSASfakeSASfakeSAS");
        properties.setConnection(connection);
        
        List<NamedThing> nameWithNumeric = new ArrayList<NamedThing>();
        nameWithNumeric.add(new SimpleNamedThing("2Name_with_numeric2", "2Name_with_numeric2"));
        properties.selectedTableNames.setStoredValue(nameWithNumeric);
        @SuppressWarnings("unused")
        ValidationResult result = properties.afterFormFinishTable(repo);
    }

}
