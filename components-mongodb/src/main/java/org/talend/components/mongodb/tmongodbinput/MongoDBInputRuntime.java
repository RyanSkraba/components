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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.facet.SimpleInputFacet;
import org.talend.components.api.runtime.ReturnObject;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class MongoDBInputRuntime extends SimpleInputFacet {

    private static final Logger LOG = LoggerFactory.getLogger(MongoDBInputRuntime.class);

    private static final String API_VERSION = "34.0";

    MongoClient mongo = null;

    DB db = null;

    @Override
    public void connection() {
        MongoClientOptions clientOptions = new MongoClientOptions.Builder().build();
        List<MongoCredential> mongoCredentialList = new ArrayList<MongoCredential>();
        ServerAddress serverAddress = new ServerAddress("127.0.0.1", 27017);
        mongo = new MongoClient(serverAddress, mongoCredentialList, clientOptions);
        db = mongo.getDB("test");
    }

    @Override
    public void execute(ReturnObject returnObject) throws Exception {
        DBCollection coll = db.getCollection("testcollection");
        com.mongodb.DBObject myQuery = (com.mongodb.DBObject) com.mongodb.util.JSON.parse("{}");

        com.mongodb.DBObject fields = new com.mongodb.BasicDBObject();
        com.mongodb.DBCursor cursor = coll.find(myQuery, fields);

        while (cursor.hasNext()) {
            Map<String, Object> field = new HashMap<String, Object>();
            field.put("DBObject", cursor.next());
            returnObject.setMainOutput(field);
        }
    }

    @Override
    public void tearDown() {
        if (mongo != null) {
            mongo.close();
        }
    }
}
