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
package org.talend.components.jdbc;

public interface ComponentConstants {

    /**
     * key for storing and getting the connection between JDBC components
     */
    String CONNECTION_KEY = "conn";

    String URL_KEY = "url";

    String USERNAME_KEY = "username";

    String RETURN_QUERY = "query";

    String RETURN_INSERT_RECORD_COUNT = "nbLineInserted";

    String RETURN_UPDATE_RECORD_COUNT = "nbLineUpdated";

    String RETURN_DELETE_RECORD_COUNT = "nbLineDeleted";

    String RETURN_REJECT_RECORD_COUNT = "nbLineRejected";

    // TOOD use a common one
    String TALEND6_DYNAMIC_COLUMN_POSITION = "di.dynamic.column.position";

    String TALEND6_DYNAMIC_COLUMN_NAME = "di.dynamic.column.name";

    String ADD_QUOTES = "ADD_QUOTES";

    String GLOBAL_CONNECTION_POOL_KEY = "GLOBAL_CONNECTION_POOL";

    String KEY_DB_DATASOURCES_RAW = "KEY_DB_DATASOURCES_RAW";

    String MAPPING_LOCATION = "MAPPING_LOCATION";
    
    String MAPPING_URL_SUBFIX = "MAPPINGS_URL";
}
