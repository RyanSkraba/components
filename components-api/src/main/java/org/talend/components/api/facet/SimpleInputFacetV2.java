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
package org.talend.components.api.facet;

import com.google.cloud.dataflow.sdk.transforms.DoFn;

/**
 * Code to execute the component's facet. This can be used at runtime or design time as required.
 */
public abstract class SimpleInputFacetV2<OutputObject> extends DoFn<Void, OutputObject> implements ComponentFacet {

    com.google.cloud.dataflow.sdk.transforms.DoFn<Void, OutputObject>.ProcessContext processContext;

    @Override
    public void startBundle(DoFn<Void, OutputObject>.Context context) throws Exception {
        // TODO pass only the properties
        setUp(context);
    }

    // TODO pass only the properties
    public abstract void setUp(DoFn<Void, OutputObject>.Context context);

    @Override
    public void processElement(com.google.cloud.dataflow.sdk.transforms.DoFn<Void, OutputObject>.ProcessContext processContext)
            throws Exception {
        this.processContext = processContext;
        execute();
    }

    /**
     * Apply a transformation on the input value and put the result into the return object
     *
     * @param inputValue Input field that will be processed.
     * @param returnObject Object that know how to correctly return the current object for any runtime
     * @throws Exception
     */
    public abstract void execute() throws Exception;

    public void addToMainOutput(OutputObject output) {
        this.processContext.output(output);
    }

    @Override
    public void finishBundle(DoFn<Void, OutputObject>.Context context) throws Exception {
        // TODO pass only the properties
        tearDown(context);
    }

    // TODO pass only the properties
    public abstract void tearDown(DoFn<Void, OutputObject>.Context context);

}
