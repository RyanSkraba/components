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
public abstract class SimpleTransformationFacet extends DoFn<Map<String, Object>, Map<String, Object>> implements ComponentFacet {

    DoFn<Map<String, Object>, Map<String, Object>>.ProcessContext context;

    @Override
    public void processElement(DoFn<Map<String, Object>, Map<String, Object>>.ProcessContext context) throws Exception {
        this.context = context;
        Map<String, Object> input = context.element();
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

    // a transformation may use a tear down
    // TODO Wrap to stopBundle
    public abstract void tearDown();

    public void addToMainOutput(Map<String, Object> output) {
        this.context.output(output);
    }
}
