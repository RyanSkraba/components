// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.snowflake;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.talend.components.common.config.jdbc.DbmsType;

/**
 * Utility class which holds all Snowflake database types
 */
public final class SnowflakeTypes {

    public static final DbmsType ARRAY = new DbmsType("ARRAY");

    public static final DbmsType BIGINT = new DbmsType("BIGINT");

    public static final DbmsType BINARY = new DbmsType("BINARY", false, true);

    public static final DbmsType BOOLEAN = new DbmsType("BOOLEAN");

    public static final DbmsType CHARACTER = new DbmsType("CHARACTER", false, true);

    public static final DbmsType DATE = new DbmsType("DATE");

    public static final DbmsType DATETIME = new DbmsType("DATETIME");

    public static final DbmsType DECIMAL = new DbmsType("DECIMAL", false, false);

    public static final DbmsType DOUBLE = new DbmsType("DOUBLE");

    public static final DbmsType DOUBLE_PRECISION = new DbmsType("DOUBLE PRECISION");

    public static final DbmsType FLOAT = new DbmsType("FLOAT");

    public static final DbmsType FLOAT4 = new DbmsType("FLOAT4");

    public static final DbmsType FLOAT8 = new DbmsType("FLOAT8");

    public static final DbmsType INT = new DbmsType("INT");

    public static final DbmsType INTEGER = new DbmsType("INTEGER");

    public static final DbmsType NUMBER = new DbmsType("NUMBER", false, false);

    public static final DbmsType NUMERIC = new DbmsType("NUMERIC", false, false);

    public static final DbmsType OBJECT = new DbmsType("OBJECT");

    public static final DbmsType REAL = new DbmsType("REAL");

    public static final DbmsType SMALLINT = new DbmsType("SMALLINT");

    public static final DbmsType STRING = new DbmsType("STRING", false, true);

    public static final DbmsType TEXT = new DbmsType("TEXT", false, true);

    public static final DbmsType TIME = new DbmsType("TIME");

    public static final DbmsType TIMESTAMP = new DbmsType("TIMESTAMP");

    public static final DbmsType TIMESTAMP_LTZ = new DbmsType("TIMESTAMP_LTZ");

    public static final DbmsType TIMESTAMP_NTZ = new DbmsType("TIMESTAMP_NTZ");

    public static final DbmsType TIMESTAMP_TZ = new DbmsType("TIMESTAMP_TZ");

    public static final DbmsType VARBINARY = new DbmsType("VARBINARY", false, true);

    public static final DbmsType VARCHAR = new DbmsType("VARCHAR", false, true);

    public static final DbmsType VARIANT = new DbmsType("VARIANT");

    private static final Map<String, DbmsType> allTypes;

    static {
        allTypes = new HashMap<>();
        allTypes.put("ARRAY", ARRAY);
        allTypes.put("BIGINT", BIGINT);
        allTypes.put("BINARY", BINARY);
        allTypes.put("BOOLEAN", BOOLEAN);
        allTypes.put("CHARACTER", CHARACTER);
        allTypes.put("DATE", DATE);
        allTypes.put("DATETIME", DATETIME);
        allTypes.put("DECIMAL", DECIMAL);
        allTypes.put("DOUBLE", DOUBLE);
        allTypes.put("DOUBLE PRECISION", DOUBLE_PRECISION);
        allTypes.put("FLOAT", FLOAT);
        allTypes.put("FLOAT4", FLOAT4);
        allTypes.put("FLOAT8", FLOAT8);
        allTypes.put("INT", INT);
        allTypes.put("INTEGER", INTEGER);
        allTypes.put("NUMBER", NUMBER);
        allTypes.put("NUMERIC", NUMERIC);
        allTypes.put("OBJECT", OBJECT);
        allTypes.put("REAL", REAL);
        allTypes.put("SMALLINT", SMALLINT);
        allTypes.put("STRING", STRING);
        allTypes.put("TEXT", TEXT);
        allTypes.put("TIME", TIME);
        allTypes.put("TIMESTAMP", TIMESTAMP);
        allTypes.put("TIMESTAMP_LTZ", TIMESTAMP_LTZ);
        allTypes.put("TIMESTAMP_NTZ", TIMESTAMP_NTZ);
        allTypes.put("TIMESTAMP_TZ", TIMESTAMP_TZ);
        allTypes.put("VARBINARY", VARBINARY);
        allTypes.put("VARCHAR", VARCHAR);
        allTypes.put("VARIANT", VARIANT);
    }

    private SnowflakeTypes() {
        // no-op
    }

    public static Map<String, DbmsType> all() {
        return Collections.unmodifiableMap(allTypes);
    }

}
