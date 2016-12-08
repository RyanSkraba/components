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

public class KafkaTestConstants {

    public static final String TOPIC_IN = "test_in";

    public static final String TOPIC_OUT = "test_out";

    public static final String TOPIC_AVRO_IN = "test_avro_in";

    public static final String TOPIC_AVRO_OUT = "test_avro_out";

    public static final String TOPIC_AVRO_CUSTOM_IN = "test_avro_custom_in";

    public static final String TOPIC_AVRO_CUSTOM_OUT = "test_avro_custom_out";

    public static final String BOOTSTRAP_HOST;

    static {
        String systemPropertyHost = System.getProperty("kafka.bootstrap");
        BOOTSTRAP_HOST = systemPropertyHost != null ? systemPropertyHost : "localhost:9092";
    }
}
