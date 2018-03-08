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

import org.talend.components.kafka.dataset.KafkaDatasetProperties;
import org.talend.components.kafka.datastore.KafkaDatastoreProperties;

public class KafkaTestConstants {

    public static final String TOPIC_IN = "test_in";

    public static final String TOPIC_OUT = "test_out";

    public static final String TOPIC_AVRO_IN = "test_avro_in";

    public static final String TOPIC_AVRO_OUT = "test_avro_out";

    public static final String BOOTSTRAP_HOST;

    static {
        String systemPropertyHost = System.getProperty("kafka.bootstrap");
        BOOTSTRAP_HOST = systemPropertyHost != null ? systemPropertyHost : "localhost:9092";
    }

    public static KafkaDatastoreProperties createDatastore() {
        KafkaDatastoreProperties datastore = new KafkaDatastoreProperties("datastore");
        datastore.init();
        datastore.brokers.setValue(KafkaTestConstants.BOOTSTRAP_HOST);
        return datastore;
    }

    public static KafkaDatasetProperties createDataset(KafkaDatastoreProperties datastore) {
        KafkaDatasetProperties dataset = new KafkaDatasetProperties("dataset");
        dataset.init();
        dataset.setDatastoreProperties(datastore);
        return dataset;
    }

    public static KafkaDatasetProperties createDatasetCSV(KafkaDatastoreProperties datastore, String topic,
            KafkaDatasetProperties.FieldDelimiterType fieldDelimiter, String specificFieldDelimiter) {
        KafkaDatasetProperties dataset = createDataset(datastore);
        dataset.topic.setValue(topic);
        dataset.valueFormat.setValue(KafkaDatasetProperties.ValueFormat.CSV);
        dataset.fieldDelimiter.setValue(fieldDelimiter);
        dataset.specificFieldDelimiter.setValue(specificFieldDelimiter);
        return dataset;
    }
}
