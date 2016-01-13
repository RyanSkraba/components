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
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.facet.SimpleInputFacet;
import org.talend.components.api.facet.SimpleInputReader;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class MongoDBInputReader extends SimpleInputReader<DBObject> {

    private static final Logger LOG = LoggerFactory.getLogger(MongoDBInputReader.class);

    private static final String API_VERSION = "34.0";

    private MongoClient mongo = null;

    private DB db = null;

    private DBCursor cursor = null;

    private DBObject currentElement = null;

    public MongoDBInputReader(SimpleInputFacet source) {
        super(source);
    }

    @Override
    public boolean start() throws IOException {
        MongoClientOptions clientOptions = new MongoClientOptions.Builder().build();
        List<MongoCredential> mongoCredentialList = new ArrayList<MongoCredential>();
        ServerAddress serverAddress = new ServerAddress("127.0.0.1", 27017);
        mongo = new MongoClient(serverAddress, mongoCredentialList, clientOptions);
        db = mongo.getDB("test");
        DBCollection coll = db.getCollection("testCollection");
        com.mongodb.DBObject myQuery = (com.mongodb.DBObject) com.mongodb.util.JSON.parse("{}");

        com.mongodb.DBObject fields = new com.mongodb.BasicDBObject();
        cursor = coll.find(myQuery, fields);
        return this.advance();
    }

    @Override
    public boolean advance() throws IOException {
        if (cursor.hasNext()) {
            currentElement = cursor.next();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public DBObject getCurrent() {
        return currentElement;
    }

    @Override
    public void close() throws IOException {
        if (mongo != null) {
            mongo.close();
        }
    }
}
