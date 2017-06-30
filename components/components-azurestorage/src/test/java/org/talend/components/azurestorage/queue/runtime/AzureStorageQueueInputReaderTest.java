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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.azurestorage.AzureBaseTest;
import org.talend.components.azurestorage.queue.AzureStorageQueueDefinition;
import org.talend.components.azurestorage.queue.AzureStorageQueueService;
import org.talend.components.azurestorage.queue.tazurestoragequeueinput.TAzureStorageQueueInputProperties;
import org.talend.daikon.properties.ValidationResult;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueueMessage;

public class AzureStorageQueueInputReaderTest extends AzureBaseTest {

    private TAzureStorageQueueInputProperties properties;

    private AzureStorageQueueInputReader reader;

    @Mock
    private AzureStorageQueueService queueService;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setup() throws IOException {
        properties = new TAzureStorageQueueInputProperties(PROP_ + "QueueInputReader");
        properties.setupProperties();
        properties.connection = getValidFakeConnection();
        properties.queueName.setValue("some-queue-name");
    }

    @Test
    public void testStartPeekAsStartable() {
        try {
            properties.peekMessages.setValue(true);

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<CloudQueueMessage> messages = new ArrayList<>();
            messages.add(new CloudQueueMessage("message-1"));
            when(queueService.peekMessages(anyString(), anyInt())).thenReturn(new Iterable<CloudQueueMessage>() {

                @Override
                public Iterator<CloudQueueMessage> iterator() {
                    return new DummyCloudQueueMessageIterator(messages);
                }
            });
            boolean startable = reader.start();
            assertTrue(startable);
        } catch (IOException | InvalidKeyException | URISyntaxException | StorageException e) {
            fail("sould not throw " + e.getMessage());
        }
    }

    @Test
    public void testStartRetrieveMessageAsStartable() {
        try {
            properties.peekMessages.setValue(false);

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<CloudQueueMessage> messages = new ArrayList<>();
            messages.add(new CloudQueueMessage("message-1"));
            when(queueService.retrieveMessages(anyString(), anyInt(), anyInt())).thenReturn(new Iterable<CloudQueueMessage>() {

                @Override
                public Iterator<CloudQueueMessage> iterator() {
                    return new DummyCloudQueueMessageIterator(messages);
                }
            });
            boolean startable = reader.start();
            assertTrue(startable);
        } catch (IOException | InvalidKeyException | URISyntaxException | StorageException e) {
            fail("sould not throw " + e.getMessage());
        }
    }

    @Test
    public void testStartPeekAsNonStartable() {
        try {
            properties.peekMessages.setValue(true);

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<CloudQueueMessage> messages = new ArrayList<>();
            when(queueService.peekMessages(anyString(), anyInt())).thenReturn(new Iterable<CloudQueueMessage>() {

                @Override
                public Iterator<CloudQueueMessage> iterator() {
                    return new DummyCloudQueueMessageIterator(messages);
                }
            });
            boolean startable = reader.start();
            assertFalse(startable);
        } catch (IOException | InvalidKeyException | URISyntaxException | StorageException e) {
            fail("sould not throw " + e.getMessage());
        }
    }

