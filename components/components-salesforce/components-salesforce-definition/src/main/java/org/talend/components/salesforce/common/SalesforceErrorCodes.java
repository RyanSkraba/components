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
package org.talend.components.salesforce.common;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

import java.util.Collection;

import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.error.DefaultErrorCode;
import org.talend.daikon.exception.error.ErrorCode;

/**
 * Salesforce error code for a back-end service application that also implements a REST API.
 */
public enum SalesforceErrorCodes implements ErrorCode {

    UNABLE_TO_RETRIEVE_MODULES(SC_INTERNAL_SERVER_ERROR),
    UNABLE_TO_RETRIEVE_MODULE_FIELDS(SC_INTERNAL_SERVER_ERROR),

    ERROR_IN_BULK_QUERY_PROCESSING(SC_BAD_REQUEST, ExceptionContext.KEY_MESSAGE),

    INVALID_SOQL(SC_BAD_REQUEST, ExceptionContext.KEY_MESSAGE);

    private DefaultErrorCode errorCodeDelegate;

    /**
     * default constructor.
     *
     * @param httpStatus the http status to use.
     */
    SalesforceErrorCodes(int httpStatus) {
        this.errorCodeDelegate = new DefaultErrorCode(httpStatus);
    }

    SalesforceErrorCodes(int httpStatus, String contextEntry) {
        this.errorCodeDelegate = new DefaultErrorCode(httpStatus, contextEntry);
    }

    /**
     * @return the product. Default value is Talend.
     */
    @Override
    public String getProduct() {
        return errorCodeDelegate.getProduct();
    }

    /**
     * @return the group. Default Value is ALL
     */
    @Override
    public String getGroup() {
        return errorCodeDelegate.getGroup();
    }

    /**
     * @return the http status.
     */
    @Override
    public int getHttpStatus() {
        return errorCodeDelegate.getHttpStatus();
    }

    /**
     * @return the expected context entries.
     */
    @Override
    public Collection<String> getExpectedContextEntries() {
        return errorCodeDelegate.getExpectedContextEntries();
    }

    @Override
    public String getCode() {
        return toString();
    }
}
