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

import org.apache.commons.lang3.StringUtils;

public class MarketoException extends Exception {

    private String apiMode = "";

    private String code = "";

    public MarketoException(String apiMode, String message) {
        super(message);
        this.apiMode = apiMode;
    }

    public MarketoException(String apiMode, String code, String message) {
        super(message);
        this.apiMode = apiMode;
        this.code = code;
    }

    public MarketoException(String apiMode, int code, String message) {
        super(message);
        this.apiMode = apiMode;
        this.code = new Integer(code).toString();
    }

    public MarketoException(String apiMode, String code, String message, Throwable cause) {
        super(message, cause);
        this.apiMode = apiMode;
        this.code = code;
    }

    public MarketoException(String apiMode, int code, String message, Throwable cause) {
        super(message, cause);
        this.apiMode = apiMode;
        this.code = new Integer(code).toString();
    }

    public MarketoException(String apiMode, String message, Throwable cause) {
        super(message, cause);
        this.apiMode = apiMode;
    }

    public MarketoException(String apiMode, Throwable cause) {
        super(cause);
        this.apiMode = apiMode;
    }

    public String getApiMode() {
        return apiMode;
    }

    public void setApiMode(String apiMode) {
        this.apiMode = apiMode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public MarketoError toMarketoError() {
        return new MarketoError(apiMode, code, getMessage());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("MarketoException{");
        sb.append("api='").append(apiMode).append('\'');
        if (!StringUtils.isEmpty(code)) {
            sb.append(", code='").append(code).append('\'');
        }
        sb.append(", message='").append(getMessage()).append('\'');
        if (getCause() != null) {
            sb.append(", cause='").append(getCause()).append('\'');
        }
        sb.append('}');
        return sb.toString();
    }
}
