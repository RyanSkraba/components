package org.talend.components.marklogic.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Test;
import org.mockito.Mockito;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.marklogic.tmarklogicoutput.MarkLogicOutputProperties;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.document.DocumentDescriptor;
import com.marklogic.client.document.DocumentManager;
import com.marklogic.client.document.DocumentUriTemplate;
import com.marklogic.client.io.FileHandle;
import com.marklogic.client.io.marker.AbstractWriteHandle;
import com.marklogic.client.io.marker.DocumentPatchHandle;
import com.marklogic.client.io.marker.GenericWriteHandle;

public class MarkLogicWriterTest {

    private MarkLogicWriter writer;

    @Test
    public void testInitDocManagerMixed() throws IOException {
        MarkLogicSink sink = new MarkLogicSink();
        MarkLogicOutputProperties properties = new MarkLogicOutputProperties("outputProperties");
        properties.init();
        properties.connection.referencedComponent.componentInstanceId.setValue("Not null value");
        sink.ioProperties = properties;

        RuntimeContainer mockedContainer = mock(RuntimeContainer.class);
        DatabaseClient mockedClient = mock(DatabaseClient.class);
        Mockito.when(mockedContainer.getComponentData(anyString(), anyString())).thenReturn(mockedClient);

        writer = sink.createWriteOperation().createWriter(mockedContainer);

        writer.open("123");

        verify(mockedClient).newDocumentManager();
    }

    @Test
    public void testInitDocManagerXML() throws IOException {
        MarkLogicSink sink = new MarkLogicSink();
        MarkLogicOutputProperties properties = new MarkLogicOutputProperties("outputProperties");
        properties.init();
        properties.connection.referencedComponent.componentInstanceId.setValue("Not null value");
        properties.docType.setValue(MarkLogicOutputProperties.DocType.XML);
        sink.ioProperties = properties;

        RuntimeContainer mockedContainer = mock(RuntimeContainer.class);
        DatabaseClient mockedClient = mock(DatabaseClient.class);
        Mockito.when(mockedContainer.getComponentData(anyString(), anyString())).thenReturn(mockedClient);

        writer = sink.createWriteOperation().createWriter(mockedContainer);

        writer.open("123");

        verify(mockedClient).newXMLDocumentManager();
    }

    @Test
    public void testInitDocManagerJSON() throws IOException {
        MarkLogicSink sink = new MarkLogicSink();
        MarkLogicOutputProperties properties = new MarkLogicOutputProperties("outputProperties");
        properties.init();
        properties.connection.referencedComponent.componentInstanceId.setValue("Not null value");
        properties.docType.setValue(MarkLogicOutputProperties.DocType.JSON);
        sink.ioProperties = properties;

        RuntimeContainer mockedContainer = mock(RuntimeContainer.class);
        DatabaseClient mockedClient = mock(DatabaseClient.class);
        Mockito.when(mockedContainer.getComponentData(anyString(), anyString())).thenReturn(mockedClient);

        writer = sink.createWriteOperation().createWriter(mockedContainer);

        writer.open("123");

        verify(mockedClient).newJSONDocumentManager();
    }

    @Test
    public void testInitDocManagerBinary() throws IOException {
        MarkLogicSink sink = new MarkLogicSink();
        MarkLogicOutputProperties properties = new MarkLogicOutputProperties("outputProperties");
        properties.init();
        properties.connection.referencedComponent.componentInstanceId.setValue("Not null value");
        properties.docType.setValue(MarkLogicOutputProperties.DocType.BINARY);
        sink.ioProperties = properties;

        RuntimeContainer mockedContainer = mock(RuntimeContainer.class);
        DatabaseClient mockedClient = mock(DatabaseClient.class);
        Mockito.when(mockedContainer.getComponentData(anyString(), anyString())).thenReturn(mockedClient);

        writer = sink.createWriteOperation().createWriter(mockedContainer);

        writer.open("123");

        verify(mockedClient).newBinaryDocumentManager();
    }

    @Test
    public void testInitDocManagerText() throws IOException {
        MarkLogicSink sink = new MarkLogicSink();
        MarkLogicOutputProperties properties = new MarkLogicOutputProperties("outputProperties");
        properties.init();
        properties.connection.referencedComponent.componentInstanceId.setValue("Not null value");
        properties.docType.setValue(MarkLogicOutputProperties.DocType.PLAIN_TEXT);
        sink.ioProperties = properties;

        RuntimeContainer mockedContainer = mock(RuntimeContainer.class);
        DatabaseClient mockedClient = mock(DatabaseClient.class);
        Mockito.when(mockedContainer.getComponentData(anyString(), anyString())).thenReturn(mockedClient);

        writer = sink.createWriteOperation().createWriter(mockedContainer);

        writer.open("123");

        verify(mockedClient).newTextDocumentManager();
    }

