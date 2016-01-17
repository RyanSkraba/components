package org.talend.dataflow.cassandra;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.io.Reader;
import org.talend.components.api.component.runtime.io.SingleSplit;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.api.schema.column.type.common.TypeMapping;
import org.talend.components.api.schema.internal.DataSchemaElement;
import org.talend.components.cassandra.io.CassandraSource;
import org.talend.components.cassandra.metadata.CassandraMetadata;
import org.talend.components.cassandra.tCassandraInput.tCassandraInputDIProperties;
import org.talend.components.cassandra.type.CassandraBaseType;
import org.talend.components.cassandra.type.CassandraTalendTypesRegistry;
import org.talend.row.BaseRowStruct;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bchen on 16-1-10.
 */
public class CassandraSourceTest {
    @Before
    public void prepare() {
        TypeMapping.registryTypes(new CassandraTalendTypesRegistry());
    }

    @Test
    public void test() {
        tCassandraInputDIProperties props = new tCassandraInputDIProperties("tCassandraInput_1");
        props.init();
        props.host.setValue("localhost");
        props.port.setValue("9042");
        props.useAuth.setValue(false);
        props.keyspace.setValue("ks");
        props.columnFamily.setValue("test");
        props.query.setValue("select name from ks.test");

        CassandraMetadata m = new CassandraMetadata();
        m.initSchema(props);

        CassandraSource cassandra = new CassandraSource();
        cassandra.init(props);

        Map<String, SchemaElement.Type> row_metadata = new HashMap<>();

        List<SchemaElement> fields = cassandra.getSchema();
        for (SchemaElement field : fields) {
            row_metadata.put(field.getName(), field.getType());
        }
        BaseRowStruct baseRowStruct = new BaseRowStruct(row_metadata);

        Reader recordReader = cassandra.getRecordReader(new SingleSplit());
        while (recordReader.advance()) {
            for (SchemaElement column : fields) {
                DataSchemaElement dataFiled = (DataSchemaElement) column;
                try {
                    baseRowStruct.put(dataFiled.getName(), TypeMapping.convert(TypeMapping.getDefaultTalendType(CassandraBaseType.FAMILY_NAME, dataFiled.getAppColType()),
                            dataFiled.getType(), dataFiled.getAppColType().newInstance().retrieveTValue(recordReader.getCurrent(), dataFiled.getAppColName())));
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            System.out.println(baseRowStruct);
        }
    }
}
