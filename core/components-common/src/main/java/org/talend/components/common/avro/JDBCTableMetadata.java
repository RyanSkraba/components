package org.talend.components.common.avro;

import java.sql.DatabaseMetaData;

/**
 * a model which provide all the information to fetch the meta data from a database table
 *
 */
public class JDBCTableMetadata {

    /**
     * database cataglog
     */
    private String catalog;

    /**
     * database schema
     */
    private String dbSchema;

    /**
     * table name
     */
    private String tablename;

    /**
     * JDBC DatabaseMetaData object, the entrance to access the JDBC API
     */
    private DatabaseMetaData DatabaseMetaData;

    public String getCatalog() {
        return catalog;
    }

    public JDBCTableMetadata setCatalog(String catalog) {
        this.catalog = catalog;
        return this;
    }

    public String getDbSchema() {
        return dbSchema;
    }

    public JDBCTableMetadata setDbSchema(String dbSchema) {
        this.dbSchema = dbSchema;
        return this;
    }

    public String getTablename() {
        return tablename;
    }

    public JDBCTableMetadata setTablename(String tablename) {
        this.tablename = tablename;
        return this;
    }

    public DatabaseMetaData getDatabaseMetaData() {
        return DatabaseMetaData;
    }

    public JDBCTableMetadata setDatabaseMetaData(DatabaseMetaData databaseMetaData) {
        DatabaseMetaData = databaseMetaData;
        return this;
    }

}
