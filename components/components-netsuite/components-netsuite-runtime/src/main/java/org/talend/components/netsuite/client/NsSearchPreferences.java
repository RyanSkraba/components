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

package org.talend.components.netsuite.client;

/**
 *
 */
public class NsSearchPreferences {

    protected Boolean bodyFieldsOnly;
    protected Boolean returnSearchColumns;
    protected Integer pageSize;

    public Boolean getBodyFieldsOnly() {
        return bodyFieldsOnly;
    }

    public void setBodyFieldsOnly(Boolean value) {
        this.bodyFieldsOnly = value;
    }

    public Boolean getReturnSearchColumns() {
        return returnSearchColumns;
    }

    public void setReturnSearchColumns(Boolean value) {
        this.returnSearchColumns = value;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer value) {
        this.pageSize = value;
    }

}
