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
package org.talend.components.api.runtime;

import java.util.Map;

import org.talend.components.api.facet.RejectableTransformationFacet;

import com.google.cloud.dataflow.sdk.transforms.DoFn;
import com.google.cloud.dataflow.sdk.transforms.ParDo;
import com.google.cloud.dataflow.sdk.values.KV;
import com.google.cloud.dataflow.sdk.values.PCollection;

public class RejectableTransformationRuntime implements FrameworkRuntime {

    RejectableTransformationFacet facet;

    PCollection<Map<String, Object>> outputMainRDD;

    PCollection<Map<String, Object>> outputErrorRDD;

    // TODO move this class out
    public class retrieveMain extends DoFn<KV<Boolean, Map<String, Object>>, Map<String, Object>> {

        @Override
        public void processElement(DoFn<KV<Boolean, Map<String, Object>>, Map<String, Object>>.ProcessContext context)
                throws Exception {
            KV<Boolean, Map<String, Object>> input = context.element();
            if (input.getKey()) {
                context.output(input.getValue());
            }
        }
    }

    // TODO move this class out
    public class retrieveError extends DoFn<KV<Boolean, Map<String, Object>>, Map<String, Object>> {

        @Override
        public void processElement(DoFn<KV<Boolean, Map<String, Object>>, Map<String, Object>>.ProcessContext context)
                throws Exception {
            KV<Boolean, Map<String, Object>> input = context.element();
            if (!input.getKey()) {
                context.output(input.getValue());
            }
        }
    }

    public RejectableTransformationRuntime(RejectableTransformationFacet facet) {
        this.facet = facet;
    }

    /**
     * Execute a transformation with a main flow and a reject flow compatible with the current Framework
     *
     * @param inputs
     * @throws Exception
     */
    public void execute(PCollection<Map<String, Object>> input) throws Exception {
        PCollection<KV<Boolean, Map<String, Object>>> outputRDD = input.apply(ParDo.of(facet));
        outputMainRDD = outputRDD.apply(ParDo.of(new retrieveMain()));
        outputErrorRDD = outputRDD.apply(ParDo.of(new retrieveError()));

    }

    /**
     * Retrieve the main output for tor the current framework
     *
     * @return
     */
    public PCollection<Map<String, Object>> getMainOutput() {
        return outputMainRDD;

    }

    /**
     * Retrieve the error output for tor the current framework
     *
     * @return
     */
    public PCollection<Map<String, Object>> getErrorOutput() {
        return outputErrorRDD;

    }

}