    @Test
    public void testFailedInitDocManager() throws IOException {
        MarkLogicSink sink = mock(MarkLogicSink.class);
        MarkLogicOutputProperties properties = new MarkLogicOutputProperties("outputProperties");
        properties.init();
        MarkLogicWriteOperation writeOperation = new MarkLogicWriteOperation(sink, properties);

        Mockito.when(sink.connect((RuntimeContainer) anyObject())).thenReturn(null);

        writer = writeOperation.createWriter(null);

        writer.open("123");

        assertNull(writer.docMgr);
    }

    @Test
    public void testWriteNull() throws IOException {
        DocumentManager markLogicDocMngrMock = mock(DocumentManager.class);
        MarkLogicSink someSink = new MarkLogicSink();
        someSink.ioProperties = new MarkLogicOutputProperties("outputProps");
        writer = someSink.createWriteOperation().createWriter(null);
        writer.docMgr = markLogicDocMngrMock;
        writer.write(null);

        verifyZeroInteractions(markLogicDocMngrMock);
    }

    @Test
    public void testWriteNotIndexedRecord() throws IOException {
        DocumentManager markLogicDocMngrMock = mock(DocumentManager.class);
        MarkLogicSink someSink = new MarkLogicSink();
        someSink.ioProperties = new MarkLogicOutputProperties("outputProps");
        writer = someSink.createWriteOperation().createWriter(null);
        writer.docMgr = markLogicDocMngrMock;
        writer.write(new Object());

        verifyZeroInteractions(markLogicDocMngrMock);
    }

    @Test
    public void testMarkLogicDelete() throws IOException {
        DocumentManager markLogicDocMngrMock = prepareDocManagerText(MarkLogicOutputProperties.Action.DELETE,
                MarkLogicOutputProperties.DocType.JSON);

        verify(markLogicDocMngrMock).delete(anyString());
    }

    @Test
    public void testMarkLogicPatchJSON() throws IOException {
        DocumentManager markLogicDocMngrMock = prepareDocManagerText(MarkLogicOutputProperties.Action.PATCH,
                MarkLogicOutputProperties.DocType.JSON);

        verify(markLogicDocMngrMock).patch(anyString(), (DocumentPatchHandle) anyObject());
    }

    @Test
    public void testMarkLogicPatchXML() throws IOException {
        DocumentManager markLogicDocMngrMock = prepareDocManagerText(MarkLogicOutputProperties.Action.PATCH,
                MarkLogicOutputProperties.DocType.XML);

        verify(markLogicDocMngrMock).patch(anyString(), (DocumentPatchHandle) anyObject());
    }

    private DocumentManager prepareDocManagerText(MarkLogicOutputProperties.Action action,
            MarkLogicOutputProperties.DocType docType) throws IOException {
        return prepareDocManagerText(action, docType, "string doc content");
    }

    private DocumentManager prepareDocManagerText(MarkLogicOutputProperties.Action action,
            MarkLogicOutputProperties.DocType docType, Object docContent) throws IOException {
        MarkLogicSink sink = new MarkLogicSink();
        MarkLogicOutputProperties properties = new MarkLogicOutputProperties("outputProperties");
        properties.init();
        properties.connection.referencedComponent.componentInstanceId.setValue("Not null value");
        properties.docType.setValue(docType);
        properties.action.setValue(action);
        sink.ioProperties = properties;

        RuntimeContainer mockedContainer = mock(RuntimeContainer.class);
        DatabaseClient mockedClient = mock(DatabaseClient.class);
        Mockito.when(mockedContainer.getComponentData(anyString(), anyString())).thenReturn(mockedClient);

        DocumentManager markLogicDocMngrMock = mock(DocumentManager.class);

        writer = sink.createWriteOperation().createWriter(mockedContainer);
        GenericData.Record indexedRecord = new GenericData.Record(properties.datasetProperties.main.schema.getValue());
        indexedRecord.put(0, "docId");
        indexedRecord.put(1, docContent);

        writer.open("123");
        writer.docMgr = markLogicDocMngrMock;
        writer.write(indexedRecord);
        return markLogicDocMngrMock;
    }

