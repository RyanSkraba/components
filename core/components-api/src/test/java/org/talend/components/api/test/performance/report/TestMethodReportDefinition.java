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
package org.talend.components.api.test.performance.report;

/**
 * Definition of test method including class name and test name
 */
public class TestMethodReportDefinition {

    private final String className;

    private final String methodName;

    private final String fullMethodName;

    public TestMethodReportDefinition(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
        this.fullMethodName = className + "." + methodName;
    }

    /**
     * Get test class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * Get test method name
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Get full method name including class name
     */
    public String getFullMethodName() {
        return fullMethodName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((className == null) ? 0 : className.hashCode());
        result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TestMethodReportDefinition other = (TestMethodReportDefinition) obj;
        if (className == null) {
            if (other.className != null)
                return false;
        } else if (!className.equals(other.className))
            return false;
        if (methodName == null) {
            if (other.methodName != null)
                return false;
        } else if (!methodName.equals(other.methodName))
            return false;
        return true;
    }

}
