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
package org.talend.components.azurestorage.table.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.azurestorage.RuntimeContainerMock;
import org.talend.components.azurestorage.table.AzureStorageTableService;
import org.talend.components.azurestorage.table.TableHelper;
import org.talend.components.azurestorage.table.tazurestorageoutputtable.TAzureStorageOutputTableProperties;
import org.talend.components.azurestorage.table.tazurestorageoutputtable.TAzureStorageOutputTableProperties.ActionOnData;
import org.talend.components.azurestorage.table.tazurestorageoutputtable.TAzureStorageOutputTableProperties.ActionOnTable;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties.Protocol;
import org.talend.daikon.properties.ValidationResult;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.TableOperation;
import com.microsoft.azure.storage.table.TableResult;

public class AzureStorageTableWriterTest {

    private AzureStorageTableWriter writer;

    private TAzureStorageOutputTableProperties properties;

    public static final String PROP_ = "PROP_";

    private RuntimeContainer container;

    private AzureStorageTableSink sink;

    @Mock
    private AzureStorageTableService tableService;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setUp() throws Exception {

        container = new RuntimeContainerMock();
        sink = new AzureStorageTableSink();

        properties = new TAzureStorageOutputTableProperties(PROP_ + "InputTable");
        properties.setupProperties();
        // valid fake connection
        properties.connection = new TAzureStorageConnectionProperties(PROP_ + "Connection");
        properties.connection.protocol.setValue(Protocol.HTTP);
        properties.connection.accountName.setValue("fakeAccountName");
        properties.connection.accountKey.setValue("fakeAccountKey=ANBHFYRJJFHRIKKJFU");

        properties.dieOnError.setValue(false);
        properties.tableName.setValue(TableHelper.generateRandomTableName());
        properties.actionOnTable.setValue(ActionOnTable.Create_table_if_does_not_exist);
        properties.actionOnData.setValue(ActionOnData.Insert);

        properties.schema.schema.setValue(TableHelper.getWriteSchema());
        properties.schemaReject.schema.setValue(TableHelper.getRejectSchema());
        properties.partitionKey.setStoredValue("PartitionKey");
        properties.rowKey.setStoredValue("RowKey");

    }

