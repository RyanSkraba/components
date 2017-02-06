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

package org.talend.components.elasticsearch.runtime_2_4;

public class ElasticsearchTestConstants {

    public static final String HOSTS;

    public static final Integer TRANSPORT_PORT;

    public static final String CLUSTER_NAME;

    static {
        String systemPropertyHost = System.getProperty("es.hosts");
        HOSTS = systemPropertyHost != null ? systemPropertyHost : "localhost:9200";
        String systemPropertyTransportPort = System.getProperty("es.transport.port");
        TRANSPORT_PORT = systemPropertyTransportPort != null ? Integer.valueOf(systemPropertyTransportPort) : 9300;
        String systemPropertyClusterName = System.getProperty("es.cluster.name");
        CLUSTER_NAME = systemPropertyClusterName != null ? systemPropertyClusterName : "elasticsearch";
    }
}
