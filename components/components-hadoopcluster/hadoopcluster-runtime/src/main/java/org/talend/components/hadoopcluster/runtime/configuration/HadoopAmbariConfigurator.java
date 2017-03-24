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
package org.talend.components.hadoopcluster.runtime.configuration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.TrustManager;

import org.apache.ambari.api.AmbariClientBuilder;
import org.apache.ambari.api.model.ApiCluster;
import org.apache.ambari.api.model.ApiClusterList;
import org.apache.ambari.api.v1.ClusterResource;
import org.apache.ambari.api.v1.RootResourceV1;

public class HadoopAmbariConfigurator implements HadoopConfigurator {

    private RootResourceV1 api;

    public HadoopAmbariConfigurator(Builder build) {
        AmbariClientBuilder amBuilder = new AmbariClientBuilder().withBaseURL(build.url).withUsernamePassword(build.user,
                build.password);
        amBuilder.setTrustManagers(build.tms);
        api = amBuilder.build().getRootV1();
    }

    @Override
    public List<String> getAllClusters() {
        ApiClusterList clusters = api.getClustersResource().readClusters();
        List<String> names = new ArrayList<String>();
        for (ApiCluster cluster : clusters.getClusters()) {
            names.add(cluster.getInfo().getName());
        }
        return names;
    }

    @Override
    public HadoopCluster getCluster(String name) {
        ClusterResource cluster = api.getClustersResource().getClusterResource(name);
        return new HadoopAmbariCluster(cluster);
    }

    public static class Builder implements HadoopConfigurator.Builder {

        private URL url;

        private String user;

        private String password;

        private TrustManager[] tms;

        public Builder() {
        }

        @Override
        public Builder withUrl(URL url) {
            this.url = url;
            return this;
        }

        @Override
        public Builder withUsernamePassword(String user, String password) {
            this.user = user;
            this.password = password;
            return this;
        }

        @Override
        public Builder withTrustManagers(TrustManager[] tms) {
            this.tms = tms;
            return this;
        }

        @Override
        public HadoopAmbariConfigurator build() {
            return new HadoopAmbariConfigurator(this);
        }

    }
}
