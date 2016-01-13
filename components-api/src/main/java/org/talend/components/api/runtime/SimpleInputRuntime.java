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

import org.talend.components.api.facet.SimpleInputFacet;

import com.google.cloud.dataflow.sdk.Pipeline;
import com.google.cloud.dataflow.sdk.io.Read;
import com.google.cloud.dataflow.sdk.values.PCollection;

public class SimpleInputRuntime<OutputObject> implements FrameworkRuntime {

    private SimpleInputFacet<OutputObject> facet;

    public SimpleInputRuntime(SimpleInputFacet<OutputObject> facet) {
        this.facet = facet;
    }

    /**
     * Retrieve the main output for tor the current framework
     *
     * @return
     */
    public PCollection<OutputObject> generatePipeline(Pipeline pipeline) {
        return pipeline.apply(Read.from(this.facet));
    }

}
