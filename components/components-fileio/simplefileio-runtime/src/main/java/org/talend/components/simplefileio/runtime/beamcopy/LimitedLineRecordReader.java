package org.talend.components.simplefileio.runtime.beamcopy;

import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;
import org.talend.components.simplefileio.runtime.hadoop.csv.CSVFileRecordReader;

import java.io.IOException;

/**
 * TDI-41193 : When record delimiter parameter doesn't match real record delimiter in File,
 *             it ends to "OutOfMemoryError". This class change it to a more explicit exception.
 */
public class LimitedLineRecordReader<K, V> extends RecordReader<K, V> {

    private final RecordReader<K, V> reader;

    public LimitedLineRecordReader(RecordReader<K, V> reader) {
        this.reader = reader;
    }

    public static <KEYIN, VALUEIN>  RecordReader<KEYIN, VALUEIN> buildReader(RecordReader<KEYIN, VALUEIN> reader) {
        if (reader instanceof LimitedLineRecordReader) {
            // already encapsulate
            return reader;
        }
        if (!LineRecordReader.class.isInstance(reader) &&
            !CSVFileRecordReader.class.isInstance(reader)) {
            // only line file reader.
            return reader;
        }
        return (RecordReader<KEYIN, VALUEIN>) new LimitedLineRecordReader(reader);
    }

    @Override
    public void initialize(InputSplit genericSplit, TaskAttemptContext context) throws IOException, InterruptedException {
        try {
            this.reader.initialize(genericSplit, context);
        }
        catch (OutOfMemoryError err) {
            throw new FileParameterException("Row size exceeded maximum allowed size", err);
        }
    }

    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException {
        try {
            return this.reader.nextKeyValue();
        }
        catch (OutOfMemoryError err) {
            throw new FileParameterException("Row size exceeded maximum allowed size", err);
        }
    }

    @Override
    public K getCurrentKey() throws IOException, InterruptedException {
        return this.reader.getCurrentKey();
    }

    @Override
    public V getCurrentValue() throws IOException, InterruptedException {
        return this.reader.getCurrentValue();
    }

    @Override
    public float getProgress() throws IOException, InterruptedException {
        return this.reader.getProgress();
    }

    @Override
    public synchronized void close() throws IOException {
        this.reader.close();
    }
}
