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

import org.talend.components.api.properties.ComponentProperties;

import com.google.cloud.dataflow.sdk.transforms.DoFn;

/**
 * Code to execute the component's facet. This can be used at runtime or design time as required.
 */
public abstract class SimpleTransformationRuntime extends DoFn<Map<String, Object>, Map<String, Object>> implements BaseRuntime {

    DoFn<Map<String, Object>, Map<String, Object>>.ProcessContext processContext;

    @Override
    public void processElement(DoFn<Map<String, Object>, Map<String, Object>>.ProcessContext processContext) throws Exception {
        this.processContext = processContext;
        Map<String, Object> input = processContext.element();
        execute(input);
    }

    /**
     * Apply a transformation on the input value and put the result into the return object
     *
     * @param inputValue Input field that will be processed.
     * @throws Exception
     */
    public abstract void execute(Map<String, Object> inputValue) throws Exception;

    public void addToMainOutput(Map<String, Object> output) {
        this.processContext.output(output);
    }

    /**
     * do nothing by default
     */
    @Override
    public void tearDown() {
        // Nothing
    }

    /**
     * do nothing by default
     */
    @Override
    public void setUp(ComponentProperties context) {
        // TODO Auto-generated method stub

    }

}
