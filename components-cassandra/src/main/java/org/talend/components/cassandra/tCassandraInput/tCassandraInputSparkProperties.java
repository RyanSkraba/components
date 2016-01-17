package org.talend.components.cassandra.tCassandraInput;

import org.talend.components.api.properties.Property;
import org.talend.components.cassandra.metadata.CassandraMetadataProperties;

import static org.talend.components.api.properties.PropertyFactory.*;

/**
 * Created by bchen on 16-1-13.
 */
public class tCassandraInputSparkProperties extends CassandraMetadataProperties {
    /**
     * named constructor to be used is these properties are nested in other properties. Do not subclass this method for
     * initialization, use {@link #init()} instead.
     *
     * @param name
     */
    public tCassandraInputSparkProperties(String name) {
        super(name);
    }

    //TODO how to hidden host/port in this component?
    public Property columnFunctionTables = newTable("columnFunctionTables", newString("COLUMN"), newEnum("FUNCTION", "TTL", "WRITETIME"));
    public Property filterConditionTables = newTable("filterConditionTables", newString("COLUMN"), newEnum("FUNCTION", "EQ", "LT", "GT", "LE", "GE", "CONTAINS", "CONTAINSKEY", "IN"), newString("VALUE"));
    public Property order = newEnum("order", "NONE", "ASC", "DESC");
    public Property useLimit = newBoolean("useLimit", false);
    public Property limit = newLong("limit", 100l);

}
