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
package org.talend.components.kafka.dataset;

import java.net.MalformedURLException;
import java.net.URL;

import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.common.dataset.DatasetDefinition;
import org.talend.components.kafka.KafkaFamilyDefinition;
import org.talend.daikon.definition.DefinitionImageType;
import org.talend.daikon.definition.I18nDefinition;
import org.talend.daikon.runtime.RuntimeInfo;

public class KafkaDatasetDefinition extends I18nDefinition implements DatasetDefinition<KafkaDatasetProperties> {

    public static final String RUNTIME = "org.talend.components.kafka.runtime.KafkaDatasetRuntime";

    public static final String NAME = "KafkaDataset";

    public KafkaDatasetDefinition() {
        super(NAME);
    }

    @Override
    public Class<KafkaDatasetProperties> getPropertiesClass() {
        return KafkaDatasetProperties.class;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(KafkaDatasetProperties properties) {
        try {
            return new JarRuntimeInfo(new URL(KafkaFamilyDefinition.MAVEN_DEFAULT_RUNTIME_URI),
                    DependenciesReader.computeDependenciesFilePath(KafkaFamilyDefinition.MAVEN_GROUP_ID,
                            KafkaFamilyDefinition.MAVEN_DEFAULT_RUNTIME_ARTIFACT_ID), RUNTIME);
        } catch (MalformedURLException e) {
            throw new ComponentException(e);
        }
    }

    @Deprecated
    @Override
    public String getImagePath() {
        return "/org/talend/components/kafka/Kafka.svg";
    }

    @Override
    public String getImagePath(DefinitionImageType type) {
        switch (type) {
        case PALETTE_ICON_32X32:
            return null;
        case SVG_ICON:
            return "/org/talend/components/kafka/Kafka.svg";
        }
        return null;
    }

    @Override
    public String getIconKey() {
        return "kafka";
    }
}
