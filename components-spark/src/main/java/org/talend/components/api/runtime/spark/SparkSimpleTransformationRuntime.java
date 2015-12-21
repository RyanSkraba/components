package org.talend.components.api.runtime.spark;

import java.util.List;
import java.util.Map;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.talend.components.api.facet.SimpleTransformationFacet;
import org.talend.components.api.runtime.ReturnObject;
import org.talend.components.api.runtime.SimpleTransformationRuntime;

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
public class SparkSimpleTransformationRuntime implements SimpleTransformationRuntime<JavaRDD<Map<String, Object>>> {

    // TODO move this class out
    public class ExecuteSimple implements FlatMapFunction<Map<String, Object>, Map<String, Object>> {

        ReturnObject output = new ReturnObject();

        private SimpleTransformationFacet facet;

        public ExecuteSimple(SimpleTransformationFacet facet) {
            this.facet = facet;
        }

        @Override
        public List<Map<String, Object>> call(Map<String, Object> input) throws Exception {
            facet.execute(input, output);
            return output.getMainOutput();
        }
    }

    SimpleTransformationFacet facet;

    JavaRDD<Map<String, Object>> outputRDD;

    public SparkSimpleTransformationRuntime(SimpleTransformationFacet facet) {
        this.facet = facet;
    }

    @Override
    public void genericEexcute(JavaRDD<Map<String, Object>> inputs) throws Exception {
        outputRDD = inputs.flatMap(new ExecuteSimple(facet));
    }

    @Override
    public JavaRDD<Map<String, Object>> getMainOutput() {
        return outputRDD;
    }

}
