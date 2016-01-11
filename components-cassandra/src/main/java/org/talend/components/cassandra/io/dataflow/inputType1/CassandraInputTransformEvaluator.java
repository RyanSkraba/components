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
package org.talend.components.cassandra.io.dataflow.inputType1;

import com.cloudera.dataflow.spark.EvaluationContext;
import com.cloudera.dataflow.spark.TransformEvaluator;
import com.datastax.spark.connector.japi.CassandraJavaUtil;
import org.apache.spark.api.java.JavaRDD;

/**
 * Created by bchen on 16-1-9.
 */
public class CassandraInputTransformEvaluator implements TransformEvaluator<CassandraIO.Read.Bound<String>> {
    @Override
    public void evaluate(CassandraIO.Read.Bound<String> transform, EvaluationContext context) {
        JavaRDD<String> rdd = CassandraJavaUtil
                .javaFunctions(context.getSparkContext())
                .cassandraTable(transform.getProperties().keySpace.getStringValue(), transform.getProperties().columnFamily.getStringValue(),
                        CassandraJavaUtil.mapColumnTo(String.class))
                .select(CassandraJavaUtil.column("name"));//TODO learn how to init schema,then change it
        context.setOutputRDD(transform, rdd);
    }
}
