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
 * Snowflake database table object.
 * There are 3 types of tables in Snowflake:
 * 1. Permanent
 * 2. Transient
 * 3. Temporary
 * This TableObject may be used to store table namespace(database and schema) and name for all these 3 types
 */
public class TableObject extends DatabaseObject {

    private final String table;

    /**
     * Table full name cached value to avoid multiple computation
     */
    private String fullName;

    public TableObject(final String database, final String schema, final String table) {
        super(database, schema);
        this.table = table;
    }

    public String getTable() {
        return table;
    }

    /**
     * Builds fully-qualifying table name in the form:
     * [database_name].[schema_name].[table_name]
     *
     * @param enclose specifies whether identifiers should be enclosed
     * @return fully-qualifying table name
     */
    public String getFullName(final boolean enclose) {
        if (fullName != null) {
            return fullName;
        }
        StringBuilder sb = new StringBuilder();
        if (getDatabase() != null) {
            if (enclose) {
                sb.append(enclose(getDatabase()));
            } else {
                sb.append(getDatabase());
            }
            sb.append(IDENTIFIER_SEPARATOR);
        }

        if (getSchema() != null) {
            if (enclose) {
                sb.append(enclose(getSchema()));
            } else {
                sb.append(getSchema());
            }
            sb.append(IDENTIFIER_SEPARATOR);
        }

        if (getTable() != null) {
            if (enclose) {
                sb.append(enclose(getTable()));
            } else {
                sb.append(getTable());
            }
        }
        fullName = sb.toString();
        return fullName;
    }

    /**
     * Builds fully-qualifying table name in the form:
     * [database_name].[schema_name].[table_name]
     * where each identifier is enclosed with double quotes
     *
     * @return fully-qualifying table name
     */
    public String getFullName() {
        return getFullName(true);
    }

}
