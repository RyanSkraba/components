package org.talend.components.mysql;

import org.talend.components.api.component.runtime.input.Split;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class DBTableSplit implements Split {

    private long end = 0;

    private long start = 0;

    /**
     * Default Constructor
     */
    public DBTableSplit() {
    }

    /**
     * Convenience Constructor
     *
     * @param start the index of the first row to select
     * @param end   the index of the last row to select
     */
    public DBTableSplit(long start, long end) {
        this.start = start;
        this.end = end;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getLocations() {
        // TODO Add a layer to enable SQL "sharding" and support locality
        return new String[]{};
    }

    /**
     * @return The index of the first row to select
     */
    public long getStart() {
        return start;
    }

    /**
     * @return The index of the last row to select
     */
    public long getEnd() {
        return end;
    }

    /**
     * @return The total row count in this split
     */
    @Override
    public long getLength() {
        return end - start;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readFields(DataInput input) {
        try {
            start = input.readLong();
            end = input.readLong();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int compareTo(Split o) {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(DataOutput output) {
        try {
            output.writeLong(start);
            output.writeLong(end);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}