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
package org.talend.components.mongodb.tmongodbextract;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.facet.ExtractionFacet;
import org.talend.components.api.properties.ComponentProperties;

import com.mongodb.DBObject;

public class MongoDBExtractRuntime extends ExtractionFacet<DBObject> {

    private static final Logger LOG = LoggerFactory.getLogger(MongoDBExtractRuntime.class);

    private static final String API_VERSION = "34.0";

    public Object getValue(String parentNode, String currentName, com.mongodb.DBObject dbObject) {
        Object value = null;
        if (dbObject == null) {
            return null;
        }
        if (parentNode == null || "".equals(parentNode)) {
            if ("*".equals(currentName)) {
                value = dbObject;
            } else if (dbObject.get(currentName) != null) {
                value = dbObject.get(currentName);
            }
        } else {
            String objNames[] = objNames = parentNode.split("\\.");
            com.mongodb.DBObject currentObj = dbObject;
            for (String objName : objNames) {
                currentObj = (com.mongodb.DBObject) currentObj.get(objName);
                if (currentObj == null) {
                    break;
                }
            }
            if ("*".equals(currentName)) {
                value = currentObj;
            } else if (currentObj != null) {
                value = currentObj.get(currentName);
            }
        }
        return value;
    }

    @Override
    public void execute(DBObject input) throws Exception {
        System.out.println("input:" + input);
        String name = getValue("test.hierarchical", "name", input).toString();
        String value = getValue("test.hierarchical", "value", input).toString();
        String wholeJSON = getValue("", "*", input).toString();
        Map<String, Object> output = new HashMap<String, Object>();
        output.put("name", name);
        output.put("value", value);
        output.put("wholeJSON", wholeJSON);
        if (name == null || value == null || wholeJSON == null) {
            Map<String, Object> error = new HashMap<String, Object>();
            error.put("errorMsg", "The input JSON is invalid");
            error.put("inputValue", input);
            this.addToErrorOutput(error);
        } else {
            this.addToMainOutput(output);
        }
    }

    @Override
    public void tearDown() {
        // Nothing
    }

    @Override
    public void setUp(ComponentProperties context) {
        // TODO Auto-generated method stub

    }
}
