package org.talend.dataflow.mongo;

import java.util.Map;

import org.junit.Test;
import org.talend.components.engine.gdf.SimpleTransformationGDF;
import org.talend.components.engine.gdf.SimpleInputGDF;
import org.talend.components.engine.gdf.SimpleOutputGDF;
import org.talend.components.mongodb.tmongodbextract.MongoDBExtractRuntime;
import org.talend.components.mongodb.tmongodbinput.MongoDBInputRuntime;
import org.talend.components.mongodb.tmongodboutput.MongoDBOutputRuntime;

import com.google.cloud.dataflow.sdk.Pipeline;
import com.google.cloud.dataflow.sdk.options.PipelineOptions;
import com.google.cloud.dataflow.sdk.options.PipelineOptionsFactory;
import com.google.cloud.dataflow.sdk.runners.DirectPipelineRunner;
import com.google.cloud.dataflow.sdk.values.PCollection;
import com.google.cloud.dataflow.sdk.values.PCollectionTuple;
import com.mongodb.DBObject;

public class TestMongoDBInputV2 {

    /**
     * These tests are here only to prove that we can read from a MongoDB collection named "inputCollection" and write
     * into a collection named "outputCollection".
     *
     * to launch a mongo db with docker, run this docker run -p 27017:27017 --name comp-mongo -d mongo to open the mongo
     * shell on this db, please run this : docker run -it --link comp-mongo:mongo --rm mongo sh -c 'exec mongo
     * "$MONGO_PORT_27017_TCP_ADDR:$MONGO_PORT_27017_TCP_PORT/test"'
     *
     *
     * You must instantiate the database with the following commands:
     *
     * db.createCollection("inputCollection")
     * db.inputCollection.save({"test":{"hierarchical":{"name":"toto","value":3}}})
     *
     * You can run this command after the test is executed to check there is a new record in the DB:
     *
     * db.outputCollection.find({})
     *
     */
    @Test
    public void testWithInsert() throws Exception {
        // create GDF components
        SimpleInputGDF<DBObject> input = new SimpleInputGDF<>(new MongoDBInputRuntime());
        SimpleTransformationGDF<DBObject, Map<String, Object>, Map<String, Object>> extract = new SimpleTransformationGDF<>(
                new MongoDBExtractRuntime());
        SimpleOutputGDF<Map<String, Object>> output = new SimpleOutputGDF<>(new MongoDBOutputRuntime());

        // setup pipeline
        PipelineOptions options = PipelineOptionsFactory.create();
        Pipeline p = Pipeline.create(options);

        PCollection<DBObject> inputResult = input.generatePipeline(p);
        PCollectionTuple multiOutPCol = extract.generatePipeline(inputResult);

        // not used here currently, but allow me to test empty results
        PCollection<Map<String, Object>> rejectedResult = multiOutPCol.get(extract.errorTag);

        output.generatePipeline(multiOutPCol.get(extract.mainTag));

        DirectPipelineRunner.createForTest().run(p);
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
    public void testWithALog() throws Exception {
        // TransformTranslator.addTransformEvaluator(CassandraIO.Read.Bound.class, new
        // CassandraInputTransformEvaluator());
        // create GDF components
        SimpleInputGDF<DBObject> input = new SimpleInputGDF<>(new MongoDBInputRuntime());
        SimpleTransformationGDF<DBObject, Map<String, Object>, Map<String, Object>> extract = new SimpleTransformationGDF<>(
                new MongoDBExtractRuntime());
        SimpleOutputGDF<Map<String, Object>> output = new SimpleOutputGDF<>(new MongoDBOutputRuntime());

        // setup pipieline
        PipelineOptions options = PipelineOptionsFactory.create();
        Pipeline p = Pipeline.create(options);

        PCollection<DBObject> inputResult = input.generatePipeline(p);
        PCollectionTuple pColTuple = extract.generatePipeline(inputResult);
        output.generatePipeline(pColTuple.get(extract.mainTag));

        DirectPipelineRunner.createForTest().run(p);
    }

}