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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.runtime.SimpleInputRuntime;
import org.talend.components.api.runtime.SingleOutputConnector;
import org.talend.components.mongodb.DBObjectIndexedRecordWrapper;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class MongoDBInputRuntime implements SimpleInputRuntime<DBObjectIndexedRecordWrapper> {

    private static final long serialVersionUID = 8345765264712176890L;

    private static final Logger LOG = LoggerFactory.getLogger(MongoDBInputRuntime.class);

    private MongoClient mongo = null;

    private DB db = null;

    // TODO: these must come from component properties. to delete.

    public static String HOST = "localhost";

    public static int PORT = 27017;

    public static String DB_NAME = "test";

    public static String DB_COLLECTION = "inputCollection";

    public static String QUERY = "{}";

    @Override
    public void setUp(ComponentProperties props) {
        MongoClientOptions clientOptions = new MongoClientOptions.Builder().build();
        List<MongoCredential> mongoCredentialList = new ArrayList<MongoCredential>();
        ServerAddress serverAddress = new ServerAddress(HOST, PORT);
        mongo = new MongoClient(serverAddress, mongoCredentialList, clientOptions);
        this.db = mongo.getDB(DB_NAME);
    }

    @Override
    public void execute(SingleOutputConnector<DBObjectIndexedRecordWrapper> soc) throws Exception {
        DBCollection coll = db.getCollection(DB_COLLECTION);
        com.mongodb.DBObject myQuery = (com.mongodb.DBObject) com.mongodb.util.JSON.parse(QUERY);
        com.mongodb.DBObject fields = new com.mongodb.BasicDBObject();
        DBCursor cursor = coll.find(myQuery, fields);
        while (cursor.hasNext()) {
            soc.outputMainData(new DBObjectIndexedRecordWrapper(cursor.next()));
        }
    }

    @Override
    public void tearDown() {
        if (mongo != null) {
            mongo.close();
        }
    }
}
