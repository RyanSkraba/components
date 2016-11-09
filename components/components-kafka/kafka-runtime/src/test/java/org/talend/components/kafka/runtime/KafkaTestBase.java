package org.talend.components.kafka.runtime;

import org.junit.Rule;

import info.batey.kafka.unit.KafkaUnitRule;

public class KafkaTestBase {

    public static String BROKER_URL = "localhost:5001";

    @Rule
    public KafkaUnitRule kafkaUnitRule = new KafkaUnitRule(5000, 5001);
}
