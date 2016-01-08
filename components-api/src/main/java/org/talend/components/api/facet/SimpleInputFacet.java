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

import java.util.List;

import com.google.cloud.dataflow.sdk.coders.Coder;
import com.google.cloud.dataflow.sdk.io.BoundedSource;
import com.google.cloud.dataflow.sdk.options.PipelineOptions;
import com.google.common.collect.ImmutableList;

/**
 * Code to execute the component's facet. This can be used at runtime or design time as required.
 */
public abstract class SimpleInputFacet<InputObject> extends BoundedSource<InputObject> implements ComponentFacet {

    // TODO set as abstract an be overrided by the component implementation
    @Override
    public long getEstimatedSizeBytes(PipelineOptions arg0) throws Exception {
        return 0;
    }

    @Override
    public boolean producesSortedKeys(PipelineOptions arg0) throws Exception {
        return false;
    }

    @Override
    public Coder<InputObject> getDefaultOutputCoder() {
        // TODO is this really working?
        // TODO please someone, test that.
        return KryoCoder.<InputObject> of();
    }

    // TODO set as abstract an be overrided by the component implementation
    @Override
    public List<? extends BoundedSource<InputObject>> splitIntoBundles(long arg0, PipelineOptions arg1) throws Exception {
        return ImmutableList.of(this);
    }

}
