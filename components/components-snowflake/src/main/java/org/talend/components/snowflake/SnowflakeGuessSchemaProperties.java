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
package org.talend.components.snowflake;

/**
 * Used to specify property as one that is using query to retrieve data.
 *
 */
public interface SnowflakeGuessSchemaProperties {

    /**
     * Guess schema needs a query for retrieving metadata from database
     *
     * @return query to be executed
     */
    String getQuery();
}
