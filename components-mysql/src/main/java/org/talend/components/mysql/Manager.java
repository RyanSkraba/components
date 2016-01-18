package org.talend.components.mysql;

import org.talend.components.api.component.runtime.input.Split;

import java.io.IOException;

public class Manager {
    protected String firstColumn;

    protected String getSelectedColumns(String columnsStr) {
        String columnStr = "";
        String[] columns = columnsStr.split(",");
        for (int i = 0; i < columns.length; i++) {
            columnStr += getLProtectedChar(columns[i]) + columns[i] + getRProtectedChar(columns[i]);
            if (i == 0) {
                firstColumn = columnStr;
            }
            if (i < columns.length - 1) {
                columnStr += ",";
            }
        }
        return columnStr;
    }

    public String getDBSql(Split split, String dbQuery, String columnsStr) throws IOException {
        String sql = "";
        String columns = getSelectedColumns(columnsStr);
        sql = getDBSql(split, dbQuery, columns, firstColumn);
        return sql;
    }

    protected String getDBSql(Split split, String dbQuery, String columnsStr, String firstColumn) throws IOException {
        String sql = "select * from (" + dbQuery + ") talendTable LIMIT " + split.getLength() + " OFFSET " + ((DBTableSplit) split).getStart();
        return sql;
    }

    public String constructQuery(String table, String[] fieldNames) {
        if (fieldNames == null) {
            throw new IllegalArgumentException("Field names may not be null");
        }
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO ");
        query.append(getLProtectedChar(table) + table + getRProtectedChar(table));
        if (fieldNames.length > 0 && fieldNames[0] != null) {
            query.append(" (");
            for (int i = 0; i < fieldNames.length; i++) {
                query.append(getLProtectedChar(fieldNames[i]) + fieldNames[i] + getRProtectedChar(fieldNames[i]));
                if (i != fieldNames.length - 1) {
                    query.append(",");
                }
            }
            query.append(")");
        }
        query.append(" VALUES (");

        for (int i = 0; i < fieldNames.length; i++) {
            query.append("?");
            if (i != fieldNames.length - 1) {
                query.append(",");
            }
        }
        query.append(")");

        return query.toString();
    }

    protected String getLProtectedChar() {
        return "";
    }

    protected String getRProtectedChar() {
        return "";
    }

    protected String getLProtectedChar(String columName) {
        return getLProtectedChar();
    }

    protected String getRProtectedChar(String columName) {
        return getRProtectedChar();
    }
}

class MysqlManager extends Manager {
    @Override
    protected String getLProtectedChar() {
        return "`";
    }

    @Override
    protected String getRProtectedChar() {
        return "`";
    }
}

class MSSQLManager extends Manager {
    @Override
    protected String getLProtectedChar() {
        return "[";
    }

    @Override
    protected String getRProtectedChar() {
        return "]";
    }

    @Override
    protected String getDBSql(Split split, String dbQuery, String columnsStr, String firstColumn) throws IOException {
        String sql = "select " + columnsStr + " from (select row_number() over(order by " + firstColumn + ") as rownum ," + columnsStr + " from (" + dbQuery + ") talend_alias_one) talend_alias_two where rownum>" + ((DBTableSplit) split).getStart() + " and rownum<=" + ((DBTableSplit) split).getEnd();
        return sql;
    }
}

