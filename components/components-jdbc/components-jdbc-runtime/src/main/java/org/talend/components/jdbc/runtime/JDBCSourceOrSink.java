
// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.jdbc.runtime;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.common.avro.JDBCResultSetIndexedRecordConverter;
import org.talend.components.common.avro.JDBCTableMetadata;
import org.talend.components.common.config.jdbc.Dbms;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.common.datastore.DatastoreProperties;
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.ComponentConstants;
import org.talend.components.jdbc.JdbcComponentErrorsCode;
import org.talend.components.jdbc.RuntimeSettingProvider;
import org.talend.components.jdbc.avro.JDBCAvroRegistryString;
import org.talend.components.jdbc.avro.ResultSetStringRecordConverter;
import org.talend.components.jdbc.runtime.schemainfer.SchemaInferer;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.components.jdbc.runtime.setting.JdbcRuntimeSourceOrSinkDefault;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.avro.converter.IndexedRecordConverter;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.daikon.properties.ValidationResult;

/**
 * common JDBC runtime execution object
 *
 */
public class JDBCSourceOrSink extends JdbcRuntimeSourceOrSinkDefault {

    private static final Logger LOGGER = LoggerFactory.getLogger(JDBCSourceOrSink.class);

    private static final long serialVersionUID = 1L;

    public RuntimeSettingProvider properties;

    protected AllSetting setting;

    private transient IndexedRecordConverter<ResultSet, IndexedRecord> converter;

    private boolean work4dataprep = false;

    @Override
    public ValidationResult initialize(RuntimeContainer runtime, ComponentProperties properties) {
        this.properties = (RuntimeSettingProvider) properties;
        setting = this.properties.getRuntimeSetting();

        converter = new JDBCResultSetIndexedRecordConverter();
        ((JDBCResultSetIndexedRecordConverter) converter).setInfluencer(setting);

        return ValidationResult.OK;
    }

    // TODO adjust it, now only as a temp workaround for reuse current class in the datastore runtime
    public ValidationResult initialize(RuntimeContainer runtime, DatastoreProperties properties) {
        this.properties = (RuntimeSettingProvider) properties;
        setting = this.properties.getRuntimeSetting();

        converter = new ResultSetStringRecordConverter();

        work4dataprep = true;

        return ValidationResult.OK;
    }

    // TODO adjust it, now only as a temp workaround for reuse current class in the dataset runtime
    @SuppressWarnings("rawtypes")
    public ValidationResult initialize(RuntimeContainer runtime, DatasetProperties properties) {
        this.properties = (RuntimeSettingProvider) properties;
        setting = this.properties.getRuntimeSetting();

        converter = new ResultSetStringRecordConverter();

        work4dataprep = true;

        return ValidationResult.OK;
    }

    private Dbms typeMapping = null;

    public void setDBTypeMapping(Dbms mapping) {
        typeMapping = mapping;
    }

