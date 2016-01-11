package org.talend.components.cassandra.tCassandraInput;

import org.talend.components.api.properties.Property;
import org.talend.components.cassandra.tCassandraConnection.tCassandraConnectionProperties;
import org.talend.components.common.SchemaProperties;

import static org.talend.components.api.properties.PropertyFactory.newString;

/**
 * Created by bchen on 16-1-14.
 */
public class tCassandraInputDIProperties extends tCassandraConnectionProperties {
    /**
     * named constructor to be used is these properties are nested in other properties. Do not subclass this method for
     * initialization, use {@link #init()} instead.
     *
     * @param name
     */
    public tCassandraInputDIProperties(String name) {
        super(name);
    }

//    public Property keySpace = newString("KEY_SPACE");
//    public Property columnFamily = newString("COLUMN_FAMILY");
    public Property query = newString("QUERY", "select id, name from employee");
    public SchemaProperties schema = new SchemaProperties("schema");
}
