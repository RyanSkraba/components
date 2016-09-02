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
package org.talend.components.api.component.runtime;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.exception.error.ComponentsApiErrorCode;
import org.talend.daikon.exception.ExceptionContext;

/**
 * create a {@link RuntimeInfo} implementation for component that do not have to deal with multiple dependencies because of
 * multiple versions or dynamic dependencies.
 */
public class SimpleRuntimeInfo implements RuntimeInfo {

    private String runtimeClassName;

    private ClassLoader classloader;

    private String mavenGroupId;

    private String mavenArtifactId;

    /**
     * uses the <code>mavenGroupId</code> <code>mavenArtifactId</code> to locate the *dependency.txt* file using the rule defined
     * in {@link DependenciesReader#computeDesignDependenciesPath()}
     * 
     * @param classloader classloader used to locate the file thanks to {@link ClassLoader#getResourceAsStream(String)}
     * @param mavenGroupId, used to locate the dependency.txt file
     * @param mavenArtifactId used to locate the dependency.txt file
     * @param runtimeClassName class to be instanciated
     */
    public SimpleRuntimeInfo(ClassLoader classloader, String mavenGroupId, String mavenArtifactId, String runtimeClassName) {
        this.classloader = classloader;
        this.mavenGroupId = mavenGroupId;
        this.mavenArtifactId = mavenArtifactId;
        this.runtimeClassName = runtimeClassName;
    }

    @Override
    public List<URL> getMavenUrlDependencies() {
        DependenciesReader dependenciesReader = new DependenciesReader(mavenGroupId, mavenArtifactId);
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
                    ExceptionContext.withBuilder().put("path", dependenciesReader.computeDesignDependenciesPath()).build());
        }

    }

    @Override
    public String getRuntimeClassName() {
        return runtimeClassName;
    }

}
