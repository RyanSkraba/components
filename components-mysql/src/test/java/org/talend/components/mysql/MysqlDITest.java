package org.talend.components.mysql;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.input.Reader;
import org.talend.components.api.component.runtime.input.Split;
import org.talend.components.api.component.runtime.metadata.Metadata;
import org.talend.components.api.runtime.row.BaseRowStruct;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.api.schema.column.type.common.TypeMapping;
import org.talend.components.api.schema.internal.DataSchemaElement;
import org.talend.components.mysql.metadata.MysqlMetadata;
import org.talend.components.mysql.tMysqlInput.MysqlSource;
import org.talend.components.mysql.tMysqlInput.tMysqlInputProperties;
import org.talend.components.mysql.type.MysqlTalendTypesRegistry;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bchen on 16-1-18.
 */
public class MysqlDITest {
    public static final String HOST = "localhost";
    public static final String PORT = "3306";
    public static final String USER = "root";
    public static final String PASS = "mysql";
    public static final String DBNAME = "tuj";
    public static final String TABLE = "test";
    public static final String PROPERTIES = "noDatetimeStringSync=true";
    Connection conn;

    @Before
    public void prepare() {
        TypeMapping.registryTypes(new MysqlTalendTypesRegistry());

        try {
            Class.forName("org.gjt.mm.mysql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        String url = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DBNAME + "?" + PROPERTIES;

        try {
            conn = DriverManager.getConnection(url, USER, PASS);
            Statement statement = conn.createStatement();
            statement.execute("drop table tuj.test");
            statement.execute("create table tuj.test(id int PRIMARY KEY, name VARCHAR(50))");
            statement.execute("insert into tuj.test(id, name) values(1,'hello')");
            statement.execute("insert into tuj.test(id, name) values(2,'world')");
            statement.execute("insert into tuj.test(id, name) values(3,'mysql')");
            statement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSplit() {
        tMysqlInputProperties props = new tMysqlInputProperties("tMysqlInput_1");
        props.initForRuntime();
        props.HOST.setValue(HOST);
        props.PORT.setValue(PORT);
        props.USER.setValue(USER);
        props.PASS.setValue(PASS);
        props.DBNAME.setValue(DBNAME);
        props.PROPERTIES.setValue(PROPERTIES);
        props.TABLE.setValue(TABLE);
        props.QUERY.setValue("select id, name from tuj.test");

        Metadata metadata = new MysqlMetadata();
        metadata.initSchema(props);

        MysqlSource source = new MysqlSource();
        source.init(props);

        Map<String, SchemaElement.Type> row_metadata = new HashMap<>();

        List<SchemaElement> fields = source.getSchema();
        for (SchemaElement field : fields) {
            row_metadata.put(field.getName(), field.getType());
        }
        List<BaseRowStruct> rows = new ArrayList<>();

        Split[] splits = source.getSplit(2);

        Reader reader = source.getRecordReader(splits[0]);
        while (reader.advance()) {
            BaseRowStruct baseRowStruct = new BaseRowStruct(row_metadata);
            for (SchemaElement column : fields) {
                DataSchemaElement dataFiled = (DataSchemaElement) column;
                try {
                    baseRowStruct.put(dataFiled.getName(), TypeMapping.convert(TypeMapping.getDefaultTalendType(source.getFamilyName(), dataFiled.getAppColType()),
                            dataFiled.getType(), dataFiled.getAppColType().newInstance().retrieveTValue(reader.getCurrent(), dataFiled.getAppColName())));
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            rows.add(baseRowStruct);
        }

        Assert.assertEquals(1, rows.size());

        rows.clear();
        reader = source.getRecordReader(splits[1]);
        while (reader.advance()) {
            BaseRowStruct baseRowStruct = new BaseRowStruct(row_metadata);
            for (SchemaElement column : fields) {
                DataSchemaElement dataFiled = (DataSchemaElement) column;
                try {
                    baseRowStruct.put(dataFiled.getName(), TypeMapping.convert(TypeMapping.getDefaultTalendType(source.getFamilyName(), dataFiled.getAppColType()),
                            dataFiled.getType(), dataFiled.getAppColType().newInstance().retrieveTValue(reader.getCurrent(), dataFiled.getAppColName())));
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            rows.add(baseRowStruct);
        }
        Assert.assertEquals(2, rows.size());
    }
}
