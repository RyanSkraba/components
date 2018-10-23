// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.common.tableaction;

import org.apache.avro.Schema;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableActionManager {

    private static TableAction noAction = new NoAction();

    public final static List<String> buildQueries(TableAction.TableActionEnum action, String[] fullTableName,
            Schema schema, TableActionConfig config, Map<String, String> dbTypeMap) throws Exception {
        TableAction tableAction = create(action, fullTableName, schema);
        tableAction.setConfig(config);
        tableAction.setDbTypeMap(dbTypeMap);
        return tableAction.getQueries();
    }

    public final static TableAction create(TableAction.TableActionEnum action, String[] fullTableName, Schema schema) {
        switch (action) {
        case CREATE:
            return new DefaultSQLCreateTableAction(fullTableName, schema, false, false, false);
        case DROP_CREATE:
            return new DefaultSQLCreateTableAction(fullTableName, schema, false, true, false);
        case DROP_IF_EXISTS_AND_CREATE:
            return new DefaultSQLCreateTableAction(fullTableName, schema, false, true, true);
        case CREATE_IF_NOT_EXISTS:
            return new DefaultSQLCreateTableAction(fullTableName, schema, true, false, false);
        case CLEAR:
            return new DefaultSQLClearTableAction(fullTableName);
        case TRUNCATE:
            return new DefaultSQLTruncateTableAction(fullTableName);
        }

        return noAction; // default
    }

    public final static void exec(Connection connection, TableAction.TableActionEnum action, String[] fullTableName,
            Schema schema) throws Exception {
        exec(connection, action, fullTableName, schema, new TableActionConfig());
    }

    public final static void exec(Connection connection, TableAction.TableActionEnum action, String[] fullTableName,
            Schema schema, TableActionConfig config) throws Exception {
        exec(connection, action, fullTableName, schema, config, new HashMap<String, String>());
    }

    public static void exec(Connection connection, TableAction.TableActionEnum action, String[] fullTableName,
            Schema schema, TableActionConfig config, Map<String, String> dbTypeMap) throws Exception {
        for (String q : buildQueries(action, fullTableName, schema, config, dbTypeMap)) {
            connection.createStatement().execute(q);
        }
    }

}
