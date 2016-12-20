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
package org.talend.components.filterrow.runtime;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.WriterWithFeedback;

/**
 * created by dmytro.chmyga on Dec 19, 2016
 */
public class TFilterRowWriter implements WriterWithFeedback<Result, IndexedRecord, IndexedRecord> {

    private final WriteOperation<Result> writeOperation;

    private List<IndexedRecord> success;

    private List<IndexedRecord> failure;

    public TFilterRowWriter(WriteOperation<Result> writeOperation) {
        this.writeOperation = writeOperation;
    }

    @Override
    public Result close() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WriteOperation<Result> getWriteOperation() {
        return writeOperation;
    }

    @Override
    public void open(String arg0) throws IOException {
        this.success = new LinkedList<>();
    }

    @Override
    public void write(Object arg0) throws IOException {
        if (arg0 instanceof IndexedRecord) {
            success.add((IndexedRecord) arg0);
        }
    }

    @Override
    public Iterable<IndexedRecord> getRejectedWrites() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterable<IndexedRecord> getSuccessfulWrites() {
        return success;
    }

}
