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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.talend.components.api.facet.SimpleOutputFacet;
import org.talend.components.api.facet.SimpleOutputWriteOperation;

import com.google.cloud.dataflow.sdk.io.Sink.Writer;
import com.google.cloud.dataflow.sdk.options.PipelineOptions;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

// TODO slice the component into a write component and an output compoenent
public class MongoDBOutputWriteOperation extends SimpleOutputWriteOperation<Map<String, Object>, String> {

    MongoClient mongo = null;

    public MongoDBOutputWriteOperation(SimpleOutputFacet<Map<String, Object>> sink) {
        super(sink);
    }

    @Override
    public Writer<Map<String, Object>, String> createWriter(PipelineOptions options) throws Exception {
        MongoClientOptions clientOptions = new MongoClientOptions.Builder().build();
        List<MongoCredential> mongoCredentialList = new ArrayList<MongoCredential>();
        ServerAddress serverAddress = new ServerAddress("127.0.0.1", 27017);
        mongo = new MongoClient(serverAddress, mongoCredentialList, clientOptions);
        return new MongoDBOutputWriter(this, mongo);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.google.cloud.dataflow.sdk.io.Sink.WriteOperation#finalize(java.lang.Iterable,
     * com.google.cloud.dataflow.sdk.options.PipelineOptions)
     */
    @Override
    public void finalize(Iterable<String> writerResults, PipelineOptions options) throws Exception {
        if (mongo != null) {
            mongo.close();
        }

    }

}
