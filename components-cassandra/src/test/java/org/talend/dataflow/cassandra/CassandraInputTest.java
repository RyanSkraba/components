package org.talend.dataflow.cassandra;

import com.cloudera.dataflow.spark.SparkPipelineOptions;
import com.cloudera.dataflow.spark.SparkPipelineOptionsFactory;
import com.cloudera.dataflow.spark.SparkPipelineRunner;
import com.cloudera.dataflow.spark.TransformTranslator;
import com.google.cloud.dataflow.sdk.Pipeline;
import com.google.cloud.dataflow.sdk.io.TextIO;
import org.junit.Test;
import org.talend.components.cassandra.io.dataflow.inputType1.CassandraIO;
import org.talend.components.cassandra.io.dataflow.inputType1.CassandraInputTransformEvaluator;
import org.talend.components.cassandra.tCassandraInput.tCassandraInputDIProperties;
import org.talend.components.cassandra.tCassandraInput.tCassandraInputSparkProperties;

/**
 * Created by bchen on 16-1-9.
 */
public class CassandraInputTest {

    @Test
    public void testType1() {
        TransformTranslator.addTransformEvaluator(CassandraIO.Read.Bound.class, new CassandraInputTransformEvaluator());
        SparkPipelineOptions options = SparkPipelineOptionsFactory.create();
        Pipeline p = Pipeline.create(options);
        tCassandraInputSparkProperties properties = new tCassandraInputSparkProperties("tCassandraInput_1");
        properties.init();
        properties.keySpace.setValue("ks");
        properties.columnFamily.setValue("test");
        p.apply(CassandraIO.Read.named("ReadCassandraRow").from(properties)).apply(TextIO.Write.named("WriteDone").to("/tmp/out"));
        SparkPipelineRunner.create().run(p);
    }

    @Test
    public void testType2() {
        TransformTranslator.addTransformEvaluator(org.talend.components.cassandra.io.dataflow.inputType2.CassandraIO.Read.Bound.class, new org.talend.components.cassandra.io.dataflow.inputType2.CassandraInputTransformEvaluator());
        SparkPipelineOptions options = SparkPipelineOptionsFactory.create();
        Pipeline p = Pipeline.create(options);
        tCassandraInputDIProperties properties = new tCassandraInputDIProperties("tCassandraInput_1");
        properties.init();
        properties.host.setValue("localhost");
        properties.port.setValue("9042");
        properties.useAuth.setValue(false);
        properties.query.setValue("select name from ks.test");
        p.apply(org.talend.components.cassandra.io.dataflow.inputType2.CassandraIO.Read.named("ReadCassandraRow").from(properties)).apply(TextIO.Write.named("WriteDone").to("/tmp/out"));
        SparkPipelineRunner.create().run(p);
    }


}
