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

import java.util.Map;

import com.google.cloud.dataflow.sdk.transforms.DoFn;

/**
 * Code to execute the component's facet. This can be used at runtime or design time as required.
 */
public abstract class SimpleOutputFacetV2 extends DoFn<Map<String, Object>, Void> implements ComponentFacet {

    @Override
    public void startBundle(DoFn<Map<String, Object>, Void>.Context context) throws Exception {
        // TODO pass only the properties
        setUp(context);
    }

    // TODO pass only the properties
    public abstract void setUp(DoFn<Map<String, Object>, Void>.Context context);

    @Override
    public void processElement(
            com.google.cloud.dataflow.sdk.transforms.DoFn<Map<String, Object>, Void>.ProcessContext processContext)
            throws Exception {
        Map<String, Object> input = processContext.element();
        execute(input);
    }

    /**
     * Apply a transformation on the input value and put the result into the return object
     *
     * @param inputValue Input field that will be processed.
     * @param returnObject Object that know how to correctly return the current object for any runtime
     * @throws Exception
     */
    public abstract void execute(Map<String, Object> inputValue) throws Exception;

    @Override
    public void finishBundle(DoFn<Map<String, Object>, Void>.Context context) throws Exception {
        // TODO pass only the properties
        tearDown(context);
    }

    // TODO pass only the properties
    public abstract void tearDown(DoFn<Map<String, Object>, Void>.Context context);
}