    @Test
    public void testStartPeekAndDeleteMessage() {
        try {
            properties.peekMessages.setValue(true);
            properties.deleteMessages.setValue(true);

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<CloudQueueMessage> messages = new ArrayList<>();
            messages.add(new CloudQueueMessage("message-1"));
            when(queueService.peekMessages(anyString(), anyInt())).thenReturn(new Iterable<CloudQueueMessage>() {

                @Override
                public Iterator<CloudQueueMessage> iterator() {
                    return new DummyCloudQueueMessageIterator(messages);
                }
            });

            doAnswer(new Answer<Void>() {

                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    return null;
                }
            }).when(queueService).deleteMessage(anyString(), any(CloudQueueMessage.class));

            boolean startable = reader.start();
            assertTrue(startable);
        } catch (IOException | InvalidKeyException | URISyntaxException | StorageException e) {
            fail("sould not throw " + e.getMessage());
        }
    }

    @Test
    public void testStartPeekHandleDeletionError() {
        try {
            properties.peekMessages.setValue(true);
            properties.deleteMessages.setValue(true);

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<CloudQueueMessage> messages = new ArrayList<>();
            messages.add(new CloudQueueMessage("message-1"));
            when(queueService.peekMessages(anyString(), anyInt())).thenReturn(new Iterable<CloudQueueMessage>() {

                @Override
                public Iterator<CloudQueueMessage> iterator() {
                    return new DummyCloudQueueMessageIterator(messages);
                }
            });

            doAnswer(new Answer<Void>() {

                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    throw new StorageException("code", "message-1 can't be deleted", new RuntimeException());
                }
            }).when(queueService).deleteMessage(anyString(), any(CloudQueueMessage.class));

            boolean startable = reader.start();
            assertTrue(startable);
        } catch (IOException | InvalidKeyException | URISyntaxException | StorageException e) {
            fail("sould not throw " + e.getMessage());
        }
    }

    @Test
    public void testStartHandleError() {
        try {
            properties.peekMessages.setValue(true);
            properties.dieOnError.setValue(false);

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<CloudQueueMessage> messages = new ArrayList<>();
            messages.add(new CloudQueueMessage("message-1"));
            when(queueService.peekMessages(anyString(), anyInt()))
                    .thenThrow(new StorageException("code", "some storage exception", new RuntimeException()));

            boolean startable = reader.start();
            assertFalse(startable);
        } catch (IOException | InvalidKeyException | URISyntaxException | StorageException e) {
            fail("sould not throw " + e.getMessage());
        }
    }

    @Test(expected = ComponentException.class)
    public void testStartDieOnError() {
        try {
            properties.peekMessages.setValue(true);
            properties.dieOnError.setValue(true);

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<CloudQueueMessage> messages = new ArrayList<>();
            messages.add(new CloudQueueMessage("message-1"));
            when(queueService.peekMessages(anyString(), anyInt()))
                    .thenThrow(new StorageException("code", "some storage exception", new RuntimeException()));

            boolean startable = reader.start();
            assertFalse(startable);
        } catch (IOException | InvalidKeyException | URISyntaxException | StorageException e) {
            fail("sould not throw " + e.getMessage());
        }
    }

    @Test
    public void testAdvanceAsAdvancable() {
        try {
            properties.peekMessages.setValue(true);

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<CloudQueueMessage> messages = new ArrayList<>();
            messages.add(new CloudQueueMessage("message-1"));
            messages.add(new CloudQueueMessage("message-2"));
            messages.add(new CloudQueueMessage("message-3"));
            when(queueService.peekMessages(anyString(), anyInt())).thenReturn(new Iterable<CloudQueueMessage>() {

                @Override
                public Iterator<CloudQueueMessage> iterator() {
                    return new DummyCloudQueueMessageIterator(messages);
                }
            });
            boolean startable = reader.start();
            assertTrue(startable);
            boolean advancable = reader.advance();
            assertTrue(advancable);

        } catch (IOException | InvalidKeyException | URISyntaxException | StorageException e) {
            fail("sould not throw " + e.getMessage());
        }
    }

    @Test
    public void testAdvanceAsNonAdvancable() {
        try {
            properties.peekMessages.setValue(true);
            properties.deleteMessages.setValue(true);

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<CloudQueueMessage> messages = new ArrayList<>();
            messages.add(new CloudQueueMessage("message-1"));
            when(queueService.peekMessages(anyString(), anyInt())).thenReturn(new Iterable<CloudQueueMessage>() {

                @Override
                public Iterator<CloudQueueMessage> iterator() {
                    return new DummyCloudQueueMessageIterator(messages);
                }
            });

            doAnswer(new Answer<Void>() {

                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    throw new StorageException("code", "message-1 can't be deleted", new RuntimeException());
                }
            }).when(queueService).deleteMessage(anyString(), any(CloudQueueMessage.class));

            boolean startable = reader.start();
            assertTrue(startable);
            boolean advancable = reader.advance();
            assertFalse(advancable);

        } catch (IOException | InvalidKeyException | URISyntaxException | StorageException e) {
            fail("sould not throw " + e.getMessage());
        }
    }

    @Test
    public void testAdvanceWhenNonStartble() {
        try {
            properties.peekMessages.setValue(true);
            properties.deleteMessages.setValue(true);

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<CloudQueueMessage> messages = new ArrayList<>();
            when(queueService.peekMessages(anyString(), anyInt())).thenReturn(new Iterable<CloudQueueMessage>() {

                @Override
                public Iterator<CloudQueueMessage> iterator() {
                    return new DummyCloudQueueMessageIterator(messages);
                }
            });

            doAnswer(new Answer<Void>() {

                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    throw new StorageException("code", "message-1 can't be deleted", new RuntimeException());
                }
            }).when(queueService).deleteMessage(anyString(), any(CloudQueueMessage.class));

            boolean startable = reader.start();
            assertFalse(startable);
            boolean advancable = reader.advance();
            assertFalse(advancable);

        } catch (IOException | InvalidKeyException | URISyntaxException | StorageException e) {
            fail("sould not throw " + e.getMessage());
        }
    }

    @Test
    public void testAdvanceWithMessageDeletion() {
        try {
            properties.peekMessages.setValue(true);
            properties.deleteMessages.setValue(true);

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<CloudQueueMessage> messages = new ArrayList<>();
            messages.add(new CloudQueueMessage("message-1"));
            messages.add(new CloudQueueMessage("message-2"));
            messages.add(new CloudQueueMessage("message-3"));
            when(queueService.peekMessages(anyString(), anyInt())).thenReturn(new Iterable<CloudQueueMessage>() {

                @Override
                public Iterator<CloudQueueMessage> iterator() {
                    return new DummyCloudQueueMessageIterator(messages);
                }
            });

            doAnswer(new Answer<Void>() {

                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    return null;
                }
            }).when(queueService).deleteMessage(anyString(), any(CloudQueueMessage.class));

            boolean startable = reader.start();
            assertTrue(startable);
            boolean advancable = reader.advance();
            assertTrue(advancable);

        } catch (IOException | InvalidKeyException | URISyntaxException | StorageException e) {
            fail("sould not throw " + e.getMessage());
        }
    }

    @Test
    public void testAdvanceWithMessageDeletionHandleError() {
        try {
            properties.peekMessages.setValue(true);
            properties.deleteMessages.setValue(true);

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<CloudQueueMessage> messages = new ArrayList<>();
            messages.add(new CloudQueueMessage("message-1"));
            messages.add(new CloudQueueMessage("message-2"));
            messages.add(new CloudQueueMessage("message-3"));
            when(queueService.peekMessages(anyString(), anyInt())).thenReturn(new Iterable<CloudQueueMessage>() {

                @Override
                public Iterator<CloudQueueMessage> iterator() {
                    return new DummyCloudQueueMessageIterator(messages);
                }
            });

            doAnswer(new Answer<Void>() {

                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    throw new StorageException("code", "message-1 can't be deleted", new RuntimeException());
                }
            }).when(queueService).deleteMessage(anyString(), any(CloudQueueMessage.class));

            boolean startable = reader.start();
            assertTrue(startable);
            boolean advancable = reader.advance();
            assertTrue(advancable);

        } catch (IOException | InvalidKeyException | URISyntaxException | StorageException e) {
            fail("sould not throw " + e.getMessage());
        }
    }

    @Test
    public void testGetCurrent() {
        try {
            properties.peekMessages.setValue(true);

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<CloudQueueMessage> messages = new ArrayList<>();
            messages.add(new CloudQueueMessage("message-1"));
            messages.add(new CloudQueueMessage("message-2"));
            messages.add(new CloudQueueMessage("message-3"));
            when(queueService.peekMessages(anyString(), anyInt())).thenReturn(new Iterable<CloudQueueMessage>() {

                @Override
                public Iterator<CloudQueueMessage> iterator() {
                    return new DummyCloudQueueMessageIterator(messages);
                }
            });
            boolean startable = reader.start();
            assertTrue(startable);
            int i = 1;
            do {
                IndexedRecord current = reader.getCurrent();
                assertNotNull(current);
                assertNotNull(current.getSchema());
                Field msgField = current.getSchema().getField(TAzureStorageQueueInputProperties.FIELD_MESSAGE_CONTENT);
                assertTrue(current.get(msgField.pos()).equals("message-" + i));
                i++;
            } while (reader.advance());

        } catch (IOException | InvalidKeyException | URISyntaxException | StorageException e) {
            fail("sould not throw " + e.getMessage());
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetCurrentWhenNotStartable() {
        try {
            properties.peekMessages.setValue(true);

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<CloudQueueMessage> messages = new ArrayList<>();
            when(queueService.peekMessages(anyString(), anyInt())).thenReturn(new Iterable<CloudQueueMessage>() {

                @Override
                public Iterator<CloudQueueMessage> iterator() {
                    return new DummyCloudQueueMessageIterator(messages);
                }
            });
            boolean startable = reader.start();
            assertFalse(startable);
            reader.getCurrent(); // should throw NoSuchElementException

        } catch (IOException | InvalidKeyException | URISyntaxException | StorageException e) {
            fail("sould not throw " + e.getMessage());
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetCurrentWhenNotAdvancable() {
        try {
            properties.peekMessages.setValue(true);

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<CloudQueueMessage> messages = new ArrayList<>();
            messages.add(new CloudQueueMessage("message-1"));
            when(queueService.peekMessages(anyString(), anyInt())).thenReturn(new Iterable<CloudQueueMessage>() {

                @Override
                public Iterator<CloudQueueMessage> iterator() {
                    return new DummyCloudQueueMessageIterator(messages);
                }
            });
            boolean startable = reader.start();
            assertTrue(startable);
            assertNotNull(reader.getCurrent());
            boolean advancable = reader.advance();
            assertFalse(advancable);
            reader.getCurrent(); // should throw NoSuchElementException

        } catch (IOException | InvalidKeyException | URISyntaxException | StorageException e) {
            fail("sould not throw " + e.getMessage());
        }
    }

    @Test
    public void testGetReturnValues() {
        try {
            properties.peekMessages.setValue(true);

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<CloudQueueMessage> messages = new ArrayList<>();
            messages.add(new CloudQueueMessage("message-1"));
            messages.add(new CloudQueueMessage("message-2"));
            messages.add(new CloudQueueMessage("message-3"));
            when(queueService.peekMessages(anyString(), anyInt())).thenReturn(new Iterable<CloudQueueMessage>() {

                @Override
                public Iterator<CloudQueueMessage> iterator() {
                    return new DummyCloudQueueMessageIterator(messages);
                }
            });
            boolean startable = reader.start();
            assertTrue(startable);
            while (reader.advance()) {
                // read all messages to init the returned values at the end
            }
            Map<String, Object> returnedValues = reader.getReturnValues();
            assertNotNull(returnedValues);
            assertEquals("some-queue-name", returnedValues.get(AzureStorageQueueDefinition.RETURN_QUEUE_NAME));
            assertEquals(3, returnedValues.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));
        } catch (IOException | InvalidKeyException | URISyntaxException | StorageException e) {
            fail("sould not throw " + e.getMessage());
        }
    }
}
