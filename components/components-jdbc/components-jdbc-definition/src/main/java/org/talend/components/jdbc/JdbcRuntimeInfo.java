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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.api.exception.ComponentException;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.sandbox.SandboxControl;

/**
 * {@link RuntimeInfo} for JDBC components. {@link ClassLoader} for JDBC components should not be reusable, because
 * component support different databases, which requires different set of classes loaded
 */
public class JdbcRuntimeInfo extends JarRuntimeInfo {

    /**
     * Provides Runtime Settings
     */
    private final RuntimeSettingProvider props;

    /**
     * JDBC Driver class name
     */
    private final String driverClassName;

    /**
     * @param runtimeClassName
     * @param props
     */
    public JdbcRuntimeInfo(RuntimeSettingProvider props, String runtimeClassName) {
        // add the version to fix the issue, not good, could we avoid it?
        this(props, JDBCFamilyDefinition.getDIRuntimeMavenURI(), DependenciesReader.computeDependenciesFilePath(
                JDBCFamilyDefinition.getDIRuntimeGroupId(), JDBCFamilyDefinition.getDIRuntimeArtifactId()), runtimeClassName);
    }

    public JdbcRuntimeInfo(RuntimeSettingProvider props, String jarUrlString, String depTxtPath, String runtimeClassName) {
        super(jarUrlString, depTxtPath, runtimeClassName, SandboxControl.CLASSLOADER_REUSABLE);
        if (props == null) {
            throw new NullPointerException("props must not be null");
        }
        this.props = props;
        this.driverClassName = props.getRuntimeSetting().getDriverClass();
    }

    public JdbcRuntimeInfo cloneWithNewJarUrlString(String newJarUrlString) {
        return new JdbcRuntimeInfo(this.props, newJarUrlString, this.getDepTxtPath(), this.getRuntimeClassName());
    }

    @Override
    public List<URL> getMavenUrlDependencies() {
        List<URL> dependencies = new ArrayList<>(super.getMavenUrlDependencies());
        dependencies.addAll(getDriverDependencies());
        return dependencies;
    }

    /**
     * Return list of JDBC driver dependencies
     * 
     * @return list of JDBC driver dependencies
     */
    private List<URL> getDriverDependencies() {
        try {
            List<URL> driverUrls = new ArrayList<>();
            List<String> driverPaths = props.getRuntimeSetting().getDriverPaths();
            if (driverPaths != null) {
                for (String driver : driverPaths) {
                    String mavenPath = removeQuote(driver);
                    // there is some bug in the upriver in studio and the getMavenUrlDependencies is called at some strange time,
                    // so need to filter like below
                    if ("newLine".equals(mavenPath)) {
                        continue;
                    }
                    driverUrls.add(new URL(mavenPath));
                }
            }
            return driverUrls;
        } catch (MalformedURLException e) {
            throw new ComponentException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    // @TODO this should not exists in TCOMP, this is Studio related.
    String removeQuote(String content) {
        if (content.startsWith("\"") && content.endsWith("\"")) {
            return content.substring(1, content.length() - 1);
        }

        return content;
    }

    @Override
    public String toString() {
        return super.toString() + ", JdbcRuntimeInfo: {driverClassName:" + driverClassName + "}";
    }

}
