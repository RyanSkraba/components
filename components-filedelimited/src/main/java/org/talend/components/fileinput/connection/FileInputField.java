package org.talend.components.fileinput.connection;

public class FileInputField {
	 	private final String columnName;

	    private final String type;

	    private final String content;

	    public FileInputField(String columnName, String type, String content) {
	        this.columnName = columnName;
	        this.type = type;
	        this.content = content;
	    }

	    public String getColumnName() {
	        return columnName;
	    }

	    public String getType() {
	        return type;
	    }

	    public String getContent() {
	        return content;
	    }
}
