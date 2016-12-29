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
package org.talend.components.kafka.dataset;

import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.SimpleRuntimeInfo;
import org.talend.components.common.dataset.DatasetDefinition;
import org.talend.daikon.definition.I18nDefinition;
import org.talend.daikon.runtime.RuntimeInfo;

public class KafkaDatasetDefinition extends I18nDefinition implements DatasetDefinition<KafkaDatasetProperties> {

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
        return new SimpleRuntimeInfo(this.getClass().getClassLoader(),
                DependenciesReader.computeDependenciesFilePath("org.talend.components", "kafka-runtime"),
                "org.talend.components.kafka.runtime.KafkaDatasetRuntime");
    }

    @Override
    public String getImagePath() {
        return "/org/talend/components/kafka/Kafka.svg";
    }

}
