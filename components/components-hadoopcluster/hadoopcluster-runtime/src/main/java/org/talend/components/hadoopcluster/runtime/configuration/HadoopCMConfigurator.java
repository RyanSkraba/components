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

import com.cloudera.api.ClouderaManagerClientBuilder;
import com.cloudera.api.DataView;
import com.cloudera.api.model.ApiCluster;
import com.cloudera.api.model.ApiClusterList;
import com.cloudera.api.v3.RootResourceV3;
import com.cloudera.api.v3.ServicesResourceV3;

public class HadoopCMConfigurator implements HadoopConfigurator {

    RootResourceV3 api;

    public HadoopCMConfigurator(Builder build) {
        ClouderaManagerClientBuilder cmBuilder = new ClouderaManagerClientBuilder().withBaseURL(build.url)
                .withUsernamePassword(build.user, build.password);
        cmBuilder.setTrustManagers(build.tms);
        api = cmBuilder.build().getRootV3();
    }

    @Override
    public List<String> getAllClusters() {
        ApiClusterList clusters = api.getClustersResource().readClusters(DataView.SUMMARY);
        List<String> names = new ArrayList<String>();
        for (ApiCluster cluster : clusters.getClusters()) {
            names.add(cluster.getName());
        }
        return names;
    }

    @Override
    public HadoopCluster getCluster(String name) {
        ServicesResourceV3 cluster = api.getClustersResource().getServicesResource(name);
        return new HadoopCMCluster(cluster);
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
        public HadoopCMConfigurator build() {
            return new HadoopCMConfigurator(this);
        }

    }

}
