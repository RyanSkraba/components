package org.talend.components.cassandra.io;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.talend.components.api.component.runtime.io.Reader;
import org.talend.components.api.component.runtime.io.SingleSplit;
import org.talend.components.api.component.runtime.io.Source;
import org.talend.components.api.component.runtime.io.Split;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.schema.Schema;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.cassandra.tCassandraInput.tCassandraInputDIProperties;
import org.talend.components.cassandra.type.CassandraBaseType;

import java.util.List;

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

    @Override
    public List<SchemaElement> getSchema() {
        return ((Schema) props.schema.getValue(props.schema.schema)).getRoot().getChildren();
    }

    public class CassandraReader implements Reader<Row> {
        ResultSet rs;
        Row row;

        CassandraReader(tCassandraInputDIProperties props, Session connection, Split split) {
            if (split instanceof SingleSplit) {
                rs = connection.execute(props.query.getStringValue());
            }
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

    @Override
    public String getFamilyName() {
        return CassandraBaseType.FAMILY_NAME;
    }
}
