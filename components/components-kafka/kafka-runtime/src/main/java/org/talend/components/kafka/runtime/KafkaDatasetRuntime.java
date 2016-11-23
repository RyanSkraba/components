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
package org.talend.components.kafka.runtime;

import java.util.Set;

import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.kafka.dataset.KafkaDatasetProperties;
import org.talend.daikon.properties.ValidationResult;

public class KafkaDatasetRuntime implements IKafkaDatasetRuntime {

    private KafkaDatasetProperties dataset;

    @Override
    public ValidationResult initialize(RuntimeContainer container, KafkaDatasetProperties properties) {
        this.dataset = properties;
        return ValidationResult.OK;
    }

    @Override
    public Set<String> listTopic() {
        return KafkaConnection.createConsumer(dataset.getDatastoreProperties()).listTopics().keySet();
    }
}
