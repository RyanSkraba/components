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

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.*;

import org.junit.*;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.azurestorage.RuntimeContainerMock;
import org.talend.components.azurestorage.table.AzureStorageTableService;
import org.talend.components.azurestorage.table.helpers.*;
import org.talend.components.azurestorage.table.tazurestorageinputtable.TAzureStorageInputTableProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties.Protocol;
import org.talend.daikon.properties.ValidationResult;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.DynamicTableEntity;
import com.microsoft.azure.storage.table.TableQuery;

public class AzureStorageTableReaderTest {

    public static final String PROP_ = "PROP_";

    private RuntimeContainer container;

    private AzureStorageTableReader reader;

    private TAzureStorageInputTableProperties properties;

    private AzureStorageTableSource source;

    @Mock
    private AzureStorageTableService tableService;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setUp() throws Exception {
        container = new RuntimeContainerMock();

        properties = new TAzureStorageInputTableProperties(PROP_ + "InputTable");
        properties.setupProperties();
        // valid fake connection
        properties.connection = new TAzureStorageConnectionProperties(PROP_ + "Connection");
        properties.connection.protocol.setValue(Protocol.HTTP);
        properties.connection.accountName.setValue("fakeAccountName");
        properties.connection.accountKey.setValue("fakeAccountKey=ANBHFYRJJFHRIKKJFU");

        properties.tableName.setValue("testTable");

        properties.filterExpression.column.setValue(new ArrayList<String>());
        properties.filterExpression.fieldType.setValue(new ArrayList<String>());
        properties.filterExpression.function.setValue(new ArrayList<String>());
        properties.filterExpression.operand.setValue(new ArrayList<String>());
        properties.filterExpression.predicate.setValue(new ArrayList<String>());

        properties.filterExpression.column.getValue().add("PartitionKey");
        properties.filterExpression.fieldType.getValue().add(SupportedFieldType.STRING.name());
        properties.filterExpression.function.getValue().add(Comparison.EQUAL.name());
        properties.filterExpression.operand.getValue().add("Departement");
        properties.filterExpression.predicate.getValue().add(Predicate.AND.name());

        source = new AzureStorageTableSource();

    }

    /**
     * Test the reader behavior when the data source is empty
     */
    @Test(expected = NoSuchElementException.class)
    public void testReadSourceEmpty() {

        // setup
        properties.dieOnError.setValue(false);

        assertEquals(ValidationResult.Result.OK, source.initialize(container, properties).getStatus());
        assertEquals(ValidationResult.Result.OK, source.validate(container).getStatus());
        reader = (AzureStorageTableReader) source.createReader(container);

        // mock
        final List<DynamicTableEntity> records = new ArrayList<>();
        try {
            reader.tableService = tableService;
            when(tableService.executeQuery(anyString(), any(TableQuery.class))).thenReturn(new Iterable<DynamicTableEntity>() {

                @Override
                public Iterator<DynamicTableEntity> iterator() {
                    return records.iterator();
                }
            });
        } catch (InvalidKeyException | URISyntaxException | StorageException e) {
            fail("should not throw " + e.getMessage());
        }

        // assert
        // Reader do not start and can not advance
        try {
            assertNotNull(reader);
            assertFalse(reader.start());
            assertFalse(reader.advance());

            reader.getCurrent();
        } catch (IOException e) {
            fail("should not throw " + e.getMessage());
        }

    }

