package org.talend.components.api.runtime.spark;

import java.util.Map;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.talend.components.api.facet.SimpleTransformationFacet;
import org.talend.components.api.runtime.RejectableTransformationRuntime;

import scala.Tuple2;

// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

/**
 * created by pbailly on 18 Dec 2015 Detailled comment
 *
 */
public class SparkRejectableTransformationRuntime implements RejectableTransformationRuntime<JavaRDD<Map<String, Object>>> {

    // TODO move this class out
    public class ExecuteWithRejectable implements PairFlatMapFunction<Map<String, Object>, Boolean, Map<String, Object>> {

        Tuple2ReturnObject output = new Tuple2ReturnObject();

        private SimpleTransformationFacet facet;

        public ExecuteWithRejectable(SimpleTransformationFacet facet) {
            this.facet = facet;
        }

        @Override
        public Iterable<Tuple2<Boolean, Map<String, Object>>> call(Map<String, Object> input) throws Exception {
            facet.execute(input, output);

            return output.getTupleOutput();
        }
    }

    // TODO move this class out
    public class retrieveMain implements Function<Tuple2<Boolean, Map<String, Object>>, Boolean> {

        @Override
        public Boolean call(Tuple2<Boolean, Map<String, Object>> input) throws Exception {
            return input._1();
        }
    }

    // TODO move this class out
    public class retrieveError implements Function<Tuple2<Boolean, Map<String, Object>>, Boolean> {

        @Override
        public Boolean call(Tuple2<Boolean, Map<String, Object>> input) throws Exception {
            return !input._1();
        }
    }

    SimpleTransformationFacet facet;

    JavaRDD<Map<String, Object>> outputMainRDD;

    JavaRDD<Map<String, Object>> outputErrorRDD;

    public SparkRejectableTransformationRuntime(SimpleTransformationFacet facet) {
        this.facet = facet;
    }

    @Override
    public void genericExecute(JavaRDD<Map<String, Object>> inputs) throws Exception {
        JavaPairRDD<Boolean, Map<String, Object>> outputRDD = inputs.flatMapToPair(new ExecuteWithRejectable(facet));
        outputMainRDD = outputRDD.filter(new retrieveMain()).values();
        outputErrorRDD = outputRDD.filter(new retrieveError()).values();
    }

    @Override
    public JavaRDD<Map<String, Object>> getMainOutput() {
        return outputMainRDD;
    }

    @Override
    public JavaRDD<Map<String, Object>> getErrorOutput() {
        return outputErrorRDD;
    }
}
