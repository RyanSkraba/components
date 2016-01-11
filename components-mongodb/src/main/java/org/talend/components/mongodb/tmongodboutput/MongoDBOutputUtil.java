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

/**
 * created by pbailly on 11 Jan 2016 Detailled comment
 *
 */
public class MongoDBOutputUtil {

    private com.mongodb.BasicDBObject object = null;

    // Put value to embedded document
    // If have no embedded document, put the value to root document
    public void put(String parentNode, String curentName, Object value) {
        if (parentNode == null || "".equals(parentNode)) {
            object.put(curentName, value);
        } else {
            String objNames[] = parentNode.split("\\.");
            com.mongodb.BasicDBObject lastNode = getParentNode(parentNode, objNames.length - 1);
            lastNode.put(curentName, value);
            com.mongodb.BasicDBObject parenttNode = null;
            for (int i = objNames.length - 1; i >= 0; i--) {
                parenttNode = getParentNode(parentNode, i - 1);
                parenttNode.put(objNames[i], lastNode);
                lastNode = (com.mongodb.BasicDBObject) parenttNode.clone();
            }
            object = lastNode;
        }
    }

    // Get node(embedded document) by path configuration
    public com.mongodb.BasicDBObject getParentNode(String parentNode, int index) {
        com.mongodb.BasicDBObject basicDBObject = object;
        if (parentNode == null || "".equals(parentNode)) {
            return object;
        } else {
            String objNames[] = parentNode.split("\\.");
            for (int i = 0; i <= index; i++) {
                basicDBObject = (com.mongodb.BasicDBObject) basicDBObject.get(objNames[i]);
                if (basicDBObject == null) {
                    basicDBObject = new com.mongodb.BasicDBObject();
                    return basicDBObject;
                }
                if (i == index) {
                    break;
                }
            }
            return basicDBObject;
        }
    }

    public void putkeyNode(String parentNode, String curentName, Object value) {
        if (parentNode == null || "".equals(parentNode) || ".".equals(parentNode)) {
            put(parentNode, curentName, value);
        } else {
            put("", parentNode + "." + curentName, value);
        }
    }

    public com.mongodb.BasicDBObject getObject() {
        return this.object;
    }

    public void setObject(com.mongodb.BasicDBObject object) {
        this.object = object;
    }

}
