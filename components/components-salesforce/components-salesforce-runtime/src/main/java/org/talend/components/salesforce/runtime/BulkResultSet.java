package org.talend.components.salesforce.runtime;

import java.io.IOException;
import java.util.List;

public class BulkResultSet {

    com.csvreader.CsvReader reader;

    List<String> header;

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
                    //We replace the . with _ to add support of relationShip Queries
                    //The relationShip Queries Use . in Salesforce and we use _ in Talend (Studio)
                    //So Account.Name in SF will be Account_Name in Talend
                    result.setValue(header.get(i).replace('.', '_'), row[i]);
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

}