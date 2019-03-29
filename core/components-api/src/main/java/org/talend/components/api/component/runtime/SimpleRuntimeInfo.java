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
package org.talend.components.api.component.runtime;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.exception.error.ComponentsApiErrorCode;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * create a {@link RuntimeInfo} implementation for component that do not have to deal with multiple dependencies because
 * of multiple versions or dynamic dependencies.
 */
public class SimpleRuntimeInfo implements RuntimeInfo {
    static {
        if (System.getProperty("sun.boot.class.path") == null) { // j11 workaround due to daikon
            System.setProperty("sun.boot.class.path", System.getProperty("java.class.path"));
        }
    }

    private String runtimeClassName;

    private ClassLoader classloader;

    private String depTxtPath;

    /**
     * Explicitly uses the specified dependency path as discovered by {@link ClassLoader#getResourceAsStream(String)}.
     * 
     * @param classloader classloader used to locate the file resource.
     * @param depTxtPath, path used to locate the dependency.txt file
     * @param runtimeClassName class to be instantiated
     */
    public SimpleRuntimeInfo(ClassLoader classloader, String depTxtPath, String runtimeClassName) {
        this.classloader = classloader;
        this.depTxtPath = depTxtPath;
        this.runtimeClassName = runtimeClassName;
    }

    @Override
    public List<URL> getMavenUrlDependencies() {
        DependenciesReader dependenciesReader = new DependenciesReader(depTxtPath);
        try {
            Set<String> dependencies = dependenciesReader.getDependencies(classloader);
            // convert the string to URL
            List<URL> result = new ArrayList<>(dependencies.size());
            for (String urlString : dependencies) {
                result.add(new URL(urlString));
            }
            return result;
        } catch (IOException e) {
            throw new ComponentException(ComponentsApiErrorCode.COMPUTE_DEPENDENCIES_FAILED, e,
                    ExceptionContext.withBuilder().put("path", dependenciesReader.getDependencyFilePath()).build());
        }

    }

    @Override
    public String getRuntimeClassName() {
        return runtimeClassName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!SimpleRuntimeInfo.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        SimpleRuntimeInfo other = (SimpleRuntimeInfo) obj;
        return this.runtimeClassName.equals(other.runtimeClassName) && this.depTxtPath.equals(other.depTxtPath);
    }

    @Override
    public int hashCode() {
        return (runtimeClassName + depTxtPath).hashCode();
    }

    @Override
    public String toString() {
        return "SimpleRuntimeInfo: {" + "runtimeClassName:" + runtimeClassName + ", " + "depTxtPath: " + depTxtPath + "}";
    }
}
