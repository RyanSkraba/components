package org.talend.dataflow.cassandra;

import com.datastax.driver.core.Row;
import org.junit.Test;
import org.talend.components.api.component.io.Reader;
import org.talend.components.api.component.io.SingleSplit;
import org.talend.components.cassandra.io.CassandraSource;
import org.talend.components.cassandra.tCassandraInput.tCassandraInputDIProperties;
import org.talend.components.cassandra.type.TEXT;
import org.talend.row.BaseRowStruct;
import org.talend.schema.Column;
import org.talend.schema.type.TBaseType;
import org.talend.schema.type.TString;
import org.talend.schema.type.TypeMapping;

import java.util.*;

/**
 * Created by bchen on 16-1-10.
 */
public class CassandraSourceTest {
    @Test
    public void test() {
        CassandraSource cassandra = new CassandraSource();
        tCassandraInputDIProperties props = new tCassandraInputDIProperties("tCassandraInput_1");
        props.init();
        props.host.setValue("localhost");
        props.port.setValue("9042");
        props.useAuth.setValue(false);
        props.query.setValue("select name from ks.test");
        cassandra.init(props);
        Reader<Row> recordReader = cassandra.getRecordReader(new SingleSplit());
        while (recordReader.advance()) {
            Row row = recordReader.getCurrent();
            Map<String, Class<? extends TBaseType>> row_metadata = new HashMap<>();
            row_metadata.put("name", TString.class);
            BaseRowStruct baseRowStruct = new BaseRowStruct(row_metadata);
            List<Column> metadata = new ArrayList<>();
            Column col1 = new Column(false, "name", TEXT.class);
            metadata.addAll(Arrays.asList(new Column[]{col1}));
            metadata.get(0).setTalendType("name", TString.class);
            for (Column column : metadata) {
                try {
                    baseRowStruct.put(column.getCol_name(), TypeMapping.convert(column.getApp_col_type().newInstance().getDefaultTalendType(),
                            column.getCol_type(), column.getApp_col_type().newInstance().retrieveTValue(row, column.getApp_col_name())));
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
