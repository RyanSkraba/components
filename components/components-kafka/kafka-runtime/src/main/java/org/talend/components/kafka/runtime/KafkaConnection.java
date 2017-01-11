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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.talend.components.kafka.KafkaConfTableProperties;
import org.talend.components.kafka.datastore.KafkaDatastoreProperties;
import org.talend.components.kafka.input.KafkaInputProperties;
import org.talend.components.kafka.output.KafkaOutputProperties;

public class KafkaConnection {

    public static KafkaConsumer<byte[], byte[]> createConsumer(KafkaDatastoreProperties datastore) {
        Properties props = createConnProps(datastore);
        return new KafkaConsumer<>(props);
    }

    private static Properties createConnProps(KafkaDatastoreProperties datastore) {
        Properties props = new Properties();
        Map<String, String> consumerMaps = createConnMaps(datastore, false);
        for (String key : consumerMaps.keySet()) {
            props.setProperty(key, consumerMaps.get(key));
        }
        return props;
    }

    public static Map<String, String> createConnMaps(KafkaDatastoreProperties datastore, boolean isBeam) {
        Map<String, String> props = new HashMap<>();
        if (datastore != null) {
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, datastore.brokers.getValue());
            if (!isBeam) {
                props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
                props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
            }
            if (datastore.ssl.useSsl.getValue()) {
                props.put("security.protocol", "SSL");
                props.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, datastore.ssl.trustStoreType.getValue().toString());
                props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, datastore.ssl.trustStorePath.getValue());
                props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, datastore.ssl.trustStorePassword.getValue());
                if (datastore.ssl.needClientAuth.getValue()) {
                    props.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, datastore.ssl.keyStoreType.getValue().toString());
                    props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, datastore.ssl.keyStorePath.getValue());
                    props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, datastore.ssl.keyStorePassword.getValue());
                }
            }
        }
        return props;
    }

    public static Map<String, Object> createInputMaps(KafkaInputProperties input) {
        Map<String, Object> props = new HashMap<>();
        props.putAll(createConnMaps(input.getDatasetProperties().getDatastoreProperties(), true));

        String groupID = input.groupID.getValue();
        if (groupID != null && !"".equals(groupID)) {
            props.put(ConsumerConfig.GROUP_ID_CONFIG, groupID);
        }
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, input.autoOffsetReset.getValue().toString().toLowerCase());

        props.putAll(createConfigurationTable(input.configurations));

        return props;
    }

    public static Map<String, Object> createOutputMaps(KafkaOutputProperties output) {
        Map<String, Object> props = new HashMap<>();
        props.putAll(createConnMaps(output.getDatasetProperties().getDatastoreProperties(), true));

        if (output.useCompress.getValue()) {
            props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, output.compressType.getValue().toString());
        }

        props.putAll(createConfigurationTable(output.configurations));

        return props;
    }

    private static Map<String, Object> createConfigurationTable(KafkaConfTableProperties table) {
        Map<String, Object> props = new HashMap<>();
        List<String> configKeys = table.keyCol.getValue();
        List<String> configValues = table.valueCol.getValue();
        if (configKeys != null && !configKeys.isEmpty() && configValues != null && !configValues.isEmpty()) {
            for (int i = 0; i < configKeys.size(); i++) {
                props.put(configKeys.get(i), configValues.get(i));
            }
        }
        return props;
    }

}
