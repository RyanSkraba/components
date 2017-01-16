// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;

/**
 * Jira {@link IndexedRecord}
 */
public class IssueIndexedRecord implements IndexedRecord {

    /**
     * Avro schema of this {@link IndexedRecord}
     */
    private final Schema schema;

    /**
     * Jira JSON string
     */
    private String jsonString;

    /**
     * Constructor sets JSON data and schema of this record
     * 
     * @param json JSON data
     * @param schema schema of this record
     */
    public IssueIndexedRecord(String json, Schema schema) {
        this.jsonString = json;
        this.schema = schema;
    }

    /**
     * Returns schema of this {@link IndexedRecord}
     * 
     * @return schema of this {@link IndexedRecord}
     */
    @Override
    public Schema getSchema() {
        return schema;
    }

    /**
     * Puts JSON data to this record in the 1st column.
     * Index parameter value could be only 0
     * 
     * @param index index of column to put field value
     * @param json data
     */
    @Override
    public void put(int index, Object json) {
        if (index != 0) {
            throw new IndexOutOfBoundsException("index argument should be 0");
        }
        if (json instanceof String) {
            jsonString = (String) json;
        }
    }

    /**
     * Returns field value of specified by index parameter column
     * Index parameter value could be only 0
     * 
     * @param index index of column to get field from
     * @return field value
     */
    @Override
    public Object get(int index) {
        if (index != 0) {
            throw new IndexOutOfBoundsException("index argument should be 0");
        }
        return jsonString;
    }

}
