package org.talend.components.cassandra.tCassandraOutput;

import org.talend.components.api.properties.Property;
import org.talend.components.api.properties.PropertyFactory;
import org.talend.components.cassandra.metadata.CassandraMetadataProperties;

/**
 * Created by bchen on 16-1-17.
 */
public class tCassandraOutputDIProperties extends CassandraMetadataProperties {
    /**
     * named constructor to be used is these properties are nested in other properties. Do not subclass this method for
     * initialization, use {@link #init()} instead.
     *
     * @param name
     */
    public tCassandraOutputDIProperties(String name) {
        super(name);
    }

    public Property actionOnKeyspace = PropertyFactory.newEnum("actionOnKeyspace", "NONE", "DROP_CREATE", "CREATE", "CREATE_IF_NOT_EXISTS", "DROP_IF_EXISTS_AND_CREATE");
    public Property actionOnColumnFamily = PropertyFactory.newEnum("actionOnColumnFamily", "NONE", "DROP_CREATE", "CREATE", "CREATE_IF_NOT_EXISTS", "DROP_IF_EXISTS_AND_CREATE", "TRUNCATE");

    public Property dataAction = PropertyFactory.newEnum("dataAction", "INSERT", "UPDATE", "DELETE");
    public Property dieOnError = PropertyFactory.newBoolean("dieOnError", false);

    public Property useUnloggedBatch = PropertyFactory.newBoolean("useUnloggedBatch", false);
    public Property batchSize = PropertyFactory.newInteger("batchSize", 10000);
    public Property insertIfNotExists = PropertyFactory.newBoolean("insertIfNotExists", false);
    public Property deleteIfExists = PropertyFactory.newBoolean("deleteIfExists", false);
    public Property usingTTL = PropertyFactory.newBoolean("usingTTL", false);
    public Property ttl = PropertyFactory.newEnum("ttl");
    public Property usingTimestamp = PropertyFactory.newBoolean("usingTimestamp", false);
    public Property timestamp = PropertyFactory.newEnum("timestamp");
    

}
