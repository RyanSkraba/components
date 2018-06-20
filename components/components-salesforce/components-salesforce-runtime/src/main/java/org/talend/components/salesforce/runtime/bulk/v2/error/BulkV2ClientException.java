
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

package org.talend.components.salesforce.runtime.bulk.v2.error;

import java.io.IOException;

import com.sforce.ws.util.Verbose;

public class BulkV2ClientException extends IOException {

    private String exceptionMessage;

    public BulkV2ClientException() {
    }

    public BulkV2ClientException(String message, Throwable th) {
        super(message, th);
        this.exceptionMessage = message;
    }

    public BulkV2ClientException(String message) {
        super(message);
        this.exceptionMessage = message;
    }

    @Override
    public String getMessage() {
        return exceptionMessage;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[BulkV2ClientException ");
        sb.append(" exceptionMessage=");
        sb.append("'").append(Verbose.toString(exceptionMessage)).append("'\n");
        sb.append("]\n");
        return sb.toString();
    }
}
