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

    public static final String BOOTSTRAP_HOST;

    static {
        String systemPropertyHost = System.getProperty("kafka.bootstrap");
        BOOTSTRAP_HOST = systemPropertyHost != null ? systemPropertyHost : "192.168.99.100:9092";
    }
}
