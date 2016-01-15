package org.talend.components.cassandra.metadata;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.DataType;
import org.talend.components.cassandra.type.ASCII;
import org.talend.components.cassandra.type.CassandraBaseType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bchen on 16-1-15.
 */
public class CassandraMetadata {
    public Map<String, Class<? extends CassandraBaseType>> retrieveMetadata(String table) {
        Map<String, Class<? extends CassandraBaseType>> tabCols = new HashMap<>();

        Cluster cluster = Cluster.builder().addContactPoints("localhost").withPort(Integer.valueOf("9042")).build();
        List<ColumnMetadata> columns = cluster.getMetadata().getKeyspace("ks").getTable(table).getColumns();
        for (ColumnMetadata column : columns) {
            DataType type = column.getType();
            if (type == DataType.ascii()) {
                tabCols.put(column.getName(), ASCII.class);
            }
        }
        return tabCols;

    }

}
