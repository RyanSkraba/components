package org.talend.components.kafka.runtime;

public class KafkaTestConstants {

    public static final String TOPIC_IN = "test_in";

    public static final String TOPIC_OUT = "test_out";

    public static final String BOOTSTRAP_HOST;

    static {
        String systemPropertyHost = System.getProperty("kafka.bootstrap");
        BOOTSTRAP_HOST = systemPropertyHost != null ? systemPropertyHost : "192.168.99.100:9092";
    }
}
