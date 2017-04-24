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

package org.talend.components.netsuite;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.talend.daikon.exception.error.ErrorCode;

/**
 * NetSuite specific implementation of <code>ErrorCode</code>.
 */
public class NetSuiteErrorCode implements ErrorCode {

    public static final String PRODUCT_TALEND_COMPONENTS = "TCOMP";

    public static final String GROUP_COMPONENT_NETSUITE = "NETSUITE";

    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    public static final String CLIENT_ERROR = "CLIENT_ERROR";

    private String code;
    private int httpStatusCode;
    private final List<String> expectedContextEntries;

    public NetSuiteErrorCode(String code) {
        this(code, 500, Collections.<String>emptyList());
    }

    public NetSuiteErrorCode(String code, String... contextEntries) {
        this(code, 500, Arrays.asList(contextEntries));
    }

    public NetSuiteErrorCode(String code, int httpStatusCode, String... contextEntries) {
        this(code, httpStatusCode, Arrays.asList(contextEntries));
    }

    public NetSuiteErrorCode(String code, int httpStatusCode, List<String> contextEntries) {
        this.code = code;
        this.httpStatusCode = httpStatusCode;
        this.expectedContextEntries = contextEntries;
    }

    @Override
    public String getProduct() {
        return PRODUCT_TALEND_COMPONENTS;
    }

    @Override
    public String getGroup() {
        return GROUP_COMPONENT_NETSUITE;
    }

    @Override
    public int getHttpStatus() {
        return httpStatusCode;
    }

    @Override
    public Collection<String> getExpectedContextEntries() {
        return expectedContextEntries;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        NetSuiteErrorCode that = (NetSuiteErrorCode) o;
        return httpStatusCode == that.httpStatusCode && Objects.equals(code, that.code) && Objects
                .equals(expectedContextEntries, that.expectedContextEntries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, httpStatusCode, expectedContextEntries);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetSuiteErrorCode{");
        sb.append("code='").append(code).append('\'');
        sb.append(", httpStatusCode=").append(httpStatusCode);
        sb.append(", expectedContextEntries=").append(expectedContextEntries);
        sb.append(", product='").append(getProduct()).append('\'');
        sb.append(", group='").append(getGroup()).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
