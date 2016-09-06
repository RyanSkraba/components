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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.exception.error.ComponentsApiErrorCode;
import org.talend.daikon.exception.ExceptionContext;

/**
 * create a {@link RuntimeInfo} that will look for a given jar and will look for a dependency.txt file inside this jar given the
 * maven groupId and artifactID to find the right path to the file.
 */
public class JarRuntimeInfo implements RuntimeInfo {

    private String runtimeClassName;

    private String mavenGroupId;

    private String mavenArtifactId;

    private URL jarUrl;

    /**
     * uses the <code>mavenGroupId</code> <code>mavenArtifactId</code> to locate the *dependency.txt* file using the rule defined
     * in {@link DependenciesReader#computeDesignDependenciesPath()}
     * 
     * @param classloader classloader used to locate the file thanks to {@link ClassLoader#getResourceAsStream(String)}
     * @param mavenGroupId, used to locate the dependency.txt file
     * @param mavenArtifactId used to locate the dependency.txt file
     * @param runtimeClassName class to be instanciated
     */
    public JarRuntimeInfo(URL jarUrl, String mavenGroupId, String mavenArtifactId, String runtimeClassName) {
        this.jarUrl = jarUrl;
        this.mavenGroupId = mavenGroupId;
        this.mavenArtifactId = mavenArtifactId;
        this.runtimeClassName = runtimeClassName;
    }

    @Override
    public List<URL> getMavenUrlDependencies() {
        DependenciesReader dependenciesReader = new DependenciesReader(mavenGroupId, mavenArtifactId);
        try {
            // we assume that the url is a jar/zip file.
            File file = new File(jarUrl.toURI());
            if (file.exists()) {
                try (ZipFile zip = new ZipFile(file)) {
                    ZipEntry entry = zip.getEntry(dependenciesReader.computeDesignDependenciesPath());
                    if (entry != null) {
                        try (InputStream is = zip.getInputStream(entry)) {
                            Set<String> dependencies = dependenciesReader.parseDependencies(is);
                            // convert the string to URL
                            List<URL> result = new ArrayList<>(dependencies.size());
                            for (String urlString : dependencies) {
                                result.add(new URL(urlString));
                            }
                            return result;
                        }
                    } else {// could not find dependency.txt
                        throw new ComponentException(ComponentsApiErrorCode.COMPUTE_DEPENDENCIES_FAILED, ExceptionContext
                                .withBuilder().put("path", dependenciesReader.computeDesignDependenciesPath()).build());
                    }
                }
            } else {// could not find file
                throw new ComponentException(ComponentsApiErrorCode.COMPUTE_DEPENDENCIES_FAILED,
                        ExceptionContext.withBuilder().put("path", dependenciesReader.computeDesignDependenciesPath()).build());
            }
        } catch (IOException | URISyntaxException e) {
            throw new ComponentException(ComponentsApiErrorCode.COMPUTE_DEPENDENCIES_FAILED, e,
                    ExceptionContext.withBuilder().put("path", dependenciesReader.computeDesignDependenciesPath()).build());
        }

    }

    @Override
    public String getRuntimeClassName() {
        return runtimeClassName;
    }

}
