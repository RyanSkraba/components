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
import org.talend.components.api.facet.SimpleOutputWriter;

import com.google.cloud.dataflow.sdk.io.Sink.WriteOperation;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

// TODO slice the component into a write component and an output compoenent
public class MongoDBOutputWriter extends SimpleOutputWriter<Map<String, Object>, String> {

    private static final Logger LOG = LoggerFactory.getLogger(MongoDBOutputWriter.class);

    private static final String API_VERSION = "34.0";

    private WriteOperation<Map<String, Object>, String> writeOperation;

    private MongoClient mongo = null;

    private DB db = null;

    private DBCursor cursor = null;

    private DBObject currentElement = null;

    private String resultMessage = "";

    public MongoDBOutputWriter(WriteOperation<Map<String, Object>, String> writeOperation, MongoClient mongo) {
        this.writeOperation = writeOperation;
        this.mongo = mongo;
        this.db = mongo.getDB("dbTest");
    }

    @Override
    public WriteOperation<Map<String, Object>, String> getWriteOperation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void open(String uId) throws Exception {
        // this should not open the connection, it's the writeOperation work.
    }

    @Override
    public void write(Map<String, Object> value) throws Exception {
        com.mongodb.DBCollection coll = db.getCollection("testCollection");
        // initialize objects
        MongoDBOutputUtil updateObjectUtil = new MongoDBOutputUtil();
        updateObjectUtil.setObject(new com.mongodb.BasicDBObject());

        java.util.Map<String, String> pathMap = new java.util.HashMap<String, String>();

        // add parent path
        pathMap.put("defaultColumn", "simplepath");

        // create BasicDBObject
        updateObjectUtil.put(pathMap.get("defaultColumn"), "outputcolumn", "name");
        com.mongodb.BasicDBObject updateObj = updateObjectUtil.getObject();

        coll.insert(updateObj);

    }

    @Override
    public String close() throws Exception {
        // this should not close the connection, it's still the job of the writeOperation.

        // TODO Handle the output correctly
        // maybe we can create a generatic function to ask developper to return usefull data
        return "Everything is awesome!!!";
    }
}
