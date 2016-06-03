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
package org.talend.components.jira.testutils;

import java.util.LinkedList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;

/**
 * {@link IndexedRecord} implementation used for tests
 */
public class ListIndexedRecord implements IndexedRecord {

    private final Schema schema;
    
    private List<Object> fields;
    
    public ListIndexedRecord(Schema schema) {
        this.schema = schema;
        fields = new LinkedList<>();
    }
    
    @Override
    public Schema getSchema() {
        return schema;
    }

    @Override
    public void put(int index, Object value) {
        while (index >= fields.size()) {
            fields.add("");
        }
        fields.set(index, value);
    }

    @Override
    public Object get(int i) {
        return fields.get(i);
    }

}
