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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.exception.error.ComponentsApiErrorCode;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;

/**
 * create a {@link RuntimeInfo} that will look for a given jar and will look for a dependency.txt file inside this jar given the
 * maven groupId and artifactID to find the right path to the file.
 */
public class JarRuntimeInfo implements RuntimeInfo {

    private String runtimeClassName;

    private URL jarUrl;

    private String depTxtPath;

    static {
        RuntimeUtil.registerMavenUrlHandler();
    }

    /**
     * uses the <code>mavenGroupId</code> <code>mavenArtifactId</code> to locate the *dependency.txt* file using the rule defined
     * in {@link DependenciesReader#computeDependenciesFilePath}
     * 
     * @param jarUrl url of the jar to read the depenency.txt from
     * @param depTxtPath, path used to locate the dependency.txt file
     * @param runtimeClassName class to be instanciated
     */
    public JarRuntimeInfo(URL jarUrl, String depTxtPath, String runtimeClassName) {
        this.jarUrl = jarUrl;
        this.depTxtPath = depTxtPath;
        this.runtimeClassName = runtimeClassName;
    }

    /**
     * uses the <code>mavenGroupId</code> <code>mavenArtifactId</code> to locate the *dependency.txt* file using the rule defined
     * in {@link DependenciesReader#computeDependenciesFilePath}
     * 
     * @param jarUrl url of the jar to read the depenency.txt from
     * @param depTxtPath, path used to locate the dependency.txt file
     * @param runtimeClassName class to be instanciated
     * @throws a {@link TalendRuntimeException} if the jarUrlString is malformed
     */
    public JarRuntimeInfo(String jarUrlString, String depTxtPath, String runtimeClassName) {
        try {
            this.jarUrl = new URL(jarUrlString);
            this.depTxtPath = depTxtPath;
            this.runtimeClassName = runtimeClassName;
        } catch (MalformedURLException e) {
            throw TalendRuntimeException.createUnexpectedException(e);
        }
    }

    @Override
    public List<URL> getMavenUrlDependencies() {
        DependenciesReader dependenciesReader = new DependenciesReader(depTxtPath);
        try {
            // we assume that the url is a jar/zip file.
            try (JarInputStream jarInputStream = new JarInputStream(jarUrl.openStream())) {
                return extractDependencyFromStream(dependenciesReader, depTxtPath, jarInputStream);
            }
        } catch (IOException e) {
            throw new ComponentException(ComponentsApiErrorCode.COMPUTE_DEPENDENCIES_FAILED, e,
                    ExceptionContext.withBuilder().put("path", depTxtPath).build());
        }
    }

    protected static List<URL> extractDependencyFromStream(DependenciesReader dependenciesReader, String depTxtPath,
            JarInputStream jarInputStream) throws IOException, MalformedURLException {
        JarEntry nextJarEntry = jarInputStream.getNextJarEntry();
        while (nextJarEntry != null) {
            if (depTxtPath.equals(nextJarEntry.getName())) {// we got it so parse it.
                Set<String> dependencies = dependenciesReader.parseDependencies(jarInputStream);
                // convert the string to URL
                List<URL> result = new ArrayList<>(dependencies.size());
                for (String urlString : dependencies) {
                    result.add(new URL(urlString));
                }
                return result;
            }
            nextJarEntry = jarInputStream.getNextJarEntry();
        }
        throw new ComponentException(ComponentsApiErrorCode.COMPUTE_DEPENDENCIES_FAILED,
                ExceptionContext.withBuilder().put("path", depTxtPath).build());
    }

    @Override
    public String getRuntimeClassName() {
        return runtimeClassName;
    }

}
