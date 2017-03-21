package org.talend.components.snowflake.runtime;

import org.apache.avro.Schema;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.daikon.avro.SchemaConstants;

/**
 * Unit-tests for {@link SnowflakeAvroRegistry} class
 */
public class SnowflakeAvroRegistryTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SnowflakeAvroRegistryTest.class);

    private static final String TALEND_EXPECTED_DATE_PATTERN = "yyyy-MM-dd";

    private static final String TALEND_EXPECTED_TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    private SnowflakeAvroRegistry snowflakeAvroRegistry;

    private int size;

    private int scale = 5;

    private String fieldName;

    private String dbColumnName;

    private boolean nullable;

    private String defaultValue;

    @Before
    public void setUp() throws Exception {
        snowflakeAvroRegistry = SnowflakeAvroRegistry.get();
        size = 10;
        scale = 5;
        fieldName = "fieldName";
        dbColumnName = "dbColumnName";
        nullable = true;
        defaultValue = "defaultValue";
    }

    /**
     * Checks {@link SnowflakeAvroRegistry#sqlType2Avro(int, int, int, boolean, String, String, Object)}
     * returns the {@link org.apache.avro.Schema.Field} with logical DATE type
     */
    @Test
    public void testSqlType2AvroDate() throws Exception {
        final int dbtype = java.sql.Types.DATE;

        Schema.Field field = snowflakeAvroRegistry
                .sqlType2Avro(size, scale, dbtype, nullable, fieldName, dbColumnName, defaultValue);

        LOGGER.debug("field: " + field.toString());

        Assert.assertEquals(fieldName, field.name());
        Assert.assertEquals(-1, field.pos());
        Assert.assertEquals(TALEND_EXPECTED_DATE_PATTERN, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        Assert.assertEquals(java.sql.Types.DATE, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        Assert.assertEquals("dbColumnName", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        Assert.assertEquals("defaultValue", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));

        LOGGER.debug(field.getObjectProps().toString());

    }

    /**
     * Checks {@link SnowflakeAvroRegistry#sqlType2Avro(int, int, int, boolean, String, String, Object)}
     * returns the {@link org.apache.avro.Schema.Field} with logical TIMESTAMP type
     */
    @Test
    public void testSqlType2AvroTimestamp() throws Exception {
        final int dbtype = java.sql.Types.TIMESTAMP;

        Schema.Field field = snowflakeAvroRegistry
                .sqlType2Avro(size, scale, dbtype, nullable, fieldName, dbColumnName, defaultValue);

        LOGGER.debug("field: " + field.toString());

        Assert.assertEquals(fieldName, field.name());
        Assert.assertEquals(-1, field.pos());
        Assert.assertEquals(TALEND_EXPECTED_TIMESTAMP_PATTERN, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        Assert.assertEquals(java.sql.Types.TIMESTAMP, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        Assert.assertEquals("dbColumnName", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        Assert.assertEquals("defaultValue", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));

        LOGGER.debug(field.getObjectProps().toString());

    }
}
