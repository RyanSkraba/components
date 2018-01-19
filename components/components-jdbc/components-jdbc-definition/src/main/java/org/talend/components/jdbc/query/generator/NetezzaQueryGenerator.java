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
package org.talend.components.jdbc.query.generator;

import org.talend.components.jdbc.query.EDatabaseTypeName;

public class NetezzaQueryGenerator extends DefaultQueryGenerator {

    public NetezzaQueryGenerator() {
        super(EDatabaseTypeName.NETEZZA);
    }

    /**
     * like "database..table".
     * like "SELECT   talend..\"table\".\"column\",talend..\"table\".newColumn1 FROM talend..\"table\"" .
     */
    protected String getTableNameWithDBAndSchema(final String dbName, final String schema, String tableName) {
        if (tableName == null || EMPTY.equals(tableName.trim())) {
            tableName = DEFAULT_TABLE_NAME;
        }

        final StringBuffer tableNameWithDBAndSchema = new StringBuffer();

        if (dbName != null && !EMPTY.equals(dbName)) {
            // no need text fence for database name
            tableNameWithDBAndSchema.append(checkContextAndAddQuote(dbName, false));
            tableNameWithDBAndSchema.append(getSQLFieldConnector());

            // schema is special, always empty "."
            tableNameWithDBAndSchema.append(getSQLFieldConnector());
        }
        tableNameWithDBAndSchema.append(checkContextAndAddQuote(tableName, true));

        return tableNameWithDBAndSchema.toString();
    }

}
