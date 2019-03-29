// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.components.marklogic.runtime.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Test;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.marklogic.exceptions.MarkLogicException;
import org.talend.components.marklogic.runtime.input.strategies.DocContentReader;
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionDefinition;
import org.talend.components.marklogic.tmarklogicinput.MarkLogicInputProperties;
import org.talend.daikon.avro.AvroUtils;

import com.marklogic.client.DatabaseClient;

public class MarkLogicRowProcessorTest {

    @Test
    public void testGetWriteOperation() {
        MarkLogicInputWriteOperation writeOperation = new MarkLogicInputWriteOperation(null, null);
        MarkLogicRowProcessor rowProcessor = writeOperation.createWriter(null);

        assertEquals(writeOperation, rowProcessor.getWriteOperation());
    }

    @Test
    public void testGetRejectedWrites() {
        MarkLogicRowProcessor rowProcessor = new MarkLogicRowProcessor(null, null, null);

        assertTrue(((Set<IndexedRecord>)rowProcessor.getRejectedWrites()).isEmpty());
    }

    @Test
    public void testOpen() throws IOException {
        DatabaseClient mockedClient = mock(DatabaseClient.class);
        MarkLogicInputSink mockedSink = mock(MarkLogicInputSink.class);

        MarkLogicInputProperties inputProperties = new MarkLogicInputProperties("inputProps");
        inputProperties.init();
        MarkLogicInputWriteOperation writeOperation = new MarkLogicInputWriteOperation(mockedSink, inputProperties);
        MarkLogicRowProcessor rowProcessor = writeOperation.createWriter(null);
        when(mockedSink.connect(any())).thenReturn(mockedClient);


        rowProcessor.open("123");

        assertNotNull(rowProcessor.docContentReader);
    }

    @Test
    public void testWriteNull() throws IOException {
        MarkLogicRowProcessor rowProcessor = new MarkLogicRowProcessor(null, null, null);
        DocContentReader mockedDocContentReader = mock(DocContentReader.class);
        rowProcessor.docContentReader = mockedDocContentReader;
        rowProcessor.write(null);

        verifyZeroInteractions(mockedDocContentReader);
    }

    @Test(expected = MarkLogicException.class)
    public void testWriteWithoutDocIdField() throws IOException {
        MarkLogicInputProperties inputProperties = new MarkLogicInputProperties("inputProps");
        inputProperties.init();
        inputProperties.inputSchema.schema.setValue(AvroUtils.createEmptySchema());
        inputProperties.docIdColumn.setValue("someValue");
        MarkLogicInputSink inputSink = new MarkLogicInputSink();

        MarkLogicInputWriteOperation writeOperation = new MarkLogicInputWriteOperation(inputSink, inputProperties);
        MarkLogicRowProcessor rowProcessor = writeOperation.createWriter(null);

        GenericData.Record someRecord = new GenericData.Record(inputProperties.datasetProperties.main.schema.getValue());
        rowProcessor.write(someRecord);
    }

    @Test
    public void testWriteCorrectRecord() throws IOException {
        MarkLogicInputProperties inputProperties = new MarkLogicInputProperties("inputProps");
        inputProperties.init();
        inputProperties.docIdColumn.setValue(inputProperties.datasetProperties.main.schema.getValue().getFields().get(0).name());

        MarkLogicInputSink inputSink = new MarkLogicInputSink();
        MarkLogicInputWriteOperation inputWriteOperation = new MarkLogicInputWriteOperation(inputSink, inputProperties);

        MarkLogicRowProcessor rowProcessor = inputWriteOperation.createWriter(null);
        DocContentReader mockedDocContentReader = mock(DocContentReader.class);

        rowProcessor.docContentReader = mockedDocContentReader;

        GenericData.Record correctRecord = new GenericData.Record(inputProperties.datasetProperties.main.schema.getValue());
        correctRecord.put(0, "docId");
        rowProcessor.write(correctRecord);

        verify(mockedDocContentReader).readDocument("docId");

        assertTrue(((ArrayList<IndexedRecord>)rowProcessor.getSuccessfulWrites()).size() > 0);
    }

    @Test
    public void testClose() throws IOException{
        DatabaseClient mockedClient = mock(DatabaseClient.class);
        MarkLogicInputSink mockedSink = mock(MarkLogicInputSink.class);

        MarkLogicInputProperties inputProperties = new MarkLogicInputProperties("inputProps");
        inputProperties.init();
        MarkLogicInputWriteOperation writeOperation = new MarkLogicInputWriteOperation(mockedSink, inputProperties);
        MarkLogicRowProcessor rowProcessor = writeOperation.createWriter(null);
        when(mockedSink.connect(any())).thenReturn(mockedClient);

        String uId = "123";
        rowProcessor.open(uId);
        Result result = rowProcessor.close();

        assertNotNull(result);
        assertEquals(uId, result.getuId());

        verify(mockedClient).release();
    }

    @Test
    public void testCloseReferencedConnectionNotReleased() throws IOException {
        DatabaseClient mockedClient = mock(DatabaseClient.class);
        MarkLogicInputSink mockedSink = mock(MarkLogicInputSink.class);

        MarkLogicInputProperties inputProperties = new MarkLogicInputProperties("inputProps");
        inputProperties.init();
        inputProperties.connection.referencedComponent.componentInstanceId.setValue(MarkLogicConnectionDefinition.COMPONENT_NAME + "_1");
        MarkLogicInputWriteOperation writeOperation = new MarkLogicInputWriteOperation(mockedSink, inputProperties);
        MarkLogicRowProcessor rowProcessor = writeOperation.createWriter(null);
        when(mockedSink.connect(any())).thenReturn(mockedClient);

        rowProcessor.open("1");
        verify(mockedClient).newDocumentManager();
        rowProcessor.close();

        verifyNoMoreInteractions(mockedClient);
    }

    @Test
    public void testCloseResult() throws IOException {
        MarkLogicInputSink sink = new MarkLogicInputSink();
        //set reference connection to avoid closing
        MarkLogicInputProperties inputProperties = new MarkLogicInputProperties("inputProps");
        inputProperties.init();
        inputProperties.connection.referencedComponent.componentInstanceId.setValue(MarkLogicConnectionDefinition.COMPONENT_NAME + "_1");
        MarkLogicInputWriteOperation writeOperation = new MarkLogicInputWriteOperation(sink, inputProperties);
        MarkLogicRowProcessor rowProcessor = writeOperation.createWriter(null);

        long longNBLine = Long.MAX_VALUE;
        rowProcessor.totalCounter = longNBLine;
        Result longResult = rowProcessor.close();
        assertTrue(longResult instanceof ResultWithLongNB);
        assertEquals(longNBLine, ((ResultWithLongNB)longResult).totalCountLong);
    }
}
