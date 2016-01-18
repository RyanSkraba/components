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

import org.talend.components.api.runtime.DoubleOutputConnector;
import org.talend.components.api.runtime.TransformationRuntime;

import com.google.cloud.dataflow.sdk.transforms.DoFn;
import com.google.cloud.dataflow.sdk.transforms.ParDo;
import com.google.cloud.dataflow.sdk.values.PCollection;
import com.google.cloud.dataflow.sdk.values.PCollectionTuple;
import com.google.cloud.dataflow.sdk.values.TupleTag;
import com.google.cloud.dataflow.sdk.values.TupleTagList;

public class SimpleTransformationGDF<InputType, OutputMain, OutputError> extends DoFn<InputType, OutputMain> {

    private static final long serialVersionUID = 3173778597329077341L;

    TransformationRuntime<InputType, OutputMain, OutputError> facet;

    private TransformationRuntime<InputType, OutputMain, OutputError> runtimeImpl;

    private ProcessContext context;

    public TupleTag<OutputMain> mainTag = new TupleTag<>();

    public TupleTag<OutputError> errorTag = new TupleTag<>();

    private DoubleOutputGdfImpl outputsConnector;

    /**
     * created by sgandon on 18 janv. 2016
     */
    private final class DoubleOutputGdfImpl implements DoubleOutputConnector<OutputMain, OutputError>, Serializable {

        @Override
        public void outputMainData(OutputMain out) {
            context.output(out);
        }

        @Override
        public void outputErrorData(OutputError out) {
            context.sideOutput(errorTag, out);
        }
    }

    public SimpleTransformationGDF(TransformationRuntime<InputType, OutputMain, OutputError> runtimeImpl) {
        this.runtimeImpl = runtimeImpl;
        outputsConnector = new DoubleOutputGdfImpl();
    }

    /**
     * Execute a transformation with a main flow and a reject flow compatible with the current Framework
     *
     * @param inputs
     * @return
     * @throws Exception
     */
    public PCollectionTuple generatePipeline(PCollection<InputType> input) throws Exception {
        PCollectionTuple pColTup = input.apply(ParDo.withOutputTags(mainTag, TupleTagList.of(errorTag)).of(this));
        pColTup.get(mainTag).setCoder(KryoCoder.of());
        pColTup.get(errorTag).setCoder(KryoCoder.of());
        return pColTup;
    }

    @Override
    public void processElement(DoFn<InputType, OutputMain>.ProcessContext context) throws Exception {
        this.context = context;
        InputType input = context.element();
        runtimeImpl.execute(input, outputsConnector);
    }

}