    @Override
    public ValidationResult validate(RuntimeContainer runtime) {
        return JdbcRuntimeUtils.validate(runtime, this);
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer runtime) throws IOException {
        List<NamedThing> result = new ArrayList<>();
        try (Connection conn = connect(runtime)) {
            DatabaseMetaData dbMetaData = conn.getMetaData();

            Set<String> tableTypes = getAvailableTableTypes(dbMetaData);

            String database_schema = getDatabaseSchema();

            try (ResultSet resultset = dbMetaData.getTables(null, database_schema, null, tableTypes.toArray(new String[0]))) {
                while (resultset.next()) {
                    String tablename = resultset.getString("TABLE_NAME");
                    if (tablename == null) {
                        tablename = resultset.getString("SYNONYM_NAME");
                    }
                    result.add(new SimpleNamedThing(tablename, tablename));
                }
            }
        } catch (Exception e) {
            throw CommonUtils.newComponentException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e,
                    ExceptionContext.withBuilder().put("message", e.getMessage()).build());
        }
        return result;
    }

    /**
     * get database schema for database special
     * @return
     */
    private String getDatabaseSchema() {
        String jdbc_url = setting.getJdbcUrl();
        String username = setting.getUsername();
        if(jdbc_url!=null && username!=null && jdbc_url.contains("oracle")) {
            return username.toUpperCase();
        }
        return null;
    }

    private Set<String> getAvailableTableTypes(DatabaseMetaData dbMetaData) throws SQLException {
        Set<String> availableTableTypes = new HashSet<String>();
        List<String> neededTableTypes = Arrays.asList("TABLE", "VIEW", "SYNONYM");

        try (ResultSet rsTableTypes = dbMetaData.getTableTypes()) {
            while (rsTableTypes.next()) {
                String currentTableType = rsTableTypes.getString("TABLE_TYPE");
                if (currentTableType == null) {
                    currentTableType = "";
                }
                currentTableType = currentTableType.trim();
                if ("BASE TABLE".equalsIgnoreCase(currentTableType)) {
                    currentTableType = "TABLE";
                }
                if (neededTableTypes.contains(currentTableType)) {
                    availableTableTypes.add(currentTableType);
                }
            }
        }

        return availableTableTypes;
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer runtime, String tableName) throws IOException {
        try (Connection conn = connect(runtime)) {
            JDBCTableMetadata tableMetadata = new JDBCTableMetadata();
            tableMetadata.setDatabaseMetaData(conn.getMetaData()).setTablename(tableName);
            return infer(tableMetadata, runtime);
        } catch (Exception e) {
            throw CommonUtils.newComponentException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e,
                    ExceptionContext.withBuilder().put("message", e.getMessage()).build());
        }
    }

    public Schema getSchemaFromQuery(RuntimeContainer runtime, String query) {
        try (Connection conn = connect(runtime);
                Statement statement = conn.createStatement();
                ResultSet resultset = statement.executeQuery(query)) {
            ResultSetMetaData metadata = resultset.getMetaData();
            return infer(metadata, runtime);
        } catch (SQLSyntaxErrorException sqlSyntaxException) {
            throw CommonUtils.newComponentException(JdbcComponentErrorsCode.SQL_SYNTAX_ERROR, sqlSyntaxException);
        } catch (SQLException e) {
            throw CommonUtils.newComponentException(JdbcComponentErrorsCode.SQL_ERROR, e);
        } catch (ClassNotFoundException e) {
            throw CommonUtils.newComponentException(JdbcComponentErrorsCode.DRIVER_NOT_PRESENT_ERROR, e);
        }
    }

    public Connection connect(RuntimeContainer runtime) throws ClassNotFoundException, SQLException {
        AllSetting setting = properties.getRuntimeSetting();

        // connection component
        Connection conn = JdbcRuntimeUtils.createConnectionOrGetFromSharedConnectionPoolOrDataSource(runtime, setting,
                work4dataprep);

        if (setting.getUseAutoCommit()) {
            conn.setAutoCommit(setting.getAutocommit());
        }

        if (runtime != null) {
            // if you check the api, you will find the parameter is set to the wrong location, but it's right now, as we need to
            // keep the connection component can work with some old javajet components
            runtime.setComponentData(ComponentConstants.CONNECTION_KEY, runtime.getCurrentComponentId(), conn);
            runtime.setComponentData(ComponentConstants.URL_KEY, runtime.getCurrentComponentId(), setting.getJdbcUrl());
            runtime.setComponentData(ComponentConstants.USERNAME_KEY, runtime.getCurrentComponentId(), setting.getUsername());
        }

        return conn;
    }

    // adapter for the two interfaces, in future, should use the second one
    public Schema infer(JDBCTableMetadata tableMetadata, RuntimeContainer runtime) throws SQLException {
        if (work4dataprep) {
            return JDBCAvroRegistryString.get().inferSchema(tableMetadata);
        } else {
            Dbms mapping = getDBMapping(runtime);
            return SchemaInferer.infer(tableMetadata, mapping, setting.getEnableSpecialTableName());
        }
    }

    // adapter for the two interfaces, in future, should use the second one
    public Schema infer(ResultSetMetaData metadata, RuntimeContainer runtime) throws SQLException {
        if (work4dataprep) {
            return JDBCAvroRegistryString.get().inferSchema(metadata);
        } else {
            Dbms mapping = getDBMapping(runtime);
            return SchemaInferer.infer(metadata, mapping, setting.getEnableSpecialTableName());
        }
    }

    private Dbms getDBMapping(RuntimeContainer runtime) {
        Dbms mapping = null;

        if (typeMapping != null) {
            mapping = typeMapping;
        } else if (runtime != null) {
            URL mappingFileDir = (URL) runtime.getComponentData(runtime.getCurrentComponentId(),
                    ComponentConstants.MAPPING_URL_SUBFIX);
            mapping = CommonUtils.getMapping(mappingFileDir, setting, null, setting.getDbMapping());
        }
        return mapping;
    }

    public IndexedRecordConverter<ResultSet, IndexedRecord> getConverter() {
        return converter;
    }

    /*
    // as studio will do schema list retrieve by the old way, now the method is not useful.
    // work for the wizard : catalog show, TODO make it common
    public List<String> getDBCatalogs(RuntimeContainer runtime) throws ClassNotFoundException, SQLException {
        List<String> catalogs = new ArrayList<>();

        try (Connection conn = connect(runtime); ResultSet result = conn.getMetaData().getCatalogs()) {
            if (result == null) {
                return catalogs;
            }

            while (result.next()) {
                String catalog = result.getString("TABLE_CAT");
                if (catalog != null) {
                    catalogs.add(catalog);
                }
            }
        }

        return catalogs;
    }

    // as studio will do schema list retrieve by the old way, now the method is not useful.
    // work for the wizard : schema show after catalog TODO make it common
    public List<String> getDBSchemas(RuntimeContainer runtime, String catalog) throws ClassNotFoundException, SQLException {
        List<String> dbschemas = new ArrayList<>();

        try (Connection conn = connect(runtime); ResultSet result = conn.getMetaData().getSchemas()) {
            if (result == null) {
                return dbschemas;
            }

            while (result.next()) {
                String dbschema = result.getString("TABLE_SCHEM");
                if (dbschema == null) {
                    continue;
                }

                // filter by catalog name
                if (catalog != null) {
                    String catalogname = result.getString("TABLE_CATALOG");
                    if (catalog.equals(catalogname)) {
                        dbschemas.add(dbschema);
                    }
                } else {
                    dbschemas.add(dbschema);
                }
            }
        }

        return dbschemas;
    }

    // as studio will do schema list retrieve by the old way, now the method is not useful.
    // work for the wizard : table show after schema after catalog TODO make it common
    public List<ModuleMetadata> getDBTables(RuntimeContainer runtime, String catalog, String dbschema, String tableNamePattern,
            String[] tableTypes) throws ClassNotFoundException, SQLException {
        List<ModuleMetadata> tables = new ArrayList<>();

        try (Connection conn = connect(runtime);
                ResultSet result = conn.getMetaData().getTables(catalog, dbschema, tableNamePattern, tableTypes)) {
            if (result == null) {
                return tables;
            }

            while (result.next()) {
                String table = result.getString("TABLE_NAME");

                if (table == null) {
                    continue;
                }

                String type = result.getString("TABLE_TYPE");
                String comment = result.getString("REMARKS");

                String dbcatalog = result.getString("TABLE_CAT");
                String db_schema = result.getString("TABLE_SCHEM");

                tables.add(new ModuleMetadata(dbcatalog, db_schema, table, type, comment, null));
            }
        }

        return tables;
    }

    // as studio will do schema list retrieve by the old way, now the method is not useful.
    // work for the schemas store after click finish button TODO make it common
    public List<ModuleMetadata> getDBTables(RuntimeContainer runtime, ModuleMetadata tableid)
            throws ClassNotFoundException, SQLException {
        List<ModuleMetadata> tables = new ArrayList<>();

        try (Connection conn = connect(runtime);
                ResultSet result = conn.getMetaData().getTables(tableid.catalog, tableid.dbschema, tableid.name,
                        tableid.type == null ? null : new String[] { tableid.type })) {
            if (result == null) {
                return tables;
            }

            while (result.next()) {
                String table = result.getString("TABLE_NAME");

                if (table == null) {
                    continue;
                }

                String type = result.getString("TABLE_TYPE");
                String comment = result.getString("REMARKS");

                String dbcatalog = result.getString("TABLE_CAT");
                String db_schema = result.getString("TABLE_SCHEM");

                JDBCTableMetadata tableMetadata = new JDBCTableMetadata();
                tableMetadata.setDatabaseMetaData(conn.getMetaData()).setTablename(tableid.name);

                Schema schema = infer(tableMetadata, runtime);

                // as ModuleMetadata is invisible for TUP, so store the metadata information to schema for TUP team can work on it
                // use the same key with JDBC api
                if (dbcatalog != null)
                    schema.addProp("TABLE_CAT", dbcatalog);

                if (db_schema != null)
                    schema.addProp("TABLE_SCHEM", db_schema);

                if (table != null)
                    schema.addProp("TABLE_NAME", table);

                if (type != null)
                    schema.addProp("TABLE_TYPE", type);

                if (comment != null)
                    schema.addProp("REMARKS", comment);

                tables.add(new ModuleMetadata(dbcatalog, db_schema, table, type, comment, schema));
            }
        }

        return tables;
    }
    */

}
