package org.talend.components.cassandra.tCassandraInput;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.Property;
import org.talend.components.common.SchemaProperties;

import static org.talend.components.api.properties.PropertyFactory.*;

/**
 * Created by bchen on 16-1-13.
 */
public class tCassandraInputSparkProperties extends ComponentProperties {
    /**
     * named constructor to be used is these properties are nested in other properties. Do not subclass this method for
     * initialization, use {@link #init()} instead.
     *
     * @param name
     */
    public tCassandraInputSparkProperties(String name) {
        super(name);
    }

    public Property keySpace = newString("KEY_SPACE");
    public Property columnFamily = newString("COLUMN_FAMILY");
    public Property columnFunctionTables = newTable("SELECTED_COLUMN_FUNCTION", newString("COLUMN"), newEnum("FUNCTION", "TTL", "WRITETIME"));
    public Property filterConditionTables = newTable("FILTER_CONDITION", newString("COLUMN"), newEnum("FUNCTION", "EQ", "LT", "GT", "LE", "GE", "CONTAINS", "CONTAINSKEY", "IN"), newString("VALUE"));
    public Property order = newEnum("ORDER", "NONE", "ASC", "DESC");
    public Property useLimit = newBoolean("USE_LIMIT", false);
    public Property limit = newLong("LIMIT", 100l);
    public SchemaProperties schema = new SchemaProperties("schema");


}
