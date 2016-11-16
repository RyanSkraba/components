/**
 *
 */
package org.talend.components.snowflake.runtime;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.snowflake.SnowflakeConnectionProperties;
import org.talend.components.snowflake.SnowflakeProvideConnectionProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SnowflakeSourceOrSink implements SourceOrSink {

    private static final long serialVersionUID = 1L;

    private transient static final Logger LOG = LoggerFactory.getLogger(SnowflakeSourceOrSink.class);

    protected SnowflakeProvideConnectionProperties properties;

    protected static final String KEY_CONNECTION = "Connection";
    protected static final String KEY_CONNECTION_PROPERTIES = "ConnectionProperties";

    @Override
    public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        this.properties = (SnowflakeProvideConnectionProperties) properties;
        return ValidationResult.OK;
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        try {
            connect(container);
        } catch (Exception ex) {
            return exceptionToValidationResult(ex);
        }
        ValidationResult vr = new ValidationResult();
        vr.setStatus(Result.OK);
        vr.setMessage("Connection Successful");
        return vr;
    }

    public static ValidationResult exceptionToValidationResult(Exception ex) {
        ValidationResult vr = new ValidationResult();
        vr.setMessage(ex.getMessage());
        vr.setStatus(ValidationResult.Result.ERROR);
        return vr;
    }

    public static ValidationResult validateConnection(SnowflakeProvideConnectionProperties properties) {
        SnowflakeSourceOrSink sss = new SnowflakeSourceOrSink();
        sss.initialize(null, (ComponentProperties) properties);
        try {
            sss.connect(null);
            // Make sure we can get the schema names, as that tests that all of the connection parameters are really OK
            sss.getSchemaNames((RuntimeContainer) null);
        } catch (Exception ex) {
            return exceptionToValidationResult(ex);
        }
        ValidationResult vr = new ValidationResult();
        vr.setStatus(Result.OK);
        vr.setMessage("Connection Successful");
        return vr;
    }

    public SnowflakeConnectionProperties getEffectiveConnectionProperties(RuntimeContainer container) {
        SnowflakeConnectionProperties connProps = properties.getConnectionProperties();
        String refComponentId = connProps.getReferencedComponentId();
        // Using another component's connection
        if (refComponentId != null) {
            // In a runtime container
            if (container != null) {
                return (SnowflakeConnectionProperties) container.getComponentData(refComponentId, KEY_CONNECTION_PROPERTIES);
            }
            // Design time
            return connProps.getReferencedConnectionProperties();
        }
        return connProps;
    }

    protected Connection connect(RuntimeContainer container) throws IOException {

        Connection conn = null;

        SnowflakeConnectionProperties connProps = properties.getConnectionProperties();
        String refComponentId = connProps.getReferencedComponentId();
        // Using another component's connection
        if (refComponentId != null) {
            // In a runtime container
            if (container != null) {
                conn = (Connection) container.getComponentData(refComponentId, KEY_CONNECTION);
                if (conn != null)
                    return conn;
                throw new IOException("Referenced component: " + refComponentId + " not connected");
            }
            // Design time
            connProps = connProps.getReferencedConnectionProperties();
            // FIXME This should not happen - but does as of now
            if (connProps == null)
                throw new IOException("Referenced component: " + refComponentId + " does not have properties set");
        }

        // Establish a new connection
        String queryString = "";

        String user = connProps.userPassword.userId.getStringValue();
        String password = connProps.userPassword.password.getStringValue();
        String account = connProps.account.getStringValue();

        String warehouse = connProps.warehouse.getStringValue();
        String db = connProps.db.getStringValue();
        String schema = connProps.schemaName.getStringValue();

        String role = connProps.role.getStringValue();
        String tracing = connProps.tracing.getStringValue();

        if (null != warehouse && !"".equals(warehouse)) {
            queryString = queryString + "warehouse=" + warehouse;
        }
        if (null != db && !"".equals(db)) {
            queryString = queryString + "&db=" + db;
        }
        if (null != schema && !"".equals(schema)) {
            queryString = queryString + "&schema=" + schema;
        }

        if (null != role && !"".equals(role)) {
            queryString = queryString + "&role=" + role;
        }
        if (null != tracing && !"".equals(tracing)) {
            queryString = queryString + "&tracing=" + tracing;
        }
        String connectionURL = "jdbc:snowflake://" + account + ".snowflakecomputing.com" + "/?" + queryString;
        String JDBC_DRIVER = "com.snowflake.client.jdbc.SnowflakeDriver";

        try {
            Driver driver = (Driver) Class.forName(JDBC_DRIVER).newInstance();
            DriverManager.registerDriver(new DriverWrapper(driver));

            conn = DriverManager.getConnection(connectionURL, user, password);
        } catch (Exception e) {
            throw new IOException(e);
        }

        if (container != null) {
            container.setComponentData(container.getCurrentComponentId(), KEY_CONNECTION, conn);
            container.setComponentData(container.getCurrentComponentId(), KEY_CONNECTION_PROPERTIES, connProps);
        }

        return conn;
    }

    public static List<NamedThing> getSchemaNames(RuntimeContainer container, SnowflakeConnectionProperties properties)
            throws IOException {
        SnowflakeSourceOrSink ss = new SnowflakeSourceOrSink();
        ss.initialize(null, properties);
        return ss.getSchemaNames(container);
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException {
        return getSchemaNames(container, connect(container));
    }

    protected String getCatalog(SnowflakeConnectionProperties connProps) {
        return connProps.db.getStringValue();
    }

    protected String getDbSchema(SnowflakeConnectionProperties connProps) {
        return connProps.schemaName.getStringValue();
    }

    protected List<NamedThing> getSchemaNames(RuntimeContainer container, Connection connection) throws IOException {
        // Returns the list with a table names (for the wh, db and schema)
        List<NamedThing> returnList = new ArrayList<>();
        SnowflakeConnectionProperties connProps = getEffectiveConnectionProperties(container);
        try {
            DatabaseMetaData metaData = connection.getMetaData();

            // Fetch all tables in the db and schema provided
            String[] types = {"TABLE"};
            ResultSet resultIter = metaData.getTables(getCatalog(connProps), getDbSchema(connProps), null, types);
            String tableName = null;
            while (resultIter.next()) {
                tableName = resultIter.getString("TABLE_NAME");
                returnList.add(new SimpleNamedThing(tableName, tableName));
            }
        } catch (SQLException se) {
            throw new IOException("Error when searching for tables in: " + getCatalog(connProps) + "." + getDbSchema(connProps) + ": " + se.getMessage(), se);
        }
        return returnList;
    }

    public static Schema getSchema(RuntimeContainer container, SnowflakeProvideConnectionProperties properties, String table)
            throws IOException {
        SnowflakeSourceOrSink ss = new SnowflakeSourceOrSink();
        ss.initialize(null, (ComponentProperties) properties);
        return ss.getSchema(container, ss.connect(container), table);
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer container, String schemaName) throws IOException {
        return getSchema(container, connect(container), schemaName);
    }

    protected Schema getSchema(RuntimeContainer container, Connection connection, String tableName) throws IOException {
        Schema tableSchema = null;

        SnowflakeConnectionProperties connProps = getEffectiveConnectionProperties(container);
        try {
            DatabaseMetaData metaData = connection.getMetaData();

            ResultSet resultSet = metaData.getColumns(getCatalog(connProps), getDbSchema(connProps), tableName, null);
            tableSchema = SnowflakeAvroRegistry.get().inferSchema(resultSet);
            // FIXME - I18N for this message
            if (tableSchema == null)
                throw new IOException("Table: " + tableName + " not found");

            // Update the schema with Primary Key details
            // FIXME - move this into the inferSchema stuff
            ResultSet keysIter = metaData.getPrimaryKeys(getCatalog(connProps), getDbSchema(connProps), tableName);

            List<String> pkColumns = new ArrayList<>(); // List of Primary Key columns for this table
            while (keysIter.next()) {
                pkColumns.add(keysIter.getString("COLUMN_NAME"));
            }

            for (Field f : tableSchema.getFields()) {
                if (pkColumns.contains(f.name())) {
                    f.schema().addProp(SchemaConstants.TALEND_COLUMN_IS_KEY, "true");
                }
            }

        } catch (SQLException se) {
            throw new IOException(se);
        }

        return tableSchema;

    }

    public class DriverWrapper implements Driver {

        private Driver driver;

        public DriverWrapper(Driver d) {
            this.driver = d;
        }

        public boolean acceptsURL(String u) throws SQLException {
            return this.driver.acceptsURL(u);
        }

        public Connection connect(String u, Properties p) throws SQLException {
            return this.driver.connect(u, p);
        }

        public int getMajorVersion() {
            return this.driver.getMajorVersion();
        }

        public int getMinorVersion() {
            return this.driver.getMinorVersion();
        }

        public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) throws SQLException {
            return this.driver.getPropertyInfo(u, p);
        }

        public boolean jdbcCompliant() {
            return this.driver.jdbcCompliant();
        }

        @Override
        public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return this.driver.getParentLogger();
        }
    }

}
