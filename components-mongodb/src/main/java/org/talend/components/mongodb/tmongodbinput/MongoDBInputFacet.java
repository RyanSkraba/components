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
package org.talend.components.mongodb.tmongodbinput;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.facet.SimpleInputFacet;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.cloud.dataflow.sdk.options.PipelineOptions;
import com.mongodb.DBObject;

public class MongoDBInputFacet extends SimpleInputFacet<DBObject> {

    private static final Logger LOG = LoggerFactory.getLogger(MongoDBInputFacet.class);

    public MongoDBInputFacet() {
        // TODO get properties
    }

    @Override
    public com.google.cloud.dataflow.sdk.io.BoundedSource.BoundedReader<DBObject> createReader(PipelineOptions options)
            throws IOException {
        return new MongoDBInputReader(this);
    }

    @Override
    public void validate() {
        Preconditions.checkNotNull("Replace this string by a condition you want to test", "Error message.");

    }
}
