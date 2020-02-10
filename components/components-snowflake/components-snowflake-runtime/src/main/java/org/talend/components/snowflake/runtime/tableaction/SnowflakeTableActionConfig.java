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
package org.talend.components.snowflake.runtime.tableaction;

import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.talend.components.common.tableaction.TableActionConfig;
import org.talend.components.snowflake.SnowflakeTypes;

import java.sql.Types;

public class SnowflakeTableActionConfig extends TableActionConfig {

    /**
     * Constant to represent fake SQL type in java - DI_DATE
     * It is used for type mapping during CREATE TABLE query generation
     * ConvertAvroTypeToSQL class will map DI DATE (java.util.Date) type to this constant
     * Then this constant will be used for mapping to type specified by user
     * This is a dirty hack for 7.1.1 quickfix to avoid components-common library modification
     */
    public static final int DI_DATE = 91404;

    public SnowflakeTableActionConfig(boolean isUpperCase){
        this.SQL_UPPERCASE_IDENTIFIER = isUpperCase;
        this.SQL_ESCAPE_ENABLED = true;
        this.SQL_DROP_TABLE_SUFFIX = " CASCADE";

        this.CONVERT_AVROTYPE_TO_SQLTYPE.put(Schema.Type.FLOAT, Types.FLOAT);
        this.CONVERT_AVROTYPE_TO_SQLTYPE.put(Schema.Type.DOUBLE, Types.FLOAT);

        this.CONVERT_LOGICALTYPE_TO_SQLTYPE.put(LogicalTypes.decimal(32), Types.FLOAT);

        this.CONVERT_JAVATYPE_TO_SQLTYPE.put("java.math.BigDecimal", Types.FLOAT);

        this.CONVERT_SQLTYPE_TO_ANOTHER_SQLTYPE.put(Types.BLOB, Types.BINARY);

        this.DB_TYPES = SnowflakeTypes.all();
    }

}