    @Test
    public void testMarkLogicFailPatch() throws IOException {
        DocumentManager markLogicDocMngrMock = prepareDocManagerText(MarkLogicOutputProperties.Action.PATCH,
                MarkLogicOutputProperties.DocType.BINARY);

        verifyZeroInteractions(markLogicDocMngrMock);
        assertFalse(((Collection<IndexedRecord>) writer.getRejectedWrites()).isEmpty());
    }

    @Test
    public void testMarkLogicUpsertText() throws IOException {
        DocumentManager markLogicDocMngrMock = prepareDocManagerText(MarkLogicOutputProperties.Action.UPSERT,
                MarkLogicOutputProperties.DocType.PLAIN_TEXT);

        verify(markLogicDocMngrMock).write(anyString(), (GenericWriteHandle) anyObject());
    }

    @Test
    public void testMarkLogicUpsertBinaryText() throws IOException {
        DocumentManager markLogicDocMngrMock = prepareDocManagerText(MarkLogicOutputProperties.Action.UPSERT,
                MarkLogicOutputProperties.DocType.BINARY);

        verify(markLogicDocMngrMock).write(anyString(), (GenericWriteHandle) anyObject());
    }

    @Test
    public void testFailedUpsert() throws IOException {
        DocumentManager markLogicDocMngrMock = prepareDocManagerText(MarkLogicOutputProperties.Action.UPSERT,
                MarkLogicOutputProperties.DocType.BINARY, new Object());

        verifyZeroInteractions(markLogicDocMngrMock);

    }

    @Test
    public void testUpsertWithAutoGenerateDocId() throws IOException {
        MarkLogicSink sink = new MarkLogicSink();
        MarkLogicOutputProperties properties = new MarkLogicOutputProperties("outputProperties");
        properties.init();
        properties.connection.referencedComponent.componentInstanceId.setValue("Not null value");
        properties.docType.setValue(MarkLogicOutputProperties.DocType.BINARY);
        properties.action.setValue(MarkLogicOutputProperties.Action.UPSERT);
        properties.autoGenerateDocId.setValue(true);
        properties.docIdPrefix.setValue("somePrefix");
        sink.ioProperties = properties;

        RuntimeContainer mockedContainer = mock(RuntimeContainer.class);
        DatabaseClient mockedClient = mock(DatabaseClient.class);
        Mockito.when(mockedContainer.getComponentData(anyString(), anyString())).thenReturn(mockedClient);

        DocumentManager markLogicDocMngrMock = mock(DocumentManager.class);
        DocumentUriTemplate uriTemplateMock = mock(DocumentUriTemplate.class);
        DocumentDescriptor descriptorMock = mock(DocumentDescriptor.class);
        when(markLogicDocMngrMock.newDocumentUriTemplate(anyString())).thenReturn(uriTemplateMock);
        when(markLogicDocMngrMock.create(any(DocumentUriTemplate.class), any(AbstractWriteHandle.class)))
                .thenReturn(descriptorMock);
        when(descriptorMock.getUri()).thenReturn("somePrefix/docId");

        MarkLogicWriter writer = sink.createWriteOperation().createWriter(mockedContainer);
        GenericData.Record indexedRecord = new GenericData.Record(properties.datasetProperties.main.schema.getValue());
        indexedRecord.put(0, "docId");
        File docContent = new File("someFile");
        indexedRecord.put(1, docContent);

        writer.open("123");
        writer.docMgr = markLogicDocMngrMock;
        writer.write(indexedRecord);
        verify(markLogicDocMngrMock).write(eq("somePrefix/docId"), any(FileHandle.class));

        assertFalse(((Collection<IndexedRecord>) writer.getSuccessfulWrites()).isEmpty());
    }

    @Test
    public void testClose() throws IOException {
        prepareDocManagerText(MarkLogicOutputProperties.Action.DELETE, MarkLogicOutputProperties.DocType.MIXED);

        Result result = writer.close();
        assertEquals(1, result.totalCount);
        verify(writer.container).setComponentData(anyString(), eq("NB_LINE_DELETED"), eq(1));
    }

    @Test
    public void testGetWriteOperation() {
        MarkLogicSink sink = mock(MarkLogicSink.class);
        MarkLogicOutputProperties properties = new MarkLogicOutputProperties("outputProperties");
        properties.init();
        MarkLogicWriteOperation writeOperation = new MarkLogicWriteOperation(sink, properties);

        Mockito.when(sink.connect((RuntimeContainer) anyObject())).thenReturn(null);

        writer = writeOperation.createWriter(null);

        assertEquals(writeOperation, writer.getWriteOperation());
    }
}
