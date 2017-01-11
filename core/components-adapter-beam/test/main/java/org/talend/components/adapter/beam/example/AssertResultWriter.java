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

package org.talend.components.adapter.beam.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.WriterWithFeedback;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

public class AssertResultWriter implements WriterWithFeedback<Result, IndexedRecord, IndexedRecord> {

    AssertResultWriteOperation writeOperation;

    List<String> expectedResult;

    int total = 0;

    int success = 0;

    int failed = 0;

    List<String> actualResult;

    private transient IndexedRecordConverter<Object, ? extends IndexedRecord> converter;

    public AssertResultWriter(AssertResultWriteOperation writeOperation) {
        this.writeOperation = writeOperation;
        expectedResult = Arrays.asList(writeOperation.getSink().properties.data.getValue()
                .split(writeOperation.getSink().properties.rowDelimited.getValue()));
        actualResult = new ArrayList<>();
    }

    @Override
    public void open(String uId) throws IOException {

    }

    @Override
    public void write(Object object) throws IOException {
        if (null == object) {
            return;
        }
        if (null == converter) {
            converter = (IndexedRecordConverter<Object, ? extends IndexedRecord>) new AvroRegistry()
                    .createIndexedRecordConverter(object.getClass());
        }
        IndexedRecord record = converter.convertToAvro(object);
        actualResult.add(record.get(0).toString());
        total++;
        if (expectedResult.contains(record.get(0).toString())) {
            success++;
        } else {
            failed++;
        }
    }

    @Override
    public Result close() throws IOException {
        return new AssertResultResult(UUID.randomUUID().toString(), total, success, failed, actualResult);
    }

    @Override
    public WriteOperation<Result> getWriteOperation() {
        return writeOperation;
    }

    @Override
    public Iterable<IndexedRecord> getSuccessfulWrites() {
        return null;
    }

    @Override
    public Iterable<IndexedRecord> getRejectedWrites() {
        return null;
    }
}
