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

import com.google.cloud.dataflow.sdk.io.Sink;
import com.google.cloud.dataflow.sdk.options.PipelineOptions;

public abstract class SimpleOutputWriteOperation<OutputObject, Statistics> extends Sink.WriteOperation<OutputObject, Statistics> {

    private final SimpleOutputFacet<OutputObject> sink;

    /**
     * Returns a DatastoreReader with Source and Datastore object set.
     *
     * @param datastore a datastore connection to use.
     */
    public SimpleOutputWriteOperation(SimpleOutputFacet<OutputObject> sink) {
        // TODO Handle properties
        this.sink = sink;
    }

    @Override
    public SimpleOutputFacet<OutputObject> getSink() {
        return this.sink;
    }

    @Override
    public void initialize(PipelineOptions options) throws Exception {
        // Nothing
    }
}