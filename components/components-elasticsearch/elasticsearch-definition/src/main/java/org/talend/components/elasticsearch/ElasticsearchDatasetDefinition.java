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

package org.talend.components.elasticsearch;

import static org.talend.components.elasticsearch.ElasticsearchComponentFamilyDefinition.MAVEN_GROUP_ID;
import static org.talend.components.elasticsearch.ElasticsearchComponentFamilyDefinition.MAVEN_RUNTIME_ARTIFACT_ID;
import static org.talend.components.elasticsearch.ElasticsearchComponentFamilyDefinition.MAVEN_RUNTIME_URI;

import java.net.MalformedURLException;
import java.net.URL;

import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.common.dataset.DatasetDefinition;
import org.talend.daikon.definition.DefinitionImageType;
import org.talend.daikon.definition.I18nDefinition;
import org.talend.daikon.runtime.RuntimeInfo;

public class ElasticsearchDatasetDefinition extends I18nDefinition implements DatasetDefinition<ElasticsearchDatasetProperties> {

    public static final String RUNTIME_2_4 = "org.talend.components.elasticsearch.runtime_2_4.ElasticsearchDatasetRuntime";

    public static final String NAME = ElasticsearchComponentFamilyDefinition.NAME + "Dataset";

    public ElasticsearchDatasetDefinition() {
        super(NAME);
    }

    @Override
    public Class<ElasticsearchDatasetProperties> getPropertiesClass() {
        return ElasticsearchDatasetProperties.class;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ElasticsearchDatasetProperties properties) {
        try {
            switch (properties.getDatastoreProperties().version.getValue()) {
            case V_2_4:
            default:
                return new JarRuntimeInfo(new URL(MAVEN_RUNTIME_URI),
                        DependenciesReader.computeDependenciesFilePath(MAVEN_GROUP_ID, MAVEN_RUNTIME_ARTIFACT_ID), RUNTIME_2_4);
            }
        } catch (MalformedURLException e) {
            throw new ComponentException(e);
        }
    }

    @Deprecated
    @Override
    public String getImagePath() {
        return NAME + "_icon32.png";
    }

    @Override
    public String getImagePath(DefinitionImageType type) {
        switch (type) {
        case PALETTE_ICON_32X32:
            return NAME + "_icon32.png";
        }
        return null;
    }

    @Override
    public String getIconKey() {
        return "elastic";
    }
}
