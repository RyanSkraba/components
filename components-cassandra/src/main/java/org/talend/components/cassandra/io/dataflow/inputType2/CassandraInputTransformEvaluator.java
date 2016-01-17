/*
 * Copyright (c) 2015, Cloudera, Inc. All Rights Reserved.
 *
 * Cloudera, Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"). You may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */
package org.talend.components.cassandra.io.dataflow.inputType2;

import com.cloudera.dataflow.spark.EvaluationContext;
import com.cloudera.dataflow.spark.TransformEvaluator;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.talend.components.cassandra.io.bd.BDInputFormat;
import org.talend.row.BaseRowStruct;
import scala.Tuple2;

/**
 * Created by bchen on 16-1-9.
 */
public class CassandraInputTransformEvaluator implements TransformEvaluator<CassandraIO.Read.Bound<String>> {
    @Override
    public void evaluate(CassandraIO.Read.Bound<String> transform, EvaluationContext context) {
        JobConf job = new JobConf();
        job.set("input.source", "org.talend.components.cassandra.io.CassandraSource");
        job.set("input.props", transform.getProperties().toSerialized());
        JavaPairRDD<NullWritable, BaseRowStruct> pairRDD = context.getSparkContext().hadoopRDD(job, BDInputFormat.class, NullWritable.class, BaseRowStruct.class);
        JavaRDD<String> rdd = pairRDD.map(new Function<Tuple2<NullWritable, BaseRowStruct>, String>() {
            @Override
            public String call(Tuple2<NullWritable, BaseRowStruct> row) throws Exception {
                return row._2().get("name").toString();
            }
        });
        context.setOutputRDD(transform, rdd);
    }
}
