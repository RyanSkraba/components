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

package org.talend.components.adapter.beam.example;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

public class FixedFlowReader extends AbstractBoundedReader<IndexedRecord> {

    private FixedFlowProperties properties;

    private Iterator<String> rows;

    private String row;

    public FixedFlowReader(BoundedSource source, FixedFlowProperties properties) {
        super(source);
        this.properties = properties;
    }

    @Override
    public boolean start() throws IOException {
        rows = Arrays.asList(properties.data.getValue().split(properties.rowDelimited.getValue())).iterator();
        return advance();
    }

    @Override
    public boolean advance() throws IOException {
        boolean hasNext = rows.hasNext();
        if (hasNext) {
            row = rows.next();
        }
        return hasNext;
    }

    @Override
    public IndexedRecord getCurrent() throws NoSuchElementException {
        return new FixedFlowIndexedRecord(row);
    }

    @Override
    public Map<String, Object> getReturnValues() {
        return null;
    }

    private class FixedFlowIndexedRecord implements IndexedRecord {

        String value;

        public FixedFlowIndexedRecord(String value) {
            this.value = value;
        }

        @Override
        public void put(int i, Object v) {
            throw new IndexedRecordConverter.UnmodifiableAdapterException();
        }

        @Override
        public Object get(int i) {
            return value;
        }

        @Override
        public Schema getSchema() {
            return properties.schema.getValue();
        }
    }
}
