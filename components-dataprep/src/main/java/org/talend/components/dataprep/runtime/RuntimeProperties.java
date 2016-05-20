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
package org.talend.components.dataprep.runtime;

import java.io.Serializable;

public class RuntimeProperties implements Serializable {

    private static final long serialVersionUID = 3992422924211437022L;
    private String ulr;
    private String login;
    private String pass;
    private DataPrepOutputModes mode;
    private String dataSetName;
    private String schema;
    private String limit;

    public String getUlr() {
        return ulr;
    }

    public void setUlr(String ulr) {
        this.ulr = ulr;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public DataPrepOutputModes getMode() {
        return mode;
    }

    public String getDataSetName() {
        return dataSetName;
    }

    public void setDataSetName(String dataSetName) {
        this.dataSetName = dataSetName;
    }

    public void setMode(String mode) {
        for (DataPrepOutputModes m: DataPrepOutputModes.values()) {
            if (m.toString().equals(mode)) {
                this.mode = m;
            }
        }
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }
}
