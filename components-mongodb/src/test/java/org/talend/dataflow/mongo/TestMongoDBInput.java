package org.talend.dataflow.mongo;

import java.util.Map;

import org.junit.Test;
import org.talend.components.api.runtime.ExtractionRuntime;
import org.talend.components.api.runtime.SimpleInputRuntime;
import org.talend.components.api.runtime.SimpleOutputRuntime;
import org.talend.components.mongodb.tmongodbextract.MongoDBExtractRuntime;
import org.talend.components.mongodb.tmongodbinput.MongoDBInputFacet;
import org.talend.components.mongodb.tmongodboutputv2.MongoDBOutputFacet;

import com.google.cloud.dataflow.sdk.Pipeline;
import com.google.cloud.dataflow.sdk.options.PipelineOptions;
import com.google.cloud.dataflow.sdk.options.PipelineOptionsFactory;
import com.google.cloud.dataflow.sdk.runners.DirectPipelineRunner;
import com.google.cloud.dataflow.sdk.values.PCollection;
import com.mongodb.DBObject;

/**
 * Created by bchen on 16-1-9.
 */
public class TestMongoDBInput {

    @Test
    public void testComplete() throws Exception {
        // TransformTranslator.addTransformEvaluator(CassandraIO.Read.Bound.class, new
        // CassandraInputTransformEvaluator());
        PipelineOptions options = PipelineOptionsFactory.create();
        Pipeline p = Pipeline.create(options);
        SimpleInputRuntime input = new SimpleInputRuntime<DBObject>(new MongoDBInputFacet());
        ExtractionRuntime<DBObject> extract = new ExtractionRuntime<DBObject>(new MongoDBExtractRuntime());
        SimpleOutputRuntime output = new SimpleOutputRuntime(new MongoDBOutputFacet());

        PCollection<DBObject> inputResult = input.execute(p);
        extract.execute(inputResult);
        PCollection<Map<String, Object>> extractedResult = extract.getMainOutput();

        // not used here currently, but allow me to test empty results
        PCollection<Map<String, Object>> rejectedResult = extract.getErrorOutput();

        output.excute(extractedResult);

        DirectPipelineRunner.createForTest().run(p);
    }

}