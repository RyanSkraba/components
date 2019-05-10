package org.talend.components.couchbase.runtime;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;

import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.hamcrest.Matchers;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.talend.components.api.component.runtime.Result;

import static org.junit.Assert.assertEquals;

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

        assertEquals(1, result.totalCount);
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
        assertEquals(writeOperation, writer.getWriteOperation());
    }

    @Test
    public void testWriterWithJson() throws IOException {
        // Prepare objects.
        CouchbaseConnection connection = Mockito.mock(CouchbaseConnection.class);
        Mockito.when(sink.getConnection()).thenReturn(connection);
        Mockito.when(sink.getContainsJson()).thenReturn(true);
        Mockito.when(writeOperation.getSink()).thenReturn(sink);
        writer = new CouchbaseWriter(writeOperation);

        Schema schema = SchemaBuilder.builder().record("record").fields().requiredString("idFieldName").endRecord();
        IndexedRecord record = new GenericRecordBuilder(schema).set("idFieldName", 1).build();

        // Calling real methods.
        writer.open("random");
        writer.write(record);
        Result result = writer.close();

        assertEquals(1, result.totalCount);
        // Verifying mock calls.
        Mockito.verify(connection, Mockito.times(1)).insertJsonDocument(Mockito.anyString(), Mockito.any());
        Mockito.verify(connection, Mockito.times(1)).increment();
        Mockito.verify(connection, Mockito.times(1)).decrement();
    }

    @Test
    public void testJsonCreationWithFlatSchema(){
        int idPos = 0;

        Schema schema = SchemaBuilder.builder().record("record").fields()
                .requiredString("id")
                .optionalBoolean("val_boolean")
                .optionalDouble("val_double")
                .optionalInt("val_int")
                .optionalString("val_str")
                .endRecord();
        IndexedRecord record = new GenericRecordBuilder(schema)
                .set("id", "id001")
                .set("val_boolean", true)
                .set("val_double", 200.2)
                .set("val_int", 100)
                .set("val_str", "str")
                .build();

        JsonObject expectedJsonObject = JsonObject.create();
        expectedJsonObject.put("val_boolean", true);
        expectedJsonObject.put("val_double", 200.2);
        expectedJsonObject.put("val_int", 100);
        expectedJsonObject.put("val_str", "str");

        assertEquals(expectedJsonObject, writer.createHierarchicalJson(schema, record, idPos));
    }

    @Test
    public void testJsonCreationWithMixedSchema(){
        int idPos = 0;
        String jsonSample = "{\"arrWithJSON\":[{\"arr01\":1,\"arr02\":2,\"arr03\":3},{\"arr01\":4," +
                "\"arr02\":5,\"arr03\":6}],\"id\":\"0017\",\"name\":\"Patrik\",\"numbers\":[\"one\",\"two\"," +
                "\"three\"],\"surname\":\"Human\",\"tel\":{\"home\":\"0342556644\",\"inner\":{\"val1\":40," +
                "\"val2\":20.2,\"val3\":true},\"mobile\":\"0989901515\",\"some\":\"444444\"}}";

        Schema schema = SchemaBuilder.builder().record("record").fields()
                .requiredString("id")
                .optionalString("val_str")
                .optionalString("val_str_like_json")
                .endRecord();
        IndexedRecord record = new GenericRecordBuilder(schema)
                .set("id", "id002")
                .set("val_str", "str")
                .set("val_str_like_json", jsonSample)
                .build();

        JsonObject expectedJsonObject = createStructuredJsonObject();

        assertEquals(expectedJsonObject, writer.createHierarchicalJson(schema, record, idPos));
    }

    @Test
    //case from Customer bug https://jira.talendforge.org/browse/TDI-42154
    public void testJsonCreationWithMixedSchemaAndSmallJson(){
        int idPos = 0;
        String jsonSample = "{\"newColumn\":\"hello\",\"newColumn1\":\"world\"}";

        Schema schema = SchemaBuilder.builder().record("record").fields()
                .requiredInt("newColumn")
                .optionalString("test")
                .endRecord();
        IndexedRecord record = new GenericRecordBuilder(schema)
                .set("newColumn", 1)
                .set("test", jsonSample)
                .build();

        JsonObject innerJsonObject = JsonObject.create();
        innerJsonObject.put("newColumn", "hello");
        innerJsonObject.put("newColumn1", "world");
        JsonObject expectedJsonObject = JsonObject.create();
        expectedJsonObject.put("test", innerJsonObject);

        assertEquals(expectedJsonObject, writer.createHierarchicalJson(schema, record, idPos));
    }

    private JsonObject createStructuredJsonObject(){
        JsonObject jsonObjectInnerInner = JsonObject.create();
        jsonObjectInnerInner.put("val1", 40);
        jsonObjectInnerInner.put("val2", 20.2);
        jsonObjectInnerInner.put("val3", true);

        JsonObject jsonObjectInner = JsonObject.create();
        jsonObjectInner.put("mobile", "0989901515");
        jsonObjectInner.put("home", "0342556644");
        jsonObjectInner.put("inner", jsonObjectInnerInner);
        jsonObjectInner.put("some", "444444");

        JsonObject jsonObjectOuterOuter = JsonObject.create();
        jsonObjectOuterOuter.put("id", "0017");
        jsonObjectOuterOuter.put("name", "Patrik");
        jsonObjectOuterOuter.put("surname", "Human");
        jsonObjectOuterOuter.put("tel" ,jsonObjectInner);

        JsonArray jsonArray = JsonArray.create();
        jsonArray.add("one");
        jsonArray.add("two");
        jsonArray.add("three");

        jsonObjectOuterOuter.put("numbers", jsonArray);

        JsonObject innerArray1 = JsonObject.create();
        innerArray1.put("arr01", 1);
        innerArray1.put("arr02", 2);
        innerArray1.put("arr03", 3);

        JsonObject innerArray2 = JsonObject.create();
        innerArray2.put("arr01", 4);
        innerArray2.put("arr02", 5);
        innerArray2.put("arr03", 6);

        JsonArray jsonArray1 = JsonArray.create();
        jsonArray1.add(innerArray1);
        jsonArray1.add(innerArray2);

        jsonObjectOuterOuter.put("arrWithJSON", jsonArray1);

        return JsonObject.create().put("val_str_like_json", jsonObjectOuterOuter).put("val_str", "str");
    }

}
