package org.talend.dataflow.mongo;

import java.util.Map;

import org.junit.Test;
import org.talend.components.api.runtime.ExtractionRuntime;
import org.talend.components.api.runtime.SimpleInputRuntime;
import org.talend.components.api.runtime.SimpleOutputRuntime;
import org.talend.components.mongodb.tmongodbextract.MongoDBExtractRuntime;
import org.talend.components.mongodb.tmongodbinput.MongoDBInputFacet;
import org.talend.components.mongodb.tmongodboutputv2.MongoDBOutputFacet;
import org.talend.components.output.tlogrow.LogRowFacet;

import com.google.cloud.dataflow.sdk.Pipeline;
import com.google.cloud.dataflow.sdk.options.PipelineOptions;
import com.google.cloud.dataflow.sdk.options.PipelineOptionsFactory;
import com.google.cloud.dataflow.sdk.runners.DirectPipelineRunner;
import com.google.cloud.dataflow.sdk.values.PCollection;
import com.mongodb.DBObject;

public class TestMongoDBInput {

    /**
     * These tests are here only to prove that we can read from a MongoDB collection named "inputCollection" and write
     * into a collection named "outputCollection".
     *
     * You must instantiate the database with the following commands:
     *
     * db.createCollection("inputCollection")
     * db.inputCollection.save({"test":{"hierarchical":{"name":"toto","value":3}}})
     *
     * You can after read the output with:
     *
     * db.outputCollection.find({})
     *
     */
    @Test
    public void testWithInsert() throws Exception {
        // TransformTranslator.addTransformEvaluator(CassandraIO.Read.Bound.class, new
        // CassandraInputTransformEvaluator());
        PipelineOptions options = PipelineOptionsFactory.create();
        Pipeline p = Pipeline.create(options);
        SimpleInputRuntime input = new SimpleInputRuntime<DBObject>(new MongoDBInputFacet());
        ExtractionRuntime<DBObject> extract = new ExtractionRuntime<DBObject>(new MongoDBExtractRuntime());
        SimpleOutputRuntime output = new SimpleOutputRuntime(new MongoDBOutputFacet());

        PCollection<DBObject> inputResult = input.generatePipeline(p);

        extract.generatePipeline(inputResult);
        PCollection<Map<String, Object>> extractedResult = extract.getMainOutput();

        // not used here currently, but allow me to test empty results
        PCollection<Map<String, Object>> rejectedResult = extract.getErrorOutput();

        output.generatePipeline(extractedResult);

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
        PipelineOptions options = PipelineOptionsFactory.create();
        Pipeline p = Pipeline.create(options);
        SimpleInputRuntime input = new SimpleInputRuntime<DBObject>(new MongoDBInputFacet());
        ExtractionRuntime<DBObject> extract = new ExtractionRuntime<DBObject>(new MongoDBExtractRuntime());
        SimpleOutputRuntime output = new SimpleOutputRuntime(new LogRowFacet());

        PCollection<DBObject> inputResult = input.generatePipeline(p);

        extract.generatePipeline(inputResult);
        PCollection<Map<String, Object>> extractedResult = extract.getMainOutput();

        output.generatePipeline(extractedResult);

        DirectPipelineRunner.createForTest().run(p);
    }

}