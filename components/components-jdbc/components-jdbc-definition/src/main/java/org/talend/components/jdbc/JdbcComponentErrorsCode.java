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

package org.talend.components.jdbc;

import static org.apache.http.HttpStatus.*;

import java.util.Arrays;
import java.util.List;

import org.talend.daikon.exception.error.ErrorCode;

public class JdbcComponentErrorsCode implements ErrorCode {

    public static final JdbcComponentErrorsCode SQL_SYNTAX_ERROR = //
    new JdbcComponentErrorsCode("SQL_SYNTAX_ERROR", SC_BAD_REQUEST);

    public static final JdbcComponentErrorsCode SQL_ERROR = new JdbcComponentErrorsCode("SQL_ERROR", SC_INTERNAL_SERVER_ERROR);

    public static final JdbcComponentErrorsCode DRIVER_NOT_PRESENT_ERROR = //
    new JdbcComponentErrorsCode("DRIVER_NOT_PRESENT_ERROR", SC_INTERNAL_SERVER_ERROR);

    public static final String PRODUCT_TALEND_COMPONENTS = "TCOMP";

    public static final String GROUP_COMPONENT_JDBC = "JDBC";

    public final String code;

    private final int httpStatus;

    private final List<String> expectedContextEntries;

    protected JdbcComponentErrorsCode(String code, int httpStatus, String... contextEntries) {
        this.code = code;
        this.httpStatus = httpStatus;
        expectedContextEntries = Arrays.asList(contextEntries);
    }

    @Override
    public String getProduct() {
        return PRODUCT_TALEND_COMPONENTS;
    }

    @Override
    public String getGroup() {
        return GROUP_COMPONENT_JDBC;
    }

    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    @Override
    public List<String> getExpectedContextEntries() {
        return expectedContextEntries;
    }

    @Override
    public String getCode() {
        return code;
    }
}
