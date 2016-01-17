package org.talend.components.cassandra.metadata;

import org.talend.components.api.properties.Property;
import org.talend.components.api.properties.PropertyFactory;
import org.talend.components.cassandra.tCassandraConnection.tCassandraConnectionProperties;
import org.talend.components.common.SchemaProperties;

/**
 * Created by bchen on 16-1-17.
 */
public class CassandraMetadataProperties extends tCassandraConnectionProperties {
    /**
     * named constructor to be used is these properties are nested in other properties. Do not subclass this method for
     * initialization, use {@link #init()} instead.
     *
     * @param name
     */
    public CassandraMetadataProperties(String name) {
        super(name);
    }

    public Property keyspace = PropertyFactory.newString("KEY_SPACE");
    public Property columnFamily = PropertyFactory.newString("COLUMN_FAMILY");
    public SchemaProperties schema = new SchemaProperties("schema");
}
