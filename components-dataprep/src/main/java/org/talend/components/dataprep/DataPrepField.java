// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.dataprep;

public class DataPrepField {

    private final String columnName;
    private final String type;
    private final String content;

    DataPrepField (String columnName, String type, String content) {
        this.columnName = columnName;
        this.type = type;
        this.content = content;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }


}
