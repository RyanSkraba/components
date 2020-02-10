package org.talend.components.snowflake.tsnowflakeoutput;

/**
 * Snowflake supported Date and Time data types
 * This enum reflects possible values for DI Date (java.util.Date) type mapping
 */
public enum DateMapping {
    DATE,
    DATETIME,
    TIME,
    TIMESTAMP,
    TIMESTAMP_LTZ,
    TIMESTAMP_NTZ,
    TIMESTAMP_TZ
}
