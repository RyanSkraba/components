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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

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
     * @param jarUrlString url of the jar to read the depenency.txt from
     * @param depTxtPath, path used to locate the dependency.txt file
     * @param runtimeClassName class to be instanciated
     * @throws {@link TalendRuntimeException} if the jarUrlString is malformed
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

    public URL getJarUrl() {
        return jarUrl;
    }

    public String getDepTxtPath() {
        return depTxtPath;
    }

    @Override
    public List<URL> getMavenUrlDependencies() {
        return DependenciesReader.extractDepenencies(jarUrl, depTxtPath);
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
        if (!JarRuntimeInfo.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        JarRuntimeInfo other = (JarRuntimeInfo) obj;
        return this.runtimeClassName.equals(other.runtimeClassName) && this.jarUrl.equals(other.jarUrl)
                && this.depTxtPath.equals(other.depTxtPath);
    }

    @Override
    public int hashCode() {
        return (runtimeClassName + jarUrl + depTxtPath).hashCode();
    }

    @Override
    public String toString() {
        return "JarRunTimeInfo: {" + "runtimeClassName:" + runtimeClassName + ", " + "jarUrl: " + jarUrl + ", " + "depTxtPath: "
                + depTxtPath + "}";
    }

}