class NetezzaManager extends Manager {
    private String[] netezzaReservedWords = {
            "ABORT", "DEC", "LEADING", "RESET", "ADMIN",
            "DECIMAL", "LEFT", "REUSE", "AGGREGATE", "DECODE",
            "LIKE", "RIGHT", "ALIGN", "DEFAULT", "LIMIT",
            "ROWS", "ALL", "DEFERRABLE", "LISTEN",
            "ROWSETLIMIT", "ALLOCATE", "DESC", "LOAD", "RULE",
            "ANALYSE", "DISTINCT", "LOCAL", "SEARCH",
            "ANALYZE", "DISTRIBUTE", "LOCK", "SELECT", "AND",
            "DO", "MATERIALIZED", "SEQUENCE", "ANY", "ELSE",
            "MINUS", "SESSION_USER", "AS", "END", "MOVE",
            "SETOF", "ASC", "EXCEPT", "NATURAL", "SHOW",
            "BETWEEN", "EXCLUDE", "NCHAR", "SOME", "BINARY",
            "EXISTS", "NEW", "SUBSTRING", "BIT", "EXPLAIN",
            "NOT", "SYSTEM", "BOTH", "EXPRESS", "NOTNULL",
            "TABLE", "CASE", "EXTEND", "NULL", "THEN", "CAST",
            "EXTERNAL", "NULLIF", "TIES", "CHAR", "EXTRACT",
            "NULLS", "TIME", "CHARACTER", "FALSE", "NUMERIC",
            "TIMESTAMP", "CHECK", "FIRST", "NVL", "TO",
            "CLUSTER", "FLOAT", "NVL2", "TRAILING", "COALESCE",
            "FOLLOWING", "OFF", "TRANSACTION", "COLLATE",
            "FOR", "OFFSET", "TRIGGER", "COLLATION", "FOREIGN",
            "OLD", "TRIM", "COLUMN", "FROM", "ON", "TRUE",
            "CONSTRAINT", "FULL", "ONLINE", "UNBOUNDED", "COPY",
            "FUNCTION", "ONLY", "UNION", "CROSS", "GENSTATS",
            "OR", "UNIQUE", "CURRENT", "GLOBAL", "ORDER", "USER",
            "CURRENT_CATALOG", "GROUP", "OTHERS", "USING",
            "CURRENT_DATE", "HAVING", "OUT", "VACUUM",
            "CURRENT_DB", "IDENTIFIER_CASE", "OUTER", "VARCHAR",
            "CURRENT_SCHEMA", "ILIKE", "OVER", "VERBOSE",
            "CURRENT_SID", "IN", "OVERLAPS", "VERSION",
            "CURRENT_TIME", "INDEX", "PARTITION", "VIEW",
            "CURRENT_TIMESTAMP", "INITIALLY", "POSITION", "WHEN",
            "CURRENT_USER", "INNER", "PRECEDING", "WHERE",
            "CURRENT_USERID", "INOUT", "PRECISION", "WITH",
            "CURRENT_USEROID", "INTERSECT", "PRESERVE", "WRITE",
            "DEALLOCATE", "INTERVAL", "PRIMARY", "RESET", "INTOREUSE"
    };

    protected boolean isNetezzaReservedWord(String keyword) {
        for (String netezzaReservedWord : netezzaReservedWords) {
            if (netezzaReservedWord.equalsIgnoreCase(keyword)) {
                return true;
            }
        }
        return false;
    }

    protected boolean contaionsSpaces(String columnName) {
        if (columnName != null) {
            if (columnName.startsWith("\" + ") && columnName.endsWith(" + \"")) {
                return false;
            }

            if (columnName.contains(" ")) {
                return true;
            }
        }
        return false;
    }

    protected String getLProtectedChar(String keyword) {
        if (isNetezzaReservedWord(keyword) || contaionsSpaces(keyword)) {
            return "\"";
        }
        return getLProtectedChar();
    }

    protected String getRProtectedChar(String keyword) {
        if (isNetezzaReservedWord(keyword) || contaionsSpaces(keyword)) {
            return "\"";
        }
        return getRProtectedChar();
    }

    @Override
    protected String getLProtectedChar() {
        return "";
    }

    @Override
    protected String getRProtectedChar() {
        return "";
    }

    @Override
    protected String getDBSql(Split split, String dbQuery, String columnsStr, String firstColumn) throws IOException {
        String sql = "select " + columnsStr + " from (select row_number() over(order by " + firstColumn + ") as rownum ," + columnsStr + " from (" + dbQuery + ") talend_alias_one) talend_alias_two where rownum>" + ((DBTableSplit) split).getStart() + " and rownum<=" + ((DBTableSplit) split).getEnd();
        return sql;
    }
}

class ParaccelManager extends Manager {
    @Override
    protected String getLProtectedChar() {
        return "\"";
    }

    @Override
    protected String getRProtectedChar() {
        return "\"";
    }
}

