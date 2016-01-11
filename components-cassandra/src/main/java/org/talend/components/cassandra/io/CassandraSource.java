package org.talend.components.cassandra.io;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.talend.components.api.component.io.Reader;
import org.talend.components.api.component.io.Source;
import org.talend.components.api.component.io.Split;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.cassandra.tCassandraInput.tCassandraInputDIProperties;

/**
 * Created by bchen on 16-1-10.
 */
public class CassandraSource implements Source<Row> {
    Session connection;
    Cluster cluster;
    tCassandraInputDIProperties props;

    @Override
    public void init(ComponentProperties properties) {
        props = (tCassandraInputDIProperties) properties;
        // TODO connection pool
        Cluster.Builder clusterBuilder = Cluster.builder().addContactPoints(props.host.getStringValue().split(",")).withPort(Integer.valueOf(props.port.getStringValue()));
        if (props.useAuth.getBooleanValue()) {
            clusterBuilder.withCredentials(props.username.getStringValue(), props.password.getStringValue());
        }

        cluster = clusterBuilder.build();
        connection = cluster.connect();
    }

    @Override
    public void close() {
        //TODO connection pool
        connection.close();
        cluster.close();
    }

    @Override
    public Reader<Row> getRecordReader(Split split) {
        return new CassandraReader(props, connection, split);
    }

    @Override
    public boolean supportSplit() {
        return false;
    }

    @Override
    public Split[] getSplit(int num) {
        return new Split[0];
    }

    public class CassandraReader implements Reader<Row> {
        ResultSet rs;
        Row row;

        CassandraReader(tCassandraInputDIProperties props, Session connection, Split split) {
            rs = connection.execute(props.query.getStringValue());
        }

        @Override
        public boolean start() {
            return advance();
        }

        @Override
        public boolean advance() {
            Row next = rs.one();
            if (next == null) {
                return false;
            } else {
                row = next;
                return true;
            }
        }


        @Override
        public Row getCurrent() {
            return row;
        }

        @Override
        public void close() {
        }
    }


}
