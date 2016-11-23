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
package org.talend.components.api.exception.error;

import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import org.talend.daikon.exception.error.DefaultErrorCode;
import org.talend.daikon.exception.error.ErrorCode;

/**
 * Error codes for the Component service
 */
public enum ComponentsApiErrorCode implements ErrorCode {

                                                         WRONG_COMPONENT_NAME(HttpServletResponse.SC_BAD_REQUEST, "name"), //$NON-NLS-1$
                                                         WRONG_WIZARD_NAME(HttpServletResponse.SC_BAD_REQUEST, "name"), //$NON-NLS-1$
                                                         COMPUTE_DEPENDENCIES_FAILED(HttpServletResponse.SC_NOT_FOUND, "path"),
                                                         WRONG_RETURNS_TYPE_NAME(
                                                                                 HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                                                                 "name"); //$NON-NLS-1$

    private DefaultErrorCode errorCodeDelegate;

    /**
     * default constructor.
     * 
     * @param httpStatus the http status to use.
     */
    ComponentsApiErrorCode(int httpStatus) {
        this.errorCodeDelegate = new DefaultErrorCode(httpStatus);
    }

    /**
     * default constructor.
     *
     * @param httpStatus the http status to use.
     */
    ComponentsApiErrorCode(int httpStatus, String... contextEntries) {
        this.errorCodeDelegate = new DefaultErrorCode(httpStatus, contextEntries);
    }

    /**
     * @return the product. Default value is Talend.
     */
    @Override
    public String getProduct() {
        return "TCOMP"; //$NON-NLS-1$
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
