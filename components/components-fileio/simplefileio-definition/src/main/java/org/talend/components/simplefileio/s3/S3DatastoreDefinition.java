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

package org.talend.components.simplefileio.s3;

import java.net.MalformedURLException;
import java.net.URL;

import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.common.datastore.DatastoreDefinition;
import org.talend.components.simplefileio.SimpleFileIOComponentFamilyDefinition;
import org.talend.components.simplefileio.s3.input.S3InputDefinition;
import org.talend.components.simplefileio.s3.output.S3OutputDefinition;
import org.talend.daikon.definition.DefinitionImageType;
import org.talend.daikon.definition.I18nDefinition;
import org.talend.daikon.runtime.RuntimeInfo;

public class S3DatastoreDefinition extends I18nDefinition implements
        DatastoreDefinition<S3DatastoreProperties> {

    public static final String RUNTIME = "org.talend.components.simplefileio.runtime.s3.S3DatastoreRuntime";

    public static final String NAME = "S3Datastore";

    public S3DatastoreDefinition() {
        super(NAME);
    }

    @Override
    public Class<S3DatastoreProperties> getPropertiesClass() {
        return S3DatastoreProperties.class;
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
        return "file-s3-o";
    }

    @Override
    public DatasetProperties createDatasetProperties(S3DatastoreProperties storeProp) {
        S3DatasetProperties setProp = new S3DatasetProperties(S3DatasetDefinition.NAME);
        setProp.init();
        setProp.setDatastoreProperties(storeProp);
        return setProp;
    }

    @Override
    public String getInputCompDefinitionName() {
        return S3InputDefinition.NAME;
    }

    @Override
    public String getOutputCompDefinitionName() {
        return S3OutputDefinition.NAME;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(S3DatastoreProperties properties) {
        try {
            return new JarRuntimeInfo(new URL(SimpleFileIOComponentFamilyDefinition.MAVEN_DEFAULT_RUNTIME_URI),
                    DependenciesReader.computeDependenciesFilePath(SimpleFileIOComponentFamilyDefinition.MAVEN_GROUP_ID,
                            SimpleFileIOComponentFamilyDefinition.MAVEN_DEFAULT_RUNTIME_ARTIFACT_ID), RUNTIME);
        } catch (MalformedURLException e) {
            throw new ComponentException(e);
        }
    }
}
