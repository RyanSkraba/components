package org.talend.components.mysql;

import com.google.cloud.dataflow.sdk.Pipeline;
import com.google.cloud.dataflow.sdk.coders.MapCoder;
import com.google.cloud.dataflow.sdk.coders.StringUtf8Coder;
import com.google.cloud.dataflow.sdk.io.Read;
import com.google.cloud.dataflow.sdk.io.TextIO;
import com.google.cloud.dataflow.sdk.options.PipelineOptions;
import com.google.cloud.dataflow.sdk.options.PipelineOptionsFactory;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.metadata.Metadata;
import org.talend.components.api.schema.column.type.common.TypeMapping;
import org.talend.components.mysql.metadata.MysqlMetadata;
import org.talend.components.mysql.tMysqlInput.Dataflow.DFBoundedSource;
import org.talend.components.mysql.tMysqlInput.MysqlSource;
import org.talend.components.mysql.tMysqlInput.tMysqlInputProperties;
import org.talend.components.mysql.type.MysqlTalendTypesRegistry;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by bchen on 16-1-18.
 */
public class MysqlDataflowTest {
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
    public void test() {
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

        DFBoundedSource source = new DFBoundedSource(MysqlSource.class, props);
        PipelineOptions options = PipelineOptionsFactory.create();
        Pipeline p = Pipeline.create(options);
        p.apply(Read.from(source).named("tMysqlInput_1")).apply(TextIO.Write.named("out").withCoder(MapCoder.of(StringUtf8Coder.of(), StringUtf8Coder.of())).to("/tmp/dffile"));

        p.run();

    }

}
