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

import org.talend.components.api.facet.SimpleTransformationFacet;

import com.google.cloud.dataflow.sdk.transforms.ParDo;
import com.google.cloud.dataflow.sdk.values.PCollection;

public class SimpleTransformationRuntime implements FrameworkRuntime {

    SimpleTransformationFacet facet;

    PCollection<Map<String, Object>> outputMainRDD;

    /**
     * Execute a transformation with only a main flow compatible with the current Framework
     */
    public void genericEexcute(PCollection<Map<String, Object>> input) throws Exception {
        outputMainRDD = input.apply(ParDo.of(facet));
    }

    /**
     * Retrieve the main output for tor the current framework
     *
     * @return
     */
    public PCollection<Map<String, Object>> getMainOutput() {
        return outputMainRDD;
    }

}
