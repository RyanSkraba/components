package org.talend.dataflow.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.input.Reader;
import org.talend.components.api.component.runtime.input.SingleSplit;
import org.talend.components.api.component.runtime.metadata.Metadata;
import org.talend.components.api.runtime.row.BaseRowStruct;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.api.schema.column.type.common.TypeMapping;
import org.talend.components.api.schema.internal.DataSchemaElement;
import org.talend.components.cassandra.io.CassandraSink;
import org.talend.components.cassandra.io.CassandraSource;
import org.talend.components.cassandra.metadata.CassandraMetadata;
import org.talend.components.cassandra.tCassandraInput.tCassandraInputDIProperties;
import org.talend.components.cassandra.tCassandraOutput.tCassandraOutputDIProperties;
import org.talend.components.cassandra.type.CassandraTalendTypesRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bchen on 16-1-10.
 */
public class CassandraDITest {
    private static final String HOST = "localhost";
    private static final String PORT = "9042";
    private static final String KEYSPACE = "ks";
    Session connect;

    @Before
    public void prepare() {
        TypeMapping.registryTypes(new CassandraTalendTypesRegistry());

        Cluster cluster = Cluster.builder().addContactPoints(HOST).withPort(Integer.valueOf(PORT)).build();
        connect = cluster.connect();
        connect.execute("drop KEYSPACE ks");
        connect.execute("create KEYSPACE ks WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1}");
        connect.execute("create TABLE ks.test ( name text PRIMARY KEY )");
        connect.execute("insert into ks.test (name) values ('hello')");
        connect.execute("insert into ks.test (name) values ('world')");

        connect.execute("create TABLE ks.test2 ( name text PRIMARY KEY )");

    }

    @Test
    public void test() {
        tCassandraInputDIProperties props = new tCassandraInputDIProperties("tCassandraInput_1");
        props.init();
        props.host.setValue(HOST);
        props.port.setValue(PORT);
        props.useAuth.setValue(false);
        props.keyspace.setValue(KEYSPACE);
        props.columnFamily.setValue("test");
        props.query.setValue("select name from ks.test");


        tCassandraOutputDIProperties outProps = new tCassandraOutputDIProperties("tCassandraOutput_1");
        outProps.init();
        outProps.host.setValue(HOST);
        outProps.port.setValue(PORT);
        outProps.keyspace.setValue(KEYSPACE);
        outProps.columnFamily.setValue("test2");
        outProps.dataAction.setValue("INSERT");
        outProps.useUnloggedBatch.setValue(false);

        Metadata m = new CassandraMetadata();
        m.initSchema(props);
        m.initSchema(outProps);

        CassandraSource source = new CassandraSource();
        source.init(props);

        CassandraSink sink = new CassandraSink();
        sink.init(outProps);
        CassandraSink.CassandraRecordWriter writer = sink.getWriter();

        Map<String, SchemaElement.Type> row_metadata = new HashMap<>();

        List<SchemaElement> fields = source.getSchema();
        for (SchemaElement field : fields) {
            row_metadata.put(field.getName(), field.getType());
        }
        BaseRowStruct baseRowStruct = new BaseRowStruct(row_metadata);

        Reader recordReader = source.getRecordReader(new SingleSplit());
        while (recordReader.advance()) {
            for (SchemaElement column : fields) {
                DataSchemaElement dataFiled = (DataSchemaElement) column;
                try {
                    baseRowStruct.put(dataFiled.getName(), TypeMapping.convert(TypeMapping.getDefaultTalendType(source.getFamilyName(), dataFiled.getAppColType()),
                            dataFiled.getType(), dataFiled.getAppColType().newInstance().retrieveTValue(recordReader.getCurrent(), dataFiled.getAppColName())));
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                writer.write(baseRowStruct);
            }
        }

        ResultSet rs = connect.execute("select name from ks.test2");
        List<String> result = new ArrayList<>();
        for (Row r : rs) {
            result.add(r.getString("name"));
        }
        Assert.assertEquals(2, result.size());
        Assert.assertEquals("hello", result.get(0));
        Assert.assertEquals("world", result.get(1));
    }
}
