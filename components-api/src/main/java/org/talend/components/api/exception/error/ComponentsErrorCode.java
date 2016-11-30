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
 * Error codes related to the Component from the component designer perspectve
 */
public enum ComponentsErrorCode implements ErrorCode {
    /** used when a component schema has a wrong type */
    SCHEMA_TYPE_MISMATCH(HttpServletResponse.SC_BAD_REQUEST, "component", "expected", "current") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** used when a component axpect a schema and none is set */
    ,SCHEMA_MISSING(HttpServletResponse.SC_BAD_REQUEST, "component") //$NON-NLS-1$
    /** thrown when a schema is requested and the connector is wrong */
    ,WRONG_CONNECTOR(HttpServletResponse.SC_BAD_REQUEST, "properties") //$NON-NLS-1$
    /** thrown when a IOException is cought */
    ,IO_EXCEPTION(HttpServletResponse.SC_EXPECTATION_FAILED)//
    ;

    private DefaultErrorCode errorCodeDelegate;

    /**
     * default constructor.
     * 
     * @param httpStatus the http status to use.
     */
    ComponentsErrorCode(int httpStatus) {
        this.errorCodeDelegate = new DefaultErrorCode(httpStatus);
    }

    /**
     * default constructor.
     *
     * @param httpStatus the http status to use.
     */
    ComponentsErrorCode(int httpStatus, String... contextEntries) {
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
