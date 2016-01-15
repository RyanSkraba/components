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

import org.talend.components.api.runtime.SimpleOutputRuntime;

import com.google.cloud.dataflow.sdk.transforms.DoFn;
import com.google.cloud.dataflow.sdk.transforms.ParDo;
import com.google.cloud.dataflow.sdk.values.PCollection;

/**
 * Code to execute the component's facet. This can be used at runtime or design time as required.
 */
public class SimpleOutputGDF<InputObject> extends DoFn<InputObject, Void> {

    private static final long serialVersionUID = 4551281004426190461L;

    private SimpleOutputRuntime<InputObject> delegate;

    /**
     * DOC sgandon SimpleOutputFacetV2 constructor comment.
     */
    public SimpleOutputGDF(SimpleOutputRuntime<InputObject> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void startBundle(Context context) throws Exception {
        // TODO pass only the properties
        delegate.setUp(null);
    }

    @Override
    public void processElement(ProcessContext processContext)
            throws Exception {
        InputObject input = processContext.element();
        delegate.execute(input);
    }

    @Override
    public void finishBundle(Context context) throws Exception {
        delegate.tearDown();
    }

    /**
     * Execute a transformation with only a main flow compatible with the current Framework
     */
    public void generatePipeline(PCollection<InputObject> input) throws Exception {
        input.apply(ParDo.of(this));
    }

}
