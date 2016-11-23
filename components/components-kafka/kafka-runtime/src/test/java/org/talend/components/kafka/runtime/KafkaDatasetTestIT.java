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

import static org.junit.Assert.assertTrue;
import static org.talend.components.kafka.runtime.KafkaTestConstants.TOPIC_IN;
import static org.talend.components.kafka.runtime.KafkaTestConstants.TOPIC_OUT;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.kafka.dataset.KafkaDatasetProperties;
import org.talend.daikon.NamedThing;

public class KafkaDatasetTestIT {

    KafkaDatasetProperties datasetProperties;

    Set expectedTopics = new HashSet();

    @Before
    public void init() throws TimeoutException {
        expectedTopics.add(TOPIC_IN);
        expectedTopics.add(TOPIC_OUT);
        // there may exists other topics than these build in(configured in pom.xml) topics, but ignore them

        datasetProperties = new KafkaDatasetProperties("inputDatasetProperties");
        datasetProperties.init();
        datasetProperties.getDatastoreProperties().brokers.setValue(KafkaTestConstants.BOOTSTRAP_HOST);
    }

    @Test
    public void listTopicForRuntime() throws Exception {
        KafkaDatasetRuntime runtime = new KafkaDatasetRuntime();
        runtime.initialize(null, datasetProperties);
        Set<String> topics = runtime.listTopic();
        for (Object expectedTopic : expectedTopics) {
            assertTrue(topics.contains(expectedTopic));
        }

    }

    @Test
    public void listTopicForProperties() throws Exception {
        datasetProperties.beforeTopic();
        List<NamedThing> possibleTopics = (List<NamedThing>) datasetProperties.topic.getPossibleValues();
        Set<String> topics = new HashSet<>();
        for (NamedThing possibleTopic : possibleTopics) {
            topics.add(possibleTopic.getName());
        }
        for (Object expectedTopic : expectedTopics) {
            assertTrue(topics.contains(expectedTopic));
        }
    }

}
