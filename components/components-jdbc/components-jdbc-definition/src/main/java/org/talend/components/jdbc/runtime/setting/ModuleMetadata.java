package org.talend.components.jdbc.runtime.setting;

import org.apache.avro.Schema;

// TODO a little duplicated with JDBCTableMetadata or make it common not only for JDBC
public class ModuleMetadata {

    public final String catalog;

    public final String dbschema;

    public final String name;

    public final String type;

    public final String comment;

    public final Schema schema;

    public ModuleMetadata(String catalog, String dbschema, String name, String type, String comment, Schema schema) {
        this.catalog = catalog;
        this.dbschema = dbschema;
        this.name = name;
        this.type = type;
        this.comment = comment;
        this.schema = schema;
    }
}
