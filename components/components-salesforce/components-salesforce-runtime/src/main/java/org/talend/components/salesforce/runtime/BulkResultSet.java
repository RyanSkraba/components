package org.talend.components.salesforce.runtime;

import java.io.IOException;
import java.util.List;

public class BulkResultSet {

    com.csvreader.CsvReader reader;

    List<String> header;

    boolean hashNext = true;

    public BulkResultSet(com.csvreader.CsvReader reader, List<String> header) {
        this.reader = reader;
        this.header = header;
    }

    public BulkResult next() throws IOException {

        boolean hasNext = false;
        try {
            hasNext = reader.readRecord();
        } catch (IOException e) {
            if (this.reader != null) {
                this.reader.close();
            }
            throw e;
        }

        BulkResult result = null;
        String[] row;

        if (hasNext) {
            if ((row = reader.getValues()) != null) {
                result = new BulkResult();
                for (int i = 0; i < this.header.size(); i++) {
                    result.setValue(header.get(i), row[i]);
                }
                return result;
            } else {
                return next();
            }
        } else {
            if (this.reader != null) {
                this.reader.close();
            }
        }
        return null;

    }

    public boolean hasNext() {
        return hashNext;
    }

}