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
package org.talend.components.azurestorage.queue.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.azurestorage.AzureBaseTest;
import org.talend.components.azurestorage.queue.AzureStorageQueueService;
import org.talend.components.azurestorage.queue.tazurestoragequeuelist.TAzureStorageQueueListDefinition;
import org.talend.components.azurestorage.queue.tazurestoragequeuelist.TAzureStorageQueueListProperties;
import org.talend.daikon.properties.ValidationResult;

import com.microsoft.azure.storage.StorageCredentialsSharedAccessSignature;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueue;

public class AzureStorageQueueListReaderTest extends AzureBaseTest {

    private AzureStorageQueueListReader reader;

    private TAzureStorageQueueListProperties properties;

    private StorageCredentialsSharedAccessSignature dummyCredential;

    @Mock
    private AzureStorageQueueService queueService;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setUp() throws Exception {
        dummyCredential = new StorageCredentialsSharedAccessSignature("fakesaas");
        properties = new TAzureStorageQueueListProperties(PROP_ + "QueueList");
        properties.setupProperties();
        properties.connection = getValidFakeConnection();
    }

    @Test
    public void testStartAsStartable() {

        AzureStorageQueueSource source = new AzureStorageQueueSource();
        ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
        assertNotNull(vr);
        assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());
        reader = (AzureStorageQueueListReader) source.createReader(getDummyRuntimeContiner());
        reader.queueService = queueService;

