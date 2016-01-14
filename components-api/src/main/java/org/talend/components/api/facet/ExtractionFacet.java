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
import com.google.cloud.dataflow.sdk.values.KV;

/**
 * Code to execute the component's facet. This can be used at runtime or design time as required.
 */
public abstract class ExtractionFacet<InputType> extends DoFn<InputType, KV<Boolean, Map<String, Object>>>
        implements ComponentFacet {

    DoFn<InputType, KV<Boolean, Map<String, Object>>>.ProcessContext context;

    @Override
    public void processElement(DoFn<InputType, KV<Boolean, Map<String, Object>>>.ProcessContext context) throws Exception {
        this.context = context;
        InputType input = context.element();
        execute(input);
    }

    public void addToMainOutput(Map<String, Object> output) {
        this.context.output(KV.of(true, output));
    }

    public void addToErrorOutput(Map<String, Object> output) {
        this.context.output(KV.of(false, output));
    }

    /**
     * Apply a transformation on the input value and put the result into the return object
     *
     * @param inputValue Input field that will be processed.
     * @param returnObject Object that know how to correctly return the current object for any runtime
     * @throws Exception
     */
    public abstract void execute(InputType inputValue) throws Exception;

}
