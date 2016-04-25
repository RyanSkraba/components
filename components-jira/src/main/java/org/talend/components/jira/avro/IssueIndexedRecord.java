// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.jira.avro;

import java.util.Collections;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field.Order;
import org.apache.avro.generic.IndexedRecord;

/**
 * created by ivan.honchar on Apr 25, 2016
 */
public class IssueIndexedRecord implements IndexedRecord{
    
    /**
     * Jira JSON string
     */
    private String jsonString;
    
    public IssueIndexedRecord(String json) {
        this.jsonString = json;
    }

    @Override
    public Schema getSchema() {
        Schema.Field jsonField = new Schema.Field("json", Schema.create(Schema.Type.STRING), null, null, Order.ASCENDING);
        return Schema.createRecord("issue", null, null, false, Collections.singletonList(jsonField));
    }

    /**
     * TODO implement it correctly
     */
    @Override
    public void put(int i, Object json) {
        if(i != 0) {
            throw new IndexOutOfBoundsException("index argument should be 0");
        }
        if(json instanceof String )
        {
            jsonString = (String) json;
        }
    }

    /**
     * TODO implement it correctly
     */
    @Override
    public Object get(int i) {
        if(i != 0) {
            throw new IndexOutOfBoundsException("index argument should be 0");
        }
        return jsonString;
    }

}