        final List<CloudQueue> list = new ArrayList<>();
        try {

            list.add(new CloudQueue(new URI("https://storagesample.queue.core.windows.net/queue-1"), dummyCredential));
            when(queueService.listQueues()).thenReturn(new Iterable<CloudQueue>() {

                @Override
                public Iterator<CloudQueue> iterator() {
                    return new DummyCloudQueueIterator(list);
                }
            });

            assertTrue(reader.start());

        } catch (InvalidKeyException | URISyntaxException | StorageException | IOException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test
    public void testStartAsNonStartable() {

        AzureStorageQueueSource source = new AzureStorageQueueSource();
        ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
        assertNotNull(vr);
        assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());
        reader = (AzureStorageQueueListReader) source.createReader(getDummyRuntimeContiner());
        reader.queueService = queueService;

        final List<CloudQueue> list = new ArrayList<>();
        try {

            when(queueService.listQueues()).thenReturn(new Iterable<CloudQueue>() {

                @Override
                public Iterator<CloudQueue> iterator() {
                    return new DummyCloudQueueIterator(list);
                }
            });

            assertFalse(reader.start());

        } catch (InvalidKeyException | URISyntaxException | IOException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test
    public void testStartHandleError() {

        properties.dieOnError.setValue(false);

        AzureStorageQueueSource source = new AzureStorageQueueSource();
        ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
        assertNotNull(vr);
        assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());
        reader = (AzureStorageQueueListReader) source.createReader(getDummyRuntimeContiner());
        reader.queueService = queueService;
        try {

            when(queueService.listQueues()).thenThrow(new InvalidKeyException());

            assertFalse(reader.start());

        } catch (InvalidKeyException | URISyntaxException | IOException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test(expected = ComponentException.class)
    public void testStartDieOnError() {

        properties.dieOnError.setValue(true);

        AzureStorageQueueSource source = new AzureStorageQueueSource();
        ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
        assertNotNull(vr);
        assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());
        reader = (AzureStorageQueueListReader) source.createReader(getDummyRuntimeContiner());
        reader.queueService = queueService;
        try {

            when(queueService.listQueues()).thenThrow(new InvalidKeyException());
            reader.start(); // should throw ComponentException

        } catch (InvalidKeyException | URISyntaxException | IOException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test
    public void testAdvanceAsAdvancable() {

        AzureStorageQueueSource source = new AzureStorageQueueSource();
        ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
        assertNotNull(vr);
        assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());
        reader = (AzureStorageQueueListReader) source.createReader(getDummyRuntimeContiner());
        reader.queueService = queueService;

        final List<CloudQueue> list = new ArrayList<>();
        try {

            list.add(new CloudQueue(new URI("https://storagesample.queue.core.windows.net/queue-1"), dummyCredential));
            list.add(new CloudQueue(new URI("https://storagesample.queue.core.windows.net/queue-2"), dummyCredential));
            when(queueService.listQueues()).thenReturn(new Iterable<CloudQueue>() {

                @Override
                public Iterator<CloudQueue> iterator() {
                    return new DummyCloudQueueIterator(list);
                }
            });

            assertTrue(reader.start());
            assertTrue(reader.advance());

        } catch (InvalidKeyException | URISyntaxException | StorageException | IOException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test
    public void testAdvanceAsNonAdvancable() {

        AzureStorageQueueSource source = new AzureStorageQueueSource();
        ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
        assertNotNull(vr);
        assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());
        reader = (AzureStorageQueueListReader) source.createReader(getDummyRuntimeContiner());
        reader.queueService = queueService;

        final List<CloudQueue> list = new ArrayList<>();
        try {

            list.add(new CloudQueue(new URI("https://storagesample.queue.core.windows.net/queue-1"), dummyCredential));
            when(queueService.listQueues()).thenReturn(new Iterable<CloudQueue>() {

                @Override
                public Iterator<CloudQueue> iterator() {
                    return new DummyCloudQueueIterator(list);
                }
            });

            assertTrue(reader.start());
            assertFalse(reader.advance());

        } catch (InvalidKeyException | URISyntaxException | StorageException | IOException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test
    public void testAdvanceAsNonStartable() {

        AzureStorageQueueSource source = new AzureStorageQueueSource();
        ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
        assertNotNull(vr);
        assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());
        reader = (AzureStorageQueueListReader) source.createReader(getDummyRuntimeContiner());
        reader.queueService = queueService;

        final List<CloudQueue> list = new ArrayList<>();
        try {

            when(queueService.listQueues()).thenReturn(new Iterable<CloudQueue>() {

                @Override
                public Iterator<CloudQueue> iterator() {
                    return new DummyCloudQueueIterator(list);
                }
            });

            assertFalse(reader.start());
            assertFalse(reader.advance());

        } catch (InvalidKeyException | URISyntaxException | IOException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test
    public void testGetCurrent() {
        AzureStorageQueueSource source = new AzureStorageQueueSource();
        ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
        assertNotNull(vr);
        assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());
        reader = (AzureStorageQueueListReader) source.createReader(getDummyRuntimeContiner());
        reader.queueService = queueService;

        final List<CloudQueue> list = new ArrayList<>();
        try {

            list.add(new CloudQueue(new URI("https://storagesample.queue.core.windows.net/queue-1"), dummyCredential));
            when(queueService.listQueues()).thenReturn(new Iterable<CloudQueue>() {

                @Override
                public Iterator<CloudQueue> iterator() {
                    return new DummyCloudQueueIterator(list);
                }
            });

            assertTrue(reader.start());
            IndexedRecord current = reader.getCurrent();
            assertNotNull(current);
            assertEquals("queue-1", current.get(0));

        } catch (InvalidKeyException | URISyntaxException | StorageException | IOException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetCurrentWhenNotStartable() {
        AzureStorageQueueSource source = new AzureStorageQueueSource();
        ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
        assertNotNull(vr);
        assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());
        reader = (AzureStorageQueueListReader) source.createReader(getDummyRuntimeContiner());
        reader.queueService = queueService;

        final List<CloudQueue> list = new ArrayList<>();
        try {

            when(queueService.listQueues()).thenReturn(new Iterable<CloudQueue>() {

                @Override
                public Iterator<CloudQueue> iterator() {
                    return new DummyCloudQueueIterator(list);
                }
            });

            assertFalse(reader.start());
            reader.getCurrent();

        } catch (InvalidKeyException | URISyntaxException | IOException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetCurrentWhenNotAdvancable() {
        AzureStorageQueueSource source = new AzureStorageQueueSource();
        ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
        assertNotNull(vr);
        assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());
        reader = (AzureStorageQueueListReader) source.createReader(getDummyRuntimeContiner());
        reader.queueService = queueService;

        final List<CloudQueue> list = new ArrayList<>();
        try {

            list.add(new CloudQueue(new URI("https://storagesample.queue.core.windows.net/queue-1"), dummyCredential));
            when(queueService.listQueues()).thenReturn(new Iterable<CloudQueue>() {

                @Override
                public Iterator<CloudQueue> iterator() {
                    return new DummyCloudQueueIterator(list);
                }
            });

            assertTrue(reader.start());
            assertFalse(reader.advance());
            reader.getCurrent();

        } catch (InvalidKeyException | URISyntaxException | IOException | StorageException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test
    public void testGetReturnValues() {
        AzureStorageQueueSource source = new AzureStorageQueueSource();
        ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
        assertNotNull(vr);
        assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());
        reader = (AzureStorageQueueListReader) source.createReader(getDummyRuntimeContiner());
        reader.queueService = queueService;

        final List<CloudQueue> list = new ArrayList<>();
        try {

            list.add(new CloudQueue(new URI("https://storagesample.queue.core.windows.net/queue-1"), dummyCredential));
            list.add(new CloudQueue(new URI("https://storagesample.queue.core.windows.net/queue-2"), dummyCredential));
            list.add(new CloudQueue(new URI("https://storagesample.queue.core.windows.net/queue-3"), dummyCredential));
            when(queueService.listQueues()).thenReturn(new Iterable<CloudQueue>() {

                @Override
                public Iterator<CloudQueue> iterator() {
                    return new DummyCloudQueueIterator(list);
                }
            });

            assertTrue(reader.start());
            while (reader.advance()) {
                // read all records
            }

            Map<String, Object> returnedValues = reader.getReturnValues();
            assertNotNull(returnedValues);
            assertEquals(3, returnedValues.get(TAzureStorageQueueListDefinition.RETURN_NB_QUEUE));

        } catch (InvalidKeyException | URISyntaxException | StorageException | IOException e) {
            fail("should not throw " + e.getMessage());
        }
    }

}