class VerticaManager extends Manager {
    private String[] verticaReservedWords = {
            "ALL", "ANALYSE", "ANALYZE", "AND", "ANY", "ARRAY",
            "AS", "ASC", "BINARY", "BOTH", "CASE", "CAST", "CHECK",
            "COLUMN", "CONSTRAINT", "CORRELATION", "CREATE",
            "CURRENT_DATABASE", "CURRENT_DATE", "CURRENT_SCHEMA",
            "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER",
            "DEFAULT", "DEFERRABLE", "DESC", "DISTINCT", "DO",
            "ELSE", "ENCODED", "END", "EXCEPT", "FALSE", "FOR",
            "FOREIGN", "FROM", "GRANT", "GROUP", "GROUPED", "HAVING",
            "IN", "INITIALLY", "INTERSECT", "INTERVAL", "INTERVALYM",
            "INTO", "JOIN", "KSAFE", "LEADING", "LIMIT", "LOCALTIME",
            "LOCALTIMESTAMP", "MATCH", "NEW", "NOT", "NULL",
            "NULLSEQUAL", "OFF", "OFFSET", "OLD", "ON", "ONLY", "OR",
            "ORDER", "PINNED", "PLACING", "PRIMARY", "PROJECTION",
            "REFERENCES", "SCHEMA", "SEGMENTED", "SELECT",
            "SESSION_USER", "SOME", "SYSDATE", "TABLE", "THEN",
            "TIMESERIES", "TO", "TRAILING", "TRUE", "UNBOUNDED",
            "UNION", "UNIQUE", "UNSEGMENTED", "USER", "USING", "WHEN",
            "WHERE", "WINDOW", "WITH", "WITHIN"
    };

    @Override
    protected String getLProtectedChar() {
        return "";
    }

    @Override
    protected String getRProtectedChar() {
        return "";
    }

    protected boolean isVerticaReservedWord(String keyword) {
        for (String verticaReservedWord : verticaReservedWords) {
            if (verticaReservedWord.equalsIgnoreCase(keyword)) {
                return true;
            }
        }
        return false;
    }

