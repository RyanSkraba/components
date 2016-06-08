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
import org.talend.daikon.avro.converter.IndexedRecordConverter;

/**
 * Jira {@link IndexedRecordAdapterFactory}
 */
public class IssueAdapterFactory implements IndexedRecordConverter<String, IssueIndexedRecord>{
    
    /**
     * Data schema. Only static schema with 1 String column is allowed
     */
    Schema schema;   

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<String> getDatumClass() {
        return String.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String convertToDatum(IssueIndexedRecord value) {
        return (String) value.get(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IssueIndexedRecord convertToAvro(String value) {
        return new IssueIndexedRecord(value, schema);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return schema;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

}
