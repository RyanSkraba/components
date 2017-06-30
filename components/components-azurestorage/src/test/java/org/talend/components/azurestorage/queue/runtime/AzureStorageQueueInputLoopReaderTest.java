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
import org.talend.components.azurestorage.AzureBaseTest;
import org.talend.components.azurestorage.queue.AzureStorageQueueDefinition;
import org.talend.components.azurestorage.queue.AzureStorageQueueService;
import org.talend.components.azurestorage.queue.tazurestoragequeueinput.TAzureStorageQueueInputProperties;
import org.talend.components.azurestorage.queue.tazurestoragequeueinputloop.TAzureStorageQueueInputLoopProperties;
import org.talend.daikon.properties.ValidationResult;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueueMessage;

public class AzureStorageQueueInputLoopReaderTest extends AzureBaseTest {

    private TAzureStorageQueueInputLoopProperties properties;

    private AzureStorageQueueInputLoopReader reader;

    @Mock
    private AzureStorageQueueService queueService;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setup() throws IOException {
        properties = new TAzureStorageQueueInputLoopProperties(PROP_ + "QueueInputLoopReader");
        properties.setupProperties();
        properties.connection = getValidFakeConnection();
        properties.queueName.setValue("some-queue-name");
    }

    @Test
    public void testStartAsStartable() {
        try {

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputLoopReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<CloudQueueMessage> messages = new ArrayList<>();
            messages.add(new CloudQueueMessage("message-1"));
            when(queueService.retrieveMessages(anyString(), anyInt())).thenReturn(new Iterable<CloudQueueMessage>() {

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
    public void testAdvanceAsAdvancable() {
        try {

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputLoopReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<CloudQueueMessage> messages = new ArrayList<>();
            messages.add(new CloudQueueMessage("message-1"));
            messages.add(new CloudQueueMessage("message-2"));
            messages.add(new CloudQueueMessage("message-3"));
            when(queueService.retrieveMessages(anyString(), anyInt())).thenReturn(new Iterable<CloudQueueMessage>() {

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
    public void testGetCurrent() {
        try {

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputLoopReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<CloudQueueMessage> messages = new ArrayList<>();
            messages.add(new CloudQueueMessage("message-1"));
            messages.add(new CloudQueueMessage("message-2"));
            messages.add(new CloudQueueMessage("message-3"));
            when(queueService.retrieveMessages(anyString(), anyInt())).thenReturn(new Iterable<CloudQueueMessage>() {

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
                if (i == 3) {
                    break;
                }
            } while (reader.advance());

        } catch (IOException | InvalidKeyException | URISyntaxException | StorageException e) {
            fail("sould not throw " + e.getMessage());
        }
    }

    @Test
    public void testGetReturnValues() {
        try {

            AzureStorageQueueSource source = new AzureStorageQueueSource();
            ValidationResult vr = source.initialize(getDummyRuntimeContiner(), properties);
            assertNotNull(vr);
            assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());

            reader = (AzureStorageQueueInputLoopReader) source.createReader(getDummyRuntimeContiner());
            reader.queueService = queueService; // inject mocked service

            final List<CloudQueueMessage> messages = new ArrayList<>();
            messages.add(new CloudQueueMessage("message-1"));
            messages.add(new CloudQueueMessage("message-2"));
            messages.add(new CloudQueueMessage("message-3"));
            when(queueService.retrieveMessages(anyString(), anyInt())).thenReturn(new Iterable<CloudQueueMessage>() {

                @Override
                public Iterator<CloudQueueMessage> iterator() {
                    return new DummyCloudQueueMessageIterator(messages);
                }
            });
            boolean startable = reader.start();
            assertTrue(startable);
            int i = 1;
            while (reader.advance()) {
                // read all messages to init the returned values at the end
                i++;
                if (i == 3) {
                    break;
                }
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
