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

package org.talend.components.simplefileio.local;

import java.net.MalformedURLException;
import java.net.URL;

import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.common.dataset.DatasetDefinition;
import org.talend.components.simplefileio.SimpleFileIOComponentFamilyDefinition;
import org.talend.daikon.definition.DefinitionImageType;
import org.talend.daikon.definition.I18nDefinition;
import org.talend.daikon.runtime.RuntimeInfo;

public class LocalFileIODatasetDefinition extends I18nDefinition implements DatasetDefinition<LocalFileIODatasetProperties> {

    //TODO create the runtime class
    public static final String RUNTIME = "org.talend.components.simplefileio.runtime.XXX";

    public static final String NAME = "LocalFileDataset";

    public LocalFileIODatasetDefinition() {
        super(NAME);
    }

    @Override
    public Class<LocalFileIODatasetProperties> getPropertiesClass() {
        return LocalFileIODatasetProperties.class;
    }

    @Deprecated
    @Override
    public String getImagePath() {
        return null;
    }

    @Override
    public String getImagePath(DefinitionImageType type) {
        return null;
    }

    @Override
    public String getIconKey() {
        return "file-local-o";
    }

    @Override
    public RuntimeInfo getRuntimeInfo(LocalFileIODatasetProperties properties) {
        try {
            return new JarRuntimeInfo(new URL(SimpleFileIOComponentFamilyDefinition.MAVEN_DEFAULT_RUNTIME_URI),
                    DependenciesReader.computeDependenciesFilePath(SimpleFileIOComponentFamilyDefinition.MAVEN_GROUP_ID,
                            SimpleFileIOComponentFamilyDefinition.MAVEN_DEFAULT_RUNTIME_ARTIFACT_ID),
                    RUNTIME, Constant.IS_CLASSLOADER_REUSABLE);
        } catch (MalformedURLException e) {
            throw new ComponentException(e);
        }
    }
}
