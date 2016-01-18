package org.talend.components.mysql.metadata;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.schema.Schema;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.api.schema.column.type.common.TypeMapping;
import org.talend.components.api.schema.internal.DataSchemaElement;
import org.talend.components.mysql.type.MysqlTalendTypesRegistry;
import org.talend.components.mysql.type.Mysql_INT;
import org.talend.components.mysql.type.Mysql_VARCHAR;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Created by bchen on 16-1-18.
 */
public class MysqlMetadataTest {

    public static final String HOST = "localhost";
    public static final String PORT = "3306";
    public static final String USER = "root";
    public static final String PASS = "mysql";
    public static final String DBNAME = "tuj";
    public static final String TABLE = "test";
    public static final String PROPERTIES = "noDatetimeStringSync=true";

    @Before
    public void init() {
        TypeMapping.registryTypes(new MysqlTalendTypesRegistry());
        try {
            Class.forName("org.gjt.mm.mysql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        String url = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DBNAME + "?" + PROPERTIES;
        Connection conn;
        try {
            conn = DriverManager.getConnection(url, USER, PASS);
            Statement statement = conn.createStatement();
            statement.execute("drop table tuj.test");
            statement.execute("create table tuj.test(id int PRIMARY KEY, name VARCHAR(50))");
            statement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test() {
        MysqlMetadataProperties props = new MysqlMetadataProperties("Metadata");
        props.initForRuntime();
        props.HOST.setValue(HOST);
        props.PORT.setValue(PORT);
        props.USER.setValue(USER);
        props.PASS.setValue(PASS);
        props.DBNAME.setValue(DBNAME);
        props.PROPERTIES.setValue(PROPERTIES);
        props.TABLE.setValue(TABLE);
        MysqlMetadata metadata = new MysqlMetadata();
        metadata.initSchema(props);
        List<SchemaElement> columns = ((Schema) props.schema.schema.getValue()).getRoot().getChildren();
        Assert.assertEquals(2, columns.size());
        Assert.assertEquals("id", columns.get(0).getName());
        Assert.assertEquals(SchemaElement.Type.INT, columns.get(0).getType());
        Assert.assertEquals(Mysql_INT.class, ((DataSchemaElement) columns.get(0)).getAppColType());
        Assert.assertEquals("name", columns.get(1).getName());
        Assert.assertEquals(SchemaElement.Type.STRING, columns.get(1).getType());
        Assert.assertEquals(Mysql_VARCHAR.class, ((DataSchemaElement) columns.get(1)).getAppColType());

    }
}
