package org.talend.components.couchbase.runtime;

import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.talend.components.api.component.runtime.Result;

public class CouchbaseWriterTest {

    private CouchbaseWriter writer;

    private CouchbaseSink sink;

    private CouchbaseWriteOperation writeOperation;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
        writeOperation = Mockito.mock(CouchbaseWriteOperation.class);
        sink = Mockito.mock(CouchbaseSink.class);
        Mockito.when(sink.getIdFieldName()).thenReturn("idFieldName");
        Mockito.when(writeOperation.getSink()).thenReturn(sink);

        writer = new CouchbaseWriter(writeOperation);
    }

    @Test
    public void testWriter() throws IOException {
        // Prepare objects.
        CouchbaseConnection connection = Mockito.mock(CouchbaseConnection.class);
        Mockito.when(sink.getConnection()).thenReturn(connection);

        Schema schema = SchemaBuilder.builder().record("record").fields().requiredString("idFieldName").endRecord();
        IndexedRecord record = new GenericRecordBuilder(schema).set("idFieldName", 1).build();

        // Calling real methods.
        writer.open("random");
        writer.write(record);
        Result result = writer.close();

        Assert.assertEquals(1, result.totalCount);
        // TODO: We need to ask about increasing success count and reject count.
        // Verifying mock calls.
        Mockito.verify(connection, Mockito.times(1)).upsert(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(connection, Mockito.times(1)).increment();
        Mockito.verify(connection, Mockito.times(1)).decrement();
    }

    @Test
    public void testWriteWithNotOpenedWriter() throws IOException {
        thrown.expect(IOException.class);
        thrown.expectMessage(Matchers.equalTo("Writer is not opened"));

        writer.write(null);
    }

    @Test
    public void testWriteNullValue() throws IOException {
        // Prepare objects.
        CouchbaseConnection connection = Mockito.mock(CouchbaseConnection.class);
        Mockito.when(sink.getConnection()).thenReturn(connection);

        // Calling real methods.
        writer.open("random");
        writer.write(null);
        writer.close();

        // Verifying mock calls.
        Mockito.verify(connection, Mockito.never()).upsert(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(connection, Mockito.times(1)).increment();
        Mockito.verify(connection, Mockito.times(1)).decrement();
    }

    @Test
    public void testWriteWithoutIdField() throws IOException {
        thrown.expect(IOException.class);
        thrown.expectMessage("Schema does not contain ID field: ");
        // Prepare objects.
        CouchbaseConnection connection = Mockito.mock(CouchbaseConnection.class);
        Mockito.when(sink.getConnection()).thenReturn(connection);

        Schema schema = SchemaBuilder.builder().record("record").fields().requiredString("field").endRecord();
        IndexedRecord record = new GenericRecordBuilder(schema).set("field", 1).build();

        // Calling real methods.
        writer.open("random");
        writer.write(record);
    }

    @Test
    public void testGetWriteOperation() {
        Assert.assertEquals(writeOperation, writer.getWriteOperation());
    }

}