    /**
     * Test the reader behavior when the data source contains only one element
     */
    @Test(expected = NoSuchElementException.class)
    public void testReadSourceWithOnly1Element() {

        // setup
        properties.dieOnError.setValue(false);

        assertEquals(ValidationResult.Result.OK, source.initialize(container, properties).getStatus());
        assertEquals(ValidationResult.Result.OK, source.validate(container).getStatus());
        reader = (AzureStorageTableReader) source.createReader(container);

        // mock
        final List<DynamicTableEntity> records = new ArrayList<>();
        records.add(new DynamicTableEntity("Departement", "1"));
        try {
            reader.tableService = tableService;
            when(tableService.executeQuery(anyString(), any(TableQuery.class))).thenReturn(new Iterable<DynamicTableEntity>() {

                @Override
                public Iterator<DynamicTableEntity> iterator() {
                    return records.iterator();
                }
            });
        } catch (InvalidKeyException | URISyntaxException | StorageException e) {
            fail("should not throw " + e.getMessage());
        }

        // assert
        // Reader can start and cannot advance
        try {
            assertNotNull(reader);
            assertTrue(reader.start());
            assertNotNull(reader.getCurrent());
            Map<String, Object> returnedValues = reader.getReturnValues();
            assertNotNull(returnedValues);
            assertTrue(returnedValues.containsKey(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));
            assertEquals(1, returnedValues.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));

            assertFalse(reader.advance());
            reader.getCurrent();
            fail("expect the reader#getCurrent() to throw a NoSuchElementException as the reader#start() returned false.");

        } catch (IOException e) {
            fail("should not throw " + e.getMessage());
        }

    }

    /**
     * Test the reader behavior when the data source contains many elements
     */
    @Test
    public void testReadSourceWithManyElements() {

        // setup
        properties.dieOnError.setValue(false);

        assertEquals(ValidationResult.Result.OK, source.initialize(container, properties).getStatus());
        assertEquals(ValidationResult.Result.OK, source.validate(container).getStatus());
        reader = (AzureStorageTableReader) source.createReader(container);

        // mock
        final List<DynamicTableEntity> records = new ArrayList<>();
        records.add(new DynamicTableEntity("Departement", "1"));
        records.add(new DynamicTableEntity("Departement", "2"));
        records.add(new DynamicTableEntity("Departement", "3"));
        try {
            reader.tableService = tableService;
            when(tableService.executeQuery(anyString(), any(TableQuery.class))).thenReturn(new Iterable<DynamicTableEntity>() {

                @Override
                public Iterator<DynamicTableEntity> iterator() {
                    return records.iterator();
                }
            });
        } catch (InvalidKeyException | URISyntaxException | StorageException e) {
            fail("should not throw " + e.getMessage());
        }

        // assert
        // Reader can start and can advance whit no error
        try {
            assertNotNull(reader);
            assertTrue(reader.start());
            assertNotNull(reader.getCurrent());

            int dataCount = 1;
            while (reader.advance()) {
                assertNotNull(reader.getCurrent());
                dataCount++;
            }
            assertTrue(dataCount > 1); // assert that the reader advanced at least once
            Map<String, Object> returnedValues = reader.getReturnValues();
            assertNotNull(returnedValues);
            assertTrue(returnedValues.containsKey(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));
            assertEquals(dataCount, returnedValues.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));

        } catch (IOException e) {
            fail("should not throw " + e.getMessage());
        }

    }

    /**
     * Test the reader behavior when the data source is unavailable reader stop
     */
    @Test(expected = ComponentException.class)
    public void testReadSourceUnavailableDieOnError() {

        // setup
        properties.dieOnError.setValue(true);

        assertEquals(ValidationResult.Result.OK, source.initialize(container, properties).getStatus());
        assertEquals(ValidationResult.Result.OK, source.validate(container).getStatus());
        reader = (AzureStorageTableReader) source.createReader(container);

        // mock
        try {
            reader.tableService = tableService;
            when(tableService.executeQuery(anyString(), any(TableQuery.class)))
                    .thenThrow(new StorageException("500", "Storage unavailable", new RuntimeException("")));

        } catch (InvalidKeyException | URISyntaxException | StorageException e) {
            fail("should not throw " + e.getMessage());
        }

        // assert
        // Reader die on error
        try {
            assertNotNull(reader);
            reader.start();
        } catch (IOException e) {
            fail("should not throw " + e.getMessage());
        }

    }

    /**
     * Test the reader behavior when the data source is unavailable reader handle error
     */
    @Test
    public void testReadSourceUnavailableHandleError() {

        // setup
        properties.dieOnError.setValue(false);

        assertEquals(ValidationResult.Result.OK, source.initialize(container, properties).getStatus());
        assertEquals(ValidationResult.Result.OK, source.validate(container).getStatus());
        reader = (AzureStorageTableReader) source.createReader(container);

        // mock
        try {
            reader.tableService = tableService;
            when(tableService.executeQuery(anyString(), any(TableQuery.class)))
                    .thenThrow(new StorageException("500", "Storage unavailable", new RuntimeException("")));

        } catch (InvalidKeyException | URISyntaxException | StorageException e) {
            fail("should not throw " + e.getMessage());
        }

        // assert
        // Reader handle error
        try {
            assertNotNull(reader);
            assertFalse(reader.start());
        } catch (IOException e) {
            fail("should not throw " + e.getMessage());
        }

    }

    /**
     * Test reader close
     */
    @Test
    public void testClose() {

    }

}