    @Test
    public void testOpenWriterWhenSinkAvailable() {
        // setup
        assertEquals(ValidationResult.Result.OK, sink.initialize(container, properties).getStatus());
        assertEquals(ValidationResult.Result.OK, sink.validate(container).getStatus());
        WriteOperation<?> writeOperation = sink.createWriteOperation();
        writeOperation.initialize(container);
        writer = (AzureStorageTableWriter) writeOperation.createWriter(container);

        // mock
        writer.tableservice = tableService;
        try {
            doAnswer(new Answer<Void>() {

                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    return null;
                }
            }).when(tableService).handleActionOnTable(Mockito.<String>any(), Mockito.<ActionOnTable>any());

            // open should not fail
            writer.open(RandomStringUtils.random(12));

        } catch (InvalidKeyException | URISyntaxException | StorageException | IOException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test(expected = ComponentException.class)
    public void testOpenWriterWehnSinkHasError() {
        // setup
        assertEquals(ValidationResult.Result.OK, sink.initialize(container, properties).getStatus());
        assertEquals(ValidationResult.Result.OK, sink.validate(container).getStatus());
        WriteOperation<?> writeOperation = sink.createWriteOperation();
        writeOperation.initialize(container);
        writer = (AzureStorageTableWriter) writeOperation.createWriter(container);

        // mock
        writer.tableservice = tableService;
        try {
            doAnswer(new Answer<Void>() {

                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    throw new StorageException("500", "Unavailable sink", new RuntimeException());
                }
            }).when(tableService).handleActionOnTable(anyString(), any(ActionOnTable.class));

            // open should not fail
            writer.open(RandomStringUtils.random(12));

        } catch (InvalidKeyException | URISyntaxException | StorageException | IOException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test
    public void testWriteNullRecordToSink() {
        // setup
        assertEquals(ValidationResult.Result.OK, sink.initialize(container, properties).getStatus());
        assertEquals(ValidationResult.Result.OK, sink.validate(container).getStatus());
        WriteOperation<?> writeOperation = sink.createWriteOperation();
        writeOperation.initialize(container);
        writer = (AzureStorageTableWriter) writeOperation.createWriter(container);

        // mock
        writer.tableservice = tableService;
        try {
            doAnswer(new Answer<Void>() {

                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    return null;
                }
            }).when(tableService).handleActionOnTable(anyString(), any(ActionOnTable.class));

            // should not fail when record is null
            writer.open(RandomStringUtils.random(12));
            writer.write(null);
            writer.close();

        } catch (InvalidKeyException | URISyntaxException | StorageException | IOException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test
    public void testWriteValidRecordsToAvailableSink() {
        // setup
        assertEquals(ValidationResult.Result.OK, sink.initialize(container, properties).getStatus());
        assertEquals(ValidationResult.Result.OK, sink.validate(container).getStatus());
        WriteOperation<?> writeOperation = sink.createWriteOperation();
        writeOperation.initialize(container);
        writer = (AzureStorageTableWriter) writeOperation.createWriter(container);

        // mock
        writer.tableservice = tableService;
        try {
            doAnswer(new Answer<Void>() {

                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    return null;
                }
            }).when(tableService).handleActionOnTable(Mockito.<String>any(), any(ActionOnTable.class));
            when(tableService.executeOperation(Mockito.<String>any(), any(TableOperation.class))).thenReturn(new TableResult(200));

            // assert
            writer.open(RandomStringUtils.random(12));
            for (int i = 0; i < 500; i++) {
                writer.write(TableHelper.getRecord(i));
            }
            writer.close();

        } catch (InvalidKeyException | URISyntaxException | StorageException | IOException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test
    public void testWriteRecordWithDynamicSchemaToAvailableSink() {
        // setup
        properties.schema.schema.setValue(TableHelper.getDynamicWriteSchema());

        assertEquals(ValidationResult.Result.OK, sink.initialize(container, properties).getStatus());
        assertEquals(ValidationResult.Result.OK, sink.validate(container).getStatus());
        WriteOperation<?> writeOperation = sink.createWriteOperation();
        writeOperation.initialize(container);
        writer = (AzureStorageTableWriter) writeOperation.createWriter(container);

        // mock
        writer.tableservice = tableService;
        try {
            doAnswer(new Answer<Void>() {

                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    return null;
                }
            }).when(tableService).handleActionOnTable(anyString(), any(ActionOnTable.class));
            when(tableService.executeOperation(anyString(), any(TableOperation.class))).thenReturn(new TableResult(200));

            // assert
            writer.open(RandomStringUtils.random(12));
            writer.write(TableHelper.getRecord(0));
            writer.close();

        } catch (InvalidKeyException | URISyntaxException | StorageException | IOException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test
    public void testWriteToUnavailableSinkHandleError() {

        assertEquals(ValidationResult.Result.OK, sink.initialize(container, properties).getStatus());
        assertEquals(ValidationResult.Result.OK, sink.validate(container).getStatus());
        WriteOperation<?> writeOperation = sink.createWriteOperation();
        writeOperation.initialize(container);
        writer = (AzureStorageTableWriter) writeOperation.createWriter(container);

        // mock
        writer.tableservice = tableService;
        try {
            doAnswer(new Answer<Void>() {

                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    return null;
                }
            }).when(tableService).handleActionOnTable(anyString(), any(ActionOnTable.class));
            when(tableService.executeOperation(anyString(), any(TableOperation.class)))
                    .thenThrow(new StorageException("500", "insertion problem", new RuntimeException()));

            // assert
            writer.open(RandomStringUtils.random(12));
            for (int i = 0; i < 2; i++) {
                writer.write(TableHelper.getRecord(i));
            }
            writer.close();

        } catch (InvalidKeyException | URISyntaxException | StorageException | IOException e) {
            fail("should not throw " + e.getMessage());
        }

    }

    @Test(expected = ComponentException.class)
    public void testWriteToUnavailableSinkDieOnError() {

        properties.dieOnError.setValue(true);

        assertEquals(ValidationResult.Result.OK, sink.initialize(container, properties).getStatus());
        assertEquals(ValidationResult.Result.OK, sink.validate(container).getStatus());
        WriteOperation<?> writeOperation = sink.createWriteOperation();
        writeOperation.initialize(container);
        writer = (AzureStorageTableWriter) writeOperation.createWriter(container);

        // mock
        writer.tableservice = tableService;
        try {
            doAnswer(new Answer<Void>() {

                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    return null;
                }
            }).when(tableService).handleActionOnTable(anyString(), any(ActionOnTable.class));
            when(tableService.executeOperation(anyString(), any(TableOperation.class)))
                    .thenThrow(new StorageException("500", "insertion problem", new RuntimeException()));

            // assert
            writer.open(RandomStringUtils.random(12));
            for (int i = 0; i < 2; i++) {
                writer.write(TableHelper.getRecord(i));
            }
            writer.close();

        } catch (InvalidKeyException | URISyntaxException | StorageException | IOException e) {
            fail("should not throw " + e.getMessage());
        }

    }

    @Test
    public void testGetWriteOperation() {
        // setup
        assertEquals(ValidationResult.Result.OK, sink.initialize(container, properties).getStatus());
        assertEquals(ValidationResult.Result.OK, sink.validate(container).getStatus());
        WriteOperation<?> writeOperation = sink.createWriteOperation();
        writeOperation.initialize(container);
        writer = (AzureStorageTableWriter) writeOperation.createWriter(container);
        assertNotNull(writer);
        assertNotNull(writer.getWriteOperation());
    }



}
