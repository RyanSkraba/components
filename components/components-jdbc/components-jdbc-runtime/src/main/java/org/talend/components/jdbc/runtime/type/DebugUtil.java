package org.talend.components.jdbc.runtime.type;

public class DebugUtil {

    private String[] splits;

    private StringBuffer strBuffer;

    public DebugUtil(String sql) {
        sql += " ";
        splits = sql.split("\\?");
        strBuffer = new StringBuffer(32);
    }

    private int index = 0;

    public void writeHead() {
        if (index < splits.length) {
            strBuffer.append(splits[index++]);
        }
    }

    public void writeColumn(String columnContent, boolean textEnclose) {
        if (index < splits.length) {
            if (textEnclose) {
                strBuffer.append("'");
            }
            strBuffer.append(columnContent);
            if (textEnclose) {
                strBuffer.append("'");
            }
            strBuffer.append(splits[index++]);
        }
    }

    public String getSQL() {
        return strBuffer.toString();
    }

}
