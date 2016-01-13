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
package org.talend.components.mongodb.tmongodboutputv2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.facet.SimpleOutputFacetV2;

import com.google.cloud.dataflow.sdk.transforms.DoFn;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

// TODO slice the component into a write component and an output compoenent
public class MongoDBOutputFacet extends SimpleOutputFacetV2 {

    private static final Logger LOG = LoggerFactory.getLogger(MongoDBOutputFacet.class);

    private MongoClient mongo = null;

    private DB db = null;

    @Override
    public void setUp(DoFn<Map<String, Object>, Void>.Context context) {
        MongoClientOptions clientOptions = new MongoClientOptions.Builder().build();
        List<MongoCredential> mongoCredentialList = new ArrayList<MongoCredential>();
        ServerAddress serverAddress = new ServerAddress("127.0.0.1", 27017);
        mongo = new MongoClient(serverAddress, mongoCredentialList, clientOptions);
        this.db = mongo.getDB("test");
    }

    @Override
    public void execute(Map<String, Object> inputValue) throws Exception {
        com.mongodb.DBCollection collection = db.getCollection("outputCollection");
        // initialize objects
        MongoDBOutputUtil updateObjectUtil = new MongoDBOutputUtil();
        updateObjectUtil.setObject(new com.mongodb.BasicDBObject());

        java.util.Map<String, String> pathMap = new java.util.HashMap<String, String>();

        // add parent path
        pathMap.put("defaultColumn", "simplepath");

        // create BasicDBObject
        updateObjectUtil.put(pathMap.get("defaultColumn"), "outputcolumn", "name");
        com.mongodb.BasicDBObject updateObj = updateObjectUtil.getObject();

        collection.insert(updateObj);
    }

    @Override
    public void tearDown(DoFn<Map<String, Object>, Void>.Context context) {
        if (mongo != null) {
            mongo.close();
        }
    }

}
