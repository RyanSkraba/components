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

import org.talend.components.api.runtime.SimpleInputRuntime;
import org.talend.components.api.runtime.SingleOutputConnector;

import com.google.cloud.dataflow.sdk.Pipeline;
import com.google.cloud.dataflow.sdk.coders.VoidCoder;
import com.google.cloud.dataflow.sdk.transforms.Create;
import com.google.cloud.dataflow.sdk.transforms.DoFn;
import com.google.cloud.dataflow.sdk.transforms.ParDo;
import com.google.cloud.dataflow.sdk.values.PCollection;

/**
 * Code to execute the component's facet. This can be used at runtime or design time as required.
 */
public class SimpleInputGDF<OutputObject> extends DoFn<Void, OutputObject> {

    /**
     * created by sgandon on 14 janv. 2016
     */
    private final class SingleOutputConnectorImpl implements SingleOutputConnector<OutputObject>, Serializable {

        private static final long serialVersionUID = -2807289784162087247L;

        @Override
        public void outputData(OutputObject out) {
            processContext.output(out);

        }
    }

    private static final long serialVersionUID = -6363022558571467775L;

    ProcessContext processContext;

    private SimpleInputRuntime<OutputObject> compFacet;

    public SimpleInputGDF(SimpleInputRuntime<OutputObject> compFacet) {
        this.compFacet = compFacet;
        compFacet.setOutputConnector(new SingleOutputConnectorImpl());
    }

    @Override
    public void startBundle(Context context) throws Exception {
        // TODO get the ComponentProperties from the context
        compFacet.setUp(null);
    }

    @Override
    public void processElement(ProcessContext processContext)
            throws Exception {
        this.processContext = processContext;
        compFacet.execute();
    }

    @Override
    public void finishBundle(Context context) throws Exception {
        compFacet.tearDown();
    }

    /**
     * add this component to the GDF pipeline
     *
     * @return
     */
    public PCollection<OutputObject> generatePipeline(Pipeline pipeline) {
        return pipeline.begin().apply(Create.of((Void) null)).setCoder(VoidCoder.of()).apply(ParDo.of(this))
                .setCoder(KryoCoder.of());
    }

}
