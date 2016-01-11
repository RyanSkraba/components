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
package org.talend.components.mongodb.tmongodboutput;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.facet.SimpleOutputFacet;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.cloud.dataflow.sdk.options.PipelineOptions;

// TODO slice the component into a write component and an output compoenent
public class MongoDBOutputFacet extends SimpleOutputFacet<Map<String, Object>> {

    private static final Logger LOG = LoggerFactory.getLogger(MongoDBOutputFacet.class);

    public MongoDBOutputFacet() {
        // TODO get properties
    }

    @Override
    public com.google.cloud.dataflow.sdk.io.Sink.WriteOperation<Map<String, Object>, String> createWriteOperation(
            PipelineOptions options) {
        return new MongoDBOutputWriteOperation(this);
    }

    @Override
    public void validate(PipelineOptions options) {
        Preconditions.checkNotNull("Replace this string by a condition you want to test", "Error message.");

    }

}
