package org.talend.components.cassandra.tCassandraConnection;

import org.talend.components.cassandra.tCassandraConfiguration.tCassandraConfigurationProperties;

/**
 * Created by bchen on 16-1-14.
 */
public class tCassandraConnectionProperties extends tCassandraConfigurationProperties {
    /**
     * named constructor to be used is these properties are nested in other properties. Do not subclass this method for
     * initialization, use {@link #init()} instead.
     *
     * @param name
     */
    public tCassandraConnectionProperties(String name) {
        super(name);
    }

}
