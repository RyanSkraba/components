package org.talend.components.kafka.runtime;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.kafka.dataset.KafkaDatasetProperties;
import org.talend.daikon.NamedThing;

public class KafkaDatasetTest extends KafkaTestBase {

    KafkaDatasetProperties datasetProperties;

    Set topics = new HashSet();

    @Before
    public void init() throws TimeoutException {
        topics.add("topic1");
        topics.add("topic2");
        topics.add("topic3");
        Iterator topicsIter = topics.iterator();
        while (topicsIter.hasNext()) {
            kafkaUnitRule.getKafkaUnit().createTopic(topicsIter.next().toString());
        }

        datasetProperties = new KafkaDatasetProperties("inputDatasetProperties");
        datasetProperties.init();
        datasetProperties.getDatastoreProperties().brokers.setValue(BROKER_URL);
    }

    @Test
    public void listTopicForRuntime() throws Exception {
        KafkaDatasetRuntime runtime = new KafkaDatasetRuntime();
        runtime.initialize(null, datasetProperties);
        Set<String> topics = runtime.listTopic();
        assertEquals(this.topics, topics);
    }

    @Test
    public void listTopicForProperties() throws Exception {
        datasetProperties.beforeTopic();
        List<NamedThing> possibleTopics = (List<NamedThing>) datasetProperties.topic.getPossibleValues();
        Set<String> topics = new HashSet<>();
        for (NamedThing possibleTopic : possibleTopics) {
            topics.add(possibleTopic.getName());
        }

        assertEquals(this.topics, topics);
    }

}
