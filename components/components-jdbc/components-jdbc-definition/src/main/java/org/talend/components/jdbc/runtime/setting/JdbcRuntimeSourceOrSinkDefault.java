package org.talend.components.jdbc.runtime.setting;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.config.jdbc.Dbms;
import org.talend.daikon.NamedThing;

public abstract class JdbcRuntimeSourceOrSinkDefault implements JdbcRuntimeSourceOrSink {

    private static final long serialVersionUID = 1L;

    private Connection conn;

    public void setDBTypeMapping(Dbms mapping) {
        
    }
    
    @Override
    public Schema getSchemaFromQuery(RuntimeContainer runtime, String query) {
        return null;
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer runtime) throws IOException {
        return null;
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer runtime, String tableName) throws IOException {
        return null;
    }

    public Connection getConnection(RuntimeContainer runtime) throws ClassNotFoundException, SQLException {
        if (conn == null) {
            conn = connect(runtime);
        }
        return conn;
    }

    public void initConnection(RuntimeContainer runtime) throws ClassNotFoundException, SQLException {
        conn = connect(runtime);
    }

    protected Connection connect(RuntimeContainer runtime) throws ClassNotFoundException, SQLException {
        return null;
    }

}
