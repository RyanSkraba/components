package org.talend.components.couchbase.runtime;

import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.couchbase.input.CouchbaseInputProperties;

public class CouchbaseSourceTest {

    private CouchbaseSource source;

    private Schema schema;

    @Before
    public void setup() {
        schema = SchemaBuilder.builder().record("record").fields().endRecord();

        CouchbaseInputProperties properties = new CouchbaseInputProperties("input");
        properties.bootstrapNodes.setValue("testNode");
        properties.bucket.setValue("testBucket");
        properties.password.setValue("password");

        properties.schema.schema.setValue(schema);
        source = new CouchbaseSource();
        source.initialize(null, properties);
    }

    @Test
    public void testCreateReader() {
        Reader<IndexedRecord> reader = source.createReader(null);

        Assert.assertTrue(reader instanceof CouchbaseReader);
    }

    @Test
    public void testGetSchema() {
        Assert.assertEquals(schema, source.getSchema());
    }

    @Test
    public void testGetSchemaNames() throws IOException {
        Assert.assertNull(source.getSchemaNames(null));
    }

    @Test
    public void testGetEndpointSchema() throws IOException {
        Assert.assertNull(source.getEndpointSchema(null, null));
    }
}
