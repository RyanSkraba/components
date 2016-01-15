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
package org.talend.components.engine.gdf;

import java.io.Serializable;
import java.util.Map;

import org.talend.components.api.runtime.ExtractionRuntime;

import com.google.cloud.dataflow.sdk.coders.KvCoder;
import com.google.cloud.dataflow.sdk.coders.SerializableCoder;
import com.google.cloud.dataflow.sdk.transforms.DoFn;
import com.google.cloud.dataflow.sdk.transforms.ParDo;
import com.google.cloud.dataflow.sdk.values.KV;
import com.google.cloud.dataflow.sdk.values.PCollection;

public class SimpleExtractionGDF<InputType>  {

    ExtractionRuntime<InputType> facet;

    PCollection<Map<String, Object>> outputMainRDD;

    PCollection<Map<String, Object>> outputErrorRDD;

    // TODO move this class out
    public static class retrieveMain extends DoFn<KV<Boolean, Map<String, Object>>, Map<String, Object>> implements Serializable {

        @Override
        public void processElement(ProcessContext context)
                throws Exception {
            KV<Boolean, Map<String, Object>> input = context.element();
            if (input.getKey()) {
                context.output(input.getValue());
            }
        }
    }

    // TODO move this class out
    public static class retrieveError extends DoFn<KV<Boolean, Map<String, Object>>, Map<String, Object>> implements Serializable {

        @Override
        public void processElement(ProcessContext context)
                throws Exception {
            KV<Boolean, Map<String, Object>> input = context.element();
            if (!input.getKey()) {
                context.output(input.getValue());
            }
        }
    }

    public SimpleExtractionGDF(ExtractionRuntime<InputType> facet) {
        this.facet = facet;
    }

    /**
     * Execute a transformation with a main flow and a reject flow compatible with the current Framework
     *
     * @param inputs
     * @throws Exception
     */
    public void generatePipeline(PCollection<InputType> input) throws Exception {
        // TODO we cannot use the KryoCoder for a KVCoder.
        // Also we cannot use KryoCoder for the Key and for the Value, because the KryoCoder is going to kill the
        // inputStream.
        PCollection<KV<Boolean, Map<String, Object>>> outputRDD = input.apply(ParDo.of(facet)).setCoder(
                KvCoder.of(SerializableCoder.of(Boolean.class), KryoCoder.of()));
        outputMainRDD = outputRDD.apply(ParDo.of(new retrieveMain())).setCoder(KryoCoder.of());
        outputErrorRDD = outputRDD.apply(ParDo.of(new retrieveError())).setCoder(KryoCoder.of());

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
