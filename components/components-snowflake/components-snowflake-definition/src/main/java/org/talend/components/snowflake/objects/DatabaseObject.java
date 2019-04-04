// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.snowflake.objects;

/**
 * Snowflake database object base class. Each database object belongs to some database and schema,
 * which together comprise namespace. Examples of database objects are table, stage, view.
 */
public abstract class DatabaseObject {

    /**
     * Double quotes character which is used as identifier (database, schema, table name) enclosure character
     */
    static final String IDENTIFIER_ENCLOSURE = "\"";

    /**
     * Dot character which is used as separator between identifiers in Snowflake fully-qualifying names
     */
    static final String IDENTIFIER_SEPARATOR = ".";

    private final String database;

    private final String schema;

    public DatabaseObject(final String database, final String schema) {
        this.database = database;
        this.schema = schema;
    }

    public String getDatabase() {
        return database;
    }

    public String getSchema() {
        return schema;
    }

    /**
     * encloses value with double quotes, if it wasn't enclosed
     *
     * @param value value to be enclosed
     * @return enclosed value
     */
    String enclose(final String value) {
        if (isEnclosed(value)) {
            return value;
        }
        return IDENTIFIER_ENCLOSURE + value + IDENTIFIER_ENCLOSURE;
    }

    /**
     * checks whether value is enclosed with double quotes characters
     *
     * @param value value to be checked
     * @return true if value is enclosed
     */
    boolean isEnclosed(final String value){
        return value.startsWith(IDENTIFIER_ENCLOSURE) && value.endsWith(IDENTIFIER_ENCLOSURE);
    }

}
