package org.talend.components.cassandra.metadata;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.DataType;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.schema.SchemaFactory;
import org.talend.components.cassandra.type.CassandraAPITypeMapping;
import org.talend.components.cassandra.type.CassandraBaseType;

import java.util.List;

/**
 * Created by bchen on 16-1-15.
 */
public class CassandraMetadata {
    public void initSchema(ComponentProperties properties) {
        CassandraMetadataProperties props = (CassandraMetadataProperties) properties;

        Cluster cluster = Cluster.builder().addContactPoints(props.host.getStringValue()).withPort(Integer.valueOf(props.port.getStringValue())).build();
        List<ColumnMetadata> columns = cluster.getMetadata().getKeyspace(props.keyspace.getStringValue()).getTable(props.columnFamily.getStringValue()).getColumns();
        for (ColumnMetadata column : columns) {
            DataType type = column.getType();
            props.schema.addSchemaChild(SchemaFactory.newDataSchemaElement(CassandraBaseType.FAMILY_NAME, column.getName(), CassandraAPITypeMapping.getType(type.getName())));
        }
    }
}
