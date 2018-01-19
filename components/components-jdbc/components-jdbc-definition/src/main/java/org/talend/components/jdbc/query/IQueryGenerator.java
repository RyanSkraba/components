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

import org.talend.components.jdbc.runtime.setting.AllSetting;

public interface IQueryGenerator {

    public static final String EMPTY = "";

    public static final String ENTER = "\n";

    public static final String SPACE = " ";

    public static final String SQL_SPLIT_FIELD = ",";

    public static final String SQL_SELECT = "SELECT";

    public static final String SQL_FROM = "FROM";
    
    public static final String JAVA_TEXT_FENCE = "\"";

    public static final String DEFAULT_TABLE_NAME = "_MyTable_";

    public void setParameters(String databaseDisplayed, String dbschemaDisplayed, String tableDisplayed, AllSetting setting);

    public abstract String generateQuery();
}
