// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.snowflake.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.snowflake.tsnowflakeoutput.TSnowflakeOutputProperties;

import net.snowflake.client.loader.LoadResultListener;
import net.snowflake.client.loader.LoadingError;
import net.snowflake.client.loader.Operation;

/**
 * Listener for processing errors and statistics of all write operations with Snowflake.
 */
public class SnowflakeResultListener implements LoadResultListener {

    final private List<IndexedRecord> errors = new ArrayList<>();

    final private AtomicInteger errorCount = new AtomicInteger(0);

    final private AtomicInteger errorRecordCount = new AtomicInteger(0);

    final public AtomicInteger counter = new AtomicInteger(0);

    final public AtomicInteger processed = new AtomicInteger(0);

    final public AtomicInteger deleted = new AtomicInteger(0);

    final public AtomicInteger updated = new AtomicInteger(0);

    final private AtomicInteger submittedRowCount = new AtomicInteger(0);

    private Object[] lastRecord = null;

    public boolean throwOnError = false; // should not trigger rollback

    private TSnowflakeOutputProperties properties;


    public SnowflakeResultListener(TSnowflakeOutputProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean needErrors() {
        return true;
    }

    @Override
    public boolean needSuccessRecords() {
        return false;
    }

    @Override
    public void addError(LoadingError error) {
        Schema rejectSchema = properties.schemaReject.schema.getValue();

        IndexedRecord reject = new GenericData.Record(rejectSchema);
        reject.put(rejectSchema.getField(TSnowflakeOutputProperties.FIELD_COLUMN_NAME).pos(),
                error.getProperty(LoadingError.ErrorProperty.COLUMN_NAME));
        reject.put(rejectSchema.getField(TSnowflakeOutputProperties.FIELD_ROW_NUMBER).pos(),
                error.getProperty(LoadingError.ErrorProperty.ROW_NUMBER));
        reject.put(rejectSchema.getField(TSnowflakeOutputProperties.FIELD_CATEGORY).pos(),
                error.getProperty(LoadingError.ErrorProperty.CATEGORY));
        reject.put(rejectSchema.getField(TSnowflakeOutputProperties.FIELD_CHARACTER).pos(),
                error.getProperty(LoadingError.ErrorProperty.CHARACTER));
        reject.put(rejectSchema.getField(TSnowflakeOutputProperties.FIELD_ERROR_MESSAGE).pos(),
                error.getProperty(LoadingError.ErrorProperty.ERROR));
        reject.put(rejectSchema.getField(TSnowflakeOutputProperties.FIELD_BYTE_OFFSET).pos(),
                error.getProperty(LoadingError.ErrorProperty.BYTE_OFFSET));
        reject.put(rejectSchema.getField(TSnowflakeOutputProperties.FIELD_LINE).pos(),
                error.getProperty(LoadingError.ErrorProperty.LINE));
        reject.put(rejectSchema.getField(TSnowflakeOutputProperties.FIELD_SQL_STATE).pos(),
                error.getProperty(LoadingError.ErrorProperty.SQL_STATE));
        reject.put(rejectSchema.getField(TSnowflakeOutputProperties.FIELD_CODE).pos(),
                error.getProperty(LoadingError.ErrorProperty.CODE));
        errors.add(reject);
    }

    @Override
    public boolean throwOnError() {
        return throwOnError;
    }

    public List<IndexedRecord> getErrors() {
        return errors;
    }

    @Override
    public void recordProvided(Operation op, Object[] record) {
        lastRecord = record;
    }

    @Override
    public void addProcessedRecordCount(Operation op, int i) {
        processed.addAndGet(i);
    }

    @Override
    public void addOperationRecordCount(Operation op, int i) {
        counter.addAndGet(i);
        if (op == Operation.DELETE) {
            deleted.addAndGet(i);
        } else if (op == Operation.MODIFY || op == Operation.UPSERT) {
            updated.addAndGet(i);
        }
    }

    public Object[] getLastRecord() {
        return lastRecord;
    }

    @Override
    public int getErrorCount() {
        return errorCount.get();
    }

    @Override
    public int getErrorRecordCount() {
        return errorRecordCount.get();
    }

    @Override
    public void resetErrorCount() {
        errorCount.set(0);
    }

    @Override
    public void resetErrorRecordCount() {
        errorRecordCount.set(0);
    }

    @Override
    public void addErrorCount(int count) {
        errorCount.addAndGet(count);
    }

    @Override
    public void addErrorRecordCount(int count) {
        errorRecordCount.addAndGet(count);
    }

    @Override
    public void resetSubmittedRowCount() {
        submittedRowCount.set(0);
    }

    @Override
    public void addSubmittedRowCount(int count) {
        submittedRowCount.addAndGet(count);
    }

    @Override
    public int getSubmittedRowCount() {
        return submittedRowCount.get();
    }

}
