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
package org.talend.components.jdbc.query;

import org.talend.components.jdbc.query.generator.AS400QueryGenerator;
import org.talend.components.jdbc.query.generator.DefaultQueryGenerator;
import org.talend.components.jdbc.query.generator.HiveQueryGenerator;
import org.talend.components.jdbc.query.generator.NetezzaQueryGenerator;

public final class GenerateQueryFactory {

    private GenerateQueryFactory() {
    }

    public static IQueryGenerator getGenerator(final String dbType) {
        return getGenerator(EDatabaseTypeName.getTypeFromDbType(dbType));
    }

    public static IQueryGenerator getGenerator(final EDatabaseTypeName dbType) {
        if (dbType == null) {
            return null;
        }
        switch (dbType) {
        case HIVE:
            return new HiveQueryGenerator(dbType);
        case NETEZZA:
            return new NetezzaQueryGenerator();
        case AS400:
            return new AS400QueryGenerator();
        case EXASOL:
        case FIREBIRD:
        case GENERAL_JDBC:
        case GODBC:
        case GREENPLUM:
        case HSQLDB:
        case HSQLDB_IN_PROGRESS:
        case HSQLDB_SERVER:
        case HSQLDB_WEBSERVER:
        case INFORMIX:
        case INTERBASE:
        case JAVADB:
        case JAVADB_DERBYCLIENT:
        case JAVADB_EMBEDED:
        case JAVADB_JCCJDBC:
        case MAXDB:
        case MSODBC:
        case MSSQL:
        case PARACCEL:
        case REDSHIFT:
        case SQLITE:
        case SYBASEASE:
        case SYBASEIQ:
        case TERADATA:
        case VERTICA:
        case ACCESS:
        case IBMDB2:
        case IBMDB2ZOS:
        case MYSQL:
        case AMAZON_AURORA:
        case INGRES:
        case H2:
        case PLUSPSQL:
        case PSQL:
        case ORACLE_OCI:
        case ORACLEFORSID:
        case ORACLESN:
        case SAS:
        case SAPHana:
        case IMPALA:
        default:
            return new DefaultQueryGenerator(dbType);
        }
    }
}
