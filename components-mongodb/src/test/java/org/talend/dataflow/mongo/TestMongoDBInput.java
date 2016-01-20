package org.talend.dataflow.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.UUID;

import org.apache.avro.generic.IndexedRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.engine.gdf.SimpleInputGDF;
import org.talend.components.engine.gdf.SimpleOutputGDF;
import org.talend.components.mongodb.DBObjectIndexedRecordWrapper;
import org.talend.components.mongodb.tmongodbinput.MongoDBInputRuntime;
import org.talend.components.mongodb.tmongodboutput.MongoDBOutputRuntime;

import com.google.cloud.dataflow.sdk.Pipeline;
import com.google.cloud.dataflow.sdk.options.PipelineOptions;
import com.google.cloud.dataflow.sdk.options.PipelineOptionsFactory;
import com.google.cloud.dataflow.sdk.runners.DirectPipelineRunner;
import com.google.cloud.dataflow.sdk.values.PCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;

public class TestMongoDBInput {

    private MongodForTestsFactory factory = null;

    private DB db;

    private int port;

    private String dbName;

    /** Takes about a quarter second to setup and run a simple embedded MongoDB unit test. */
    @Before
    public void setup() throws IOException {
        factory = MongodForTestsFactory.with(Version.Main.V3_2);
        MongoClient mongo = factory.newMongo();
        port = mongo.getAddress().getPort();
        dbName = "test-" + UUID.randomUUID();
        db = mongo.getDB(dbName);
    }

    @After
    public void teardown() {
        if (factory != null)
            factory.shutdown();

    }

    /** A basic test just for showing how to use the embedded mongo db. */
    @Test
    public void testEmbeddedMongoDB() {
        // Create the collection and insert an object
        DBCollection col = db.createCollection("inputCollection", new BasicDBObject());
        col.insert(new BasicDBObject("test", //
                new BasicDBObject("hierarchical", //
                        new BasicDBObject().append("name", "toto").append("value", 3))));

        // Get all of the objects in the collection.
        DBCursor cursor = col.find();
        DBObject dbo = cursor.next();
        assertTrue(dbo.containsField("_id"));
        assertEquals(dbo.get("test").toString(), "{ \"hierarchical\" : { \"name\" : \"toto\" , \"value\" : 3}}");
        assertTrue(!cursor.hasNext());
    }

    /**
     * A basic test for MongoDBInputRuntime running in a DirectPipelineRunner.
     */
    @Test
    public void testMongoDBInputRuntime() throws Exception {

        // Create the collection and insert an object
        DBCollection col = db.createCollection("inputCollection", new BasicDBObject());
        col.insert(new BasicDBObject("id", "12345").append("test", //
                new BasicDBObject("hierarchical", //
                        new BasicDBObject().append("name", "toto").append("value", 3))));

        // create GDF components TODO: get config from properties.
        MongoDBInputRuntime.HOST = "localhost";
        MongoDBInputRuntime.PORT = port;
        MongoDBInputRuntime.DB_NAME = dbName;

        MongoDBOutputRuntime.HOST = MongoDBInputRuntime.HOST;
        MongoDBOutputRuntime.PORT = MongoDBInputRuntime.PORT;
        MongoDBOutputRuntime.DB_NAME = MongoDBInputRuntime.DB_NAME;

        // Join the two components.
        SimpleInputGDF<? extends IndexedRecord> input = new SimpleInputGDF<>(new MongoDBInputRuntime());

        SimpleOutputGDF output = new SimpleOutputGDF(new MongoDBOutputRuntime());

        PipelineOptions options = PipelineOptionsFactory.create();
        Pipeline p = Pipeline.create(options);

        PCollection<? extends IndexedRecord> inputResult = input.generatePipeline(p);
        output.generatePipeline(inputResult);

        DirectPipelineRunner.createForTest().run(p);

        // Go directly to the embedded mongodb to check the output.
        DBCollection actual = db.getCollection(MongoDBOutputRuntime.DB_COLLECTION);
        DBCursor cursor = actual.find();
        DBObject dbo = cursor.next();
        assertTrue(dbo.containsField("_id"));
        assertEquals("{ \"outputcolumn\" : \"12345\"}", dbo.get("simplepath").toString());
        assertTrue(!cursor.hasNext());
    }

    /**
     * These tests are here only to prove that we can read from a MongoDB collection named "inputCollection" and write
     * into a collection named "outputCollection".
     *
     * You must instantiate the database with the following commands:
     *
     * db.createCollection("inputCollection")
     * db.inputCollection.save({"test":{"hierarchical":{"name":"toto","value":3}}})
     *
     * You can after read the output on the shell
     *
     */
    @Test
    @Ignore("Not yet implemented.")
    public void testWithALog() throws Exception {
        // TransformTranslator.addTransformEvaluator(CassandraIO.Read.Bound.class, new
        // CassandraInputTransformEvaluator());
        // create GDF components
        SimpleInputGDF<DBObjectIndexedRecordWrapper> input = new SimpleInputGDF<>(new MongoDBInputRuntime());
        // SimpleTransformationGDF<DBObject, Map<String, Object>, Map<String, Object>> extract = new
        // SimpleTransformationGDF<>(
        // new MongoDBExtractRuntime());
        // SimpleOutputGDF output = new SimpleOutputGDF(new LogRowFacet());
        //
        // // setup pipieline
        // PipelineOptions options = PipelineOptionsFactory.create();
        // Pipeline p = Pipeline.create(options);
        //
        // PCollection<DBObject> inputResult = input.generatePipeline(p);
        // PCollectionTuple pColTuple = extract.generatePipeline(inputResult);
        // output.generatePipeline(pColTuple.get(extract.mainTag));
        //
        // DirectPipelineRunner.createForTest().run(p);
    }

}