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
package org.talend.components.common.tableaction;

import org.apache.avro.LogicalType;
import org.apache.avro.Schema;
import org.talend.components.common.config.jdbc.DbmsType;

import java.util.HashMap;
import java.util.Map;

public class TableActionConfig {

    public boolean SQL_UPPERCASE_IDENTIFIER = false;
    public boolean SQL_LOWERCASE_IDENTIFIER = false;

    public String SQL_ESCAPE = "\"";
    public boolean SQL_ESCAPE_ENABLED = true;

    public String SQL_FULL_NAME_SEGMENT_SEP = ".";

    public String SQL_CREATE_TABLE_PREFIX = "";
    public String SQL_CREATE_TABLE_SUFFIX = "";
    public String SQL_CREATE_TABLE = "CREATE TABLE";
    public String SQL_CREATE_TABLE_FIELD_SEP = ", ";
    public String SQL_CREATE_TABLE_FIELD_ENCLOSURE_START = "(";
    public String SQL_CREATE_TABLE_FIELD_ENCLOSURE_END = ")";
    public String SQL_CREATE_TABLE_LENGTH_START = "(";
    public String SQL_CREATE_TABLE_LENGTH_END = ")";
    public boolean SQL_CREATE_TABLE_LENGTH_ENABLED = true;
    public String SQL_CREATE_TABLE_PRECISION_SEP = ", ";
    public boolean SQL_CREATE_TABLE_PRECISION_ENABLED = true;
    public String SQL_CREATE_TABLE_CONSTRAINT = "CONSTRAINT";
    public boolean SQL_CREATE_TABLE_CONSTRAINT_ENABLED = true;
    public String SQL_CREATE_TABLE_PRIMARY_KEY_PREFIX = "pk_";
    public String SQL_CREATE_TABLE_PRIMARY_KEY = "PRIMARY KEY";
    public String SQL_CREATE_TABLE_PRIMARY_KEY_ENCLOSURE_START = "(";
    public String SQL_CREATE_TABLE_PRIMARY_KEY_ENCLOSURE_END = ")";
    public String SQL_PRIMARY_KEY_FULL_NAME_SEGMENT_SEP = "_";
    public String SQL_CREATE_TABLE_DEFAULT = "DEFAULT";
    public boolean SQL_CREATE_TABLE_DEFAULT_ENABLED = true;
    public String SQL_CREATE_TABLE_IF_NOT_EXISTS = "IF NOT EXISTS";

    public String SQL_CREATE_TABLE_NOT_NULL = " NOT NULL";

    public String SQL_DROP_TABLE_PREFIX = "";
    public String SQL_DROP_TABLE_SUFFIX = "";
    public String SQL_DROP_TABLE = "DROP TABLE";
    public String SQL_DROP_TABLE_IF_EXISITS = "IF EXISTS";

    public String SQL_TRUNCATE_PREFIX = "";
    public String SQL_TRUNCATE_SUFFIX = "";
    public String SQL_TRUNCATE = "TRUNCATE TABLE";

    public String SQL_DELETE_PREFIX = "";
    public String SQL_DELETE_SUFFIX = "";
    public String SQL_DELETE = "DELETE FROM";

    public Map<String, Integer> CONVERT_JAVATYPE_TO_SQLTYPE = new HashMap<>();
    public Map<LogicalType, Integer> CONVERT_LOGICALTYPE_TO_SQLTYPE = new HashMap<>();
    public Map<Schema.Type, Integer> CONVERT_AVROTYPE_TO_SQLTYPE = new HashMap<>();
    public Map<Integer, Integer> CONVERT_SQLTYPE_TO_ANOTHER_SQLTYPE = new HashMap<>();

    public Map<Integer, String> CUSTOMIZE_SQLTYPE_TYPENAME = new HashMap<>();

    public Map<String, DbmsType> DB_TYPES = new HashMap<>();

}
