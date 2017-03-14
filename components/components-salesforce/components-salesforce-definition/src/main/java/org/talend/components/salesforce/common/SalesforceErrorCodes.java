// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
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

import org.talend.daikon.exception.error.DefaultErrorCode;
import org.talend.daikon.exception.error.ErrorCode;

import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

/**
 * Salesforce error code for a backend service application that also implents a REST API.
 */
public enum SalesforceErrorCodes implements ErrorCode {

    UNABLE_TO_RETRIEVE_MODULES(SC_INTERNAL_SERVER_ERROR);

    private DefaultErrorCode errorCodeDelegate;

    /**
     * default constructor.
     *
     * @param httpStatus the http status to use.
     */
    SalesforceErrorCodes(int httpStatus) {
        this.errorCodeDelegate = new DefaultErrorCode(httpStatus);
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
