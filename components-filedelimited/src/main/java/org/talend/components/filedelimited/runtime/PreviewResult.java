package org.talend.components.filedelimited.runtime;

import java.util.List;

public class PreviewResult {

    private String[] columnNames;

    private List<String[]> data;

    public String[] getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
    }

    public List<String[]> getData() {
        return data;
    }

    public void setData(List<String[]> data) {
        this.data = data;
    }
}
