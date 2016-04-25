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

import org.apache.avro.Schema;
import org.talend.daikon.avro.IndexedRecordAdapterFactory;

/**
 * created by ivan.honchar on Apr 25, 2016
 */
public class IssueAdapterFactory implements IndexedRecordAdapterFactory<String, IssueIndexedRecord>{
    
    Schema schema;

    @Override
    public Class<String> getDatumClass() {
        return String.class;
    }

    @Override
    public String convertToDatum(IssueIndexedRecord value) {
        return (String) value.get(0);
    }

    @Override
    public IssueIndexedRecord convertToAvro(String value) {
        return new IssueIndexedRecord(value);
    }

    @Override
    public Schema getSchema() {
        return schema;
    }

    @Override
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

}
