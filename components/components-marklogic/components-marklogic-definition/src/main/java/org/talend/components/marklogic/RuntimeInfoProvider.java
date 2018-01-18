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
package org.talend.components.marklogic;

import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * Service class, which provides {@link RuntimeInfo} instances
 */
public final class RuntimeInfoProvider {

    public static final String MAVEN_GROUP_ID = "org.talend.components";

    public static final String MAVEN_RUNTIME_ARTIFACT_ID = "components-marklogic-runtime";

    public static final String MAVEN_RUNTIME_URI = "mvn:" + MAVEN_GROUP_ID + "/" + MAVEN_RUNTIME_ARTIFACT_ID;

    // Class provides static utility methods and shouldn't be instantiated
    private RuntimeInfoProvider() {}

    public static RuntimeInfo getCommonRuntimeInfo(String clazzFullName) {
        return new JarRuntimeInfo(MAVEN_RUNTIME_URI,
                DependenciesReader.computeDependenciesFilePath(MAVEN_GROUP_ID, MAVEN_RUNTIME_ARTIFACT_ID), clazzFullName);
    }
}