    protected boolean contaionsSpaces(String columnName) {
        if (columnName != null) {
            if (columnName.startsWith("\" + ") && columnName.endsWith(" + \"")) {
                return false;
            }

            if (columnName.contains(" ")) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected String getLProtectedChar(String keyword) {
        if (isVerticaReservedWord(keyword) || contaionsSpaces(keyword)) {
            return "\"";
        }
        return getLProtectedChar();
    }

    @Override
    protected String getRProtectedChar(String keyword) {
        if (isVerticaReservedWord(keyword) || contaionsSpaces(keyword)) {
            return "\"";
        }
        return getRProtectedChar();
    }
}

class PostgreManager extends Manager {
    @Override
    protected String getLProtectedChar() {
        return "\"";
    }

    @Override
    protected String getRProtectedChar() {
        return "\"";
    }
}

class TeradataManager extends Manager {
    @Override
    protected String getLProtectedChar() {
        return "\"";
    }

    @Override
    protected String getRProtectedChar() {
        return "\"";
    }

    @Override
    protected String getDBSql(Split split, String dbQuery, String columnsStr, String firstColumn) throws IOException {
        String sql = "select " + columnsStr + " from (select row_number() over(order by " + firstColumn + ") as RowNum," + columnsStr + " from (" + dbQuery + ") talend_alias_one qualify RowNum between " + (((DBTableSplit) split).getStart() + 1) + " and " + ((DBTableSplit) split).getEnd() + ") talend_alias_two";
        return sql;
    }
}

class InformixManager extends Manager {
    @Override
    protected String getLProtectedChar() {
        return "";
    }

    @Override
    protected String getRProtectedChar() {
        return "";
    }

    @Override
    protected String getDBSql(Split split, String dbQuery, String columnsStr, String firstColumn) throws IOException {
        String sql = "select skip " + ((DBTableSplit) split).getStart() + " first " + split.getLength() + " " + columnsStr + " from (" + dbQuery + ")";
        return sql;
    }
}

class AS400Manager extends Manager {
    @Override
    protected String getLProtectedChar() {
        return "";
    }

    @Override
    protected String getRProtectedChar() {
        return "";
    }

    @Override
    protected String getDBSql(Split split, String dbQuery, String columnsStr, String firstColumn) throws IOException {
        String sql = "select " + columnsStr + " from (select " + columnsStr + " from (" + dbQuery + ") talend_alias_one ORDER BY " + firstColumn + " asc fetch first " + ((DBTableSplit) split).getEnd() + " rows only) talend_alias_two ORDER BY " + firstColumn + " desc fetch first " + split.getLength() + " rows only";
        return sql;
    }
}

class DB2Manager extends Manager {
    @Override
    protected String getLProtectedChar() {
        return "\"";
    }

    @Override
    protected String getRProtectedChar() {
        return "\"";
    }

    @Override
    protected String getDBSql(Split split, String dbQuery, String columnsStr, String firstColumn) throws IOException {
        String sql = "select " + columnsStr + " from (select row_number() over() as rownum ," + columnsStr + " from (" + dbQuery + ") talend_alias_one) talend_alias_two where rownum>" + ((DBTableSplit) split).getStart() + " and rownum<=" + ((DBTableSplit) split).getEnd();
        return sql;
    }
}

class IngresManager extends Manager {
    @Override
    protected String getLProtectedChar() {
        return "\"";
    }

    @Override
    protected String getRProtectedChar() {
        return "\"";
    }

    @Override
    protected String getDBSql(Split split, String dbQuery, String columnsStr, String firstColumn) throws IOException {
        String sql = "SELECT * FROM (" + dbQuery + ") talend_alias OFFSET " + ((DBTableSplit) split).getStart() + " FETCH FIRST " + split.getLength() + " ROWS ONLY";
        return sql;
    }
}

class MaxDBManager extends Manager {
    private String[] maxDBKeyWords = {
            "ABS", "ABSOLUTE", "ACOS",
            "ADDDATE", "ADDTIME", "ALL",
            "ALPHA", "ALTER", "ANY", "ASCII",
            "ASIN", "ATAN", "ATAN2", "AVG",
            "BINARY", "BIT", "BOOLEAN", "BYTE",
            "CASE", "CEIL", "CEILING", "CHAR",
            "CHARACTER", "CHECK", "CHR",
            "COLUMN", "CONCAT", "CONSTRAINT",
            "COS", "COSH", "COT", "COUNT",
            "CROSS", "CURDATE", "CURRENT",
            "CURTIME", "DATABASE", "DATE",
            "DATEDIFF", "DAY", "DAYNAME",
            "DAYOFMONTH", "DAYOFWEEK",
            "DAYOFYEAR", "DEC", "DECIMAL",
            "DECODE", "DEFAULT", "DEGREES",
            "DELETE", "DIGITS", "DISTINCT",
            "DOUBLE", "EXCEPT", "EXISTS", "EXP",
            "EXPAND", "FIRST", "FIXED", "FLOAT",
            "FLOOR", "FOR", "FROM", "FULL",
            "GET_OBJECTNAME", "GET_SCHEMA",
            "GRAPHIC", "GREATEST", "GROUP",
            "HAVING", "HEX", "HEXTORAW", "HOUR",
            "IFNULL", "IGNORE", "INDEX", "INITCAP",
            "INNER", "INSERT", "INT", "INTEGER",
            "INTERNAL", "INTERSECT", "INTO", "JOIN",
            "KEY", "LAST", "LCASE", "LEAST", "LEFT",
            "LENGTH", "LFILL", "LIST", "LN", "LOCATE",
            "LOG", "LOG10", "LONG", "LONGFILE",
            "LOWER", "LPAD", "LTRIM", "MAKEDATE",
            "MAKETIME", "MAPCHAR", "MAX", "MBCS",
            "MICROSECOND", "MIN", "MINUTE", "MOD",
            "MONTH", "MONTHNAME", "NATURAL", "NCHAR",
            "NEXT", "NO", "NOROUND", "NOT", "NOW",
            "NULL", "NUM", "NUMERIC", "OBJECT", "OF",
            "ON", "ORDER", "PACKED", "PI", "POWER",
            "PREV", "PRIMARY", "RADIANS", "REAL",
            "REJECT", "RELATIVE", "REPLACE", "RFILL",
            "RIGHT", "ROUND", "ROWID", "ROWNO", "RPAD",
            "RTRIM", "SECOND", "SELECT", "SELUPD",
            "SERIAL", "SET", "SHOW", "SIGN", "SIN",
            "SINH", "SMALLINT", "SOME", "SOUNDEX", "SPACE",
            "SQRT", "STAMP", "STATISTICS", "STDDEV",
            "SUBDATE", "SUBSTR", "SUBSTRING", "SUBTIME",
            "SUM", "SYSDBA", "TABLE", "TAN", "TANH", "TIME",
            "TIMEDIFF", "TIMESTAMP", "TIMEZONE", "TO",
            "TOIDENTIFIER", "TRANSACTION", "TRANSLATE",
            "TRIM", "TRUNC", "TRUNCATE", "UCASE", "UID",
            "UNICODE", "UNION", "UPDATE", "UPPER", "USER",
            "USERGROUP", "USING", "UTCDATE", "UTCDIFF",
            "VALUE", "VALUES", "VARCHAR", "VARGRAPHIC",
            "VARIANCE", "WEEK", "WEEKOFYEAR", "WHEN",
            "WHERE", "WITH", "YEAR", "ZONED"
    };

    protected boolean isMaxDBKeyword(String keyword) {
        for (int i = 0; i < maxDBKeyWords.length; i++) {
            if (maxDBKeyWords[i].equalsIgnoreCase(keyword)) {
                return true;
            }
        }
        return false;
    }

    protected String getLProtectedChar(String keyword) {
        if (isMaxDBKeyword(keyword)) {
            return "\"";
        }
        return getLProtectedChar();
    }

    protected String getRProtectedChar(String keyword) {
        if (isMaxDBKeyword(keyword)) {
            return "\"";
        }
        return getRProtectedChar();
    }

    @Override
    protected String getLProtectedChar() {
        return "";
    }

    @Override
    protected String getRProtectedChar() {
        return "";
    }

    @Override
    protected String getDBSql(Split split, String dbQuery, String columnsStr, String firstColumn) throws IOException {
        String sql = "select * from (" + dbQuery + ") limit " + ((DBTableSplit) split).getStart() + "," + split.getLength();
        return sql;
    }
}

class OracleManager extends Manager {
    private String[] oracleKeyWords = {
            "ACCESS", "AUDIT", "COMPRESS", "DESC",
            "ADD", "CONNECT", "DISTINCT",
            "ALL", "BY", "CREATE", "DROP",
            "ALTER", "CHAR", "CURRENT", "ELSE",
            "AND", "CHECK", "DATE", "EXCLUSIVE",
            "ANY", "CLUSTER", "DECIMAL", " EXISTS",
            "AS", "COLUMN", "DEFAULT", "FILE",
            "ASC", "COMMENT", "DELETE", "FLOAT",
            "FOR", "LONG", "PCTFREE", "SUCCESSFUL",
            "FROM", "MAXEXTENTS", "PRIOR", "SYNONYM",
            "GRANT", "MINUS", "PRIVILEGES", "SYSDATE",
            "GROUP", "MODE", "PUBLIC", "TABLE",
            "HAVING", "MODIFY", "RAW", "THEN",
            "IDENTIFIED", "NETWORK", "RENAME", "TO",
            "IMMEDIATE", "NOAUDIT", "RESOURCE", "TRIGGER",
            "IN", "NOCOMPRESS", "REVOKE", "UID",
            "INCREMENT", "NOT", "ROW", "UNION",
            "INDEX", "NOWAIT", "ROWID", "UNIQUE",
            "INITIAL", "NULL", "ROWNUM", "UPDATE",
            "INSERT", "NUMBER", "ROWS", "USER",
            "INTEGER", "OF", "SELECT", "VALIDATE",
            "INTERSECT", "OFFLINE", "SESSION", "VALUES",
            "INTO", "ON", "SET", "VARCHAR",
            "IS", "ONLINE", "SHARE", "VARCHAR2",
            "LEVEL", "OPTION", "SIZE", "VIEW",
            "LIKE", "OR", "SMALLINT", "WHENEVER",
            "LOCK", "ORDER", "START", "WHERE", "WITH"
    };

    @Override
    protected String getLProtectedChar() {
        return "";
    }

    @Override
    protected String getRProtectedChar() {
        return "";
    }

    protected boolean isOracleKeyword(String keyword) {
        for (String oracleKeyWord : oracleKeyWords) {
            if (oracleKeyWord.equalsIgnoreCase(keyword)) {
                return true;
            }
        }
        return false;
    }

    protected boolean contaionsSpaces(String columnName) {
        if (columnName != null) {
            if (columnName.startsWith("\" + ") && columnName.endsWith(" + \"")) {
                return false;
            }

            if (columnName.contains(" ")) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected String getLProtectedChar(String keyword) {
        if (isOracleKeyword(keyword) || contaionsSpaces(keyword)) {
            return "\"";
        }
        return getLProtectedChar();
    }

    @Override
    protected String getRProtectedChar(String keyword) {
        if (isOracleKeyword(keyword) || contaionsSpaces(keyword)) {
            return "\"";
        }
        return getRProtectedChar();
    }

    @Override
    protected String getDBSql(Split split, String dbQuery, String columnsStr, String firstColumn) throws IOException {
        String sql = "select " + columnsStr + " from (select rownum rm," + columnsStr + " from (" + dbQuery + ") where rownum <=" + ((DBTableSplit) split).getEnd() + " ) talend_alias_one where talend_alias_one.rm>" + ((DBTableSplit) split).getStart();
        return sql;
    }
}