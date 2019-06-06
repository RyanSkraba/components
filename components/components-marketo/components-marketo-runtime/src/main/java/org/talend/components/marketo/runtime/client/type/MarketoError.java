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
package org.talend.components.marketo.runtime.client.type;

public class MarketoError {

    private String code = "";

    private String message = "";

    private String apiMode = "";

    public MarketoError(String apiMode, String code, String message) {
        this.code = code;
        this.message = message;
        this.apiMode = apiMode;
    }

    public MarketoError(String apiMode, String message) {
        this.message = message;
        this.apiMode = apiMode;
    }

    public MarketoError(String apiMode) {
        this.apiMode = apiMode;
    }

    public String getApiMode() {
        return apiMode;
    }

    public void setApiMode(String apiMode) {
        this.apiMode = apiMode;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("MarketoError{");
        sb.append("api='").append(apiMode).append('\'');
        sb.append(", code='").append(code).append('\'');
        sb.append(", message='").append(message).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
