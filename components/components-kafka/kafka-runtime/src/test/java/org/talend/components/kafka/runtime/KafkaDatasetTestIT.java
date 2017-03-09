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
package org.talend.components.kafka.runtime;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.talend.components.kafka.runtime.KafkaTestConstants.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.kafka.dataset.KafkaDatasetProperties;
import org.talend.components.kafka.datastore.KafkaDatastoreProperties;

public class KafkaDatasetTestIT {

    KafkaDatasetProperties datasetProperties;


    @Before
    public void init() throws TimeoutException {
        // there may exists other topics than these build in(configured in pom.xml) topics, but ignore them

        datasetProperties = new KafkaDatasetProperties("inputDatasetProperties");
        datasetProperties.init();
        KafkaDatastoreProperties datastore = new KafkaDatastoreProperties("datastore");
        datasetProperties.setDatastoreProperties(datastore);
        datasetProperties.getDatastoreProperties().brokers.setValue(KafkaTestConstants.BOOTSTRAP_HOST);
    }

    @Test
    public void listTopicForRuntime() throws Exception {
        KafkaDatasetRuntime runtime = new KafkaDatasetRuntime();
        runtime.initialize(null, datasetProperties);
        Set<String> topics = runtime.listTopic();
        assertThat(topics, hasItems(TOPIC_IN, TOPIC_OUT));
    }

    @Test
    public void listTopicForProperties() throws Exception {
        datasetProperties.beforeTopic();
        List<String> possibleTopics = (List<String>) datasetProperties.topic.getPossibleValues();
        assertThat(possibleTopics, hasItems(TOPIC_IN, TOPIC_OUT));
    }

}
