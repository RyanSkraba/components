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

package org.talend.components.hadoopcluster.runtime.configuration.input;

import java.io.IOException;
import java.net.URL;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.net.ssl.TrustManager;

import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.hadoopcluster.configuration.input.HadoopClusterConfiguration;
import org.talend.components.hadoopcluster.configuration.input.HadoopClusterConfigurationInputProperties;
import org.talend.components.hadoopcluster.runtime.configuration.HadoopCluster;
import org.talend.components.hadoopcluster.runtime.configuration.HadoopClusterService;
import org.talend.components.hadoopcluster.runtime.configuration.HadoopConfigurator;
import org.talend.components.hadoopcluster.runtime.configuration.HadoopHostedService;

public abstract class HadoopClusterConfigurationReader extends AbstractBoundedReader<IndexedRecord> {

    HadoopClusterConfigurationInputProperties properties;

    HadoopConfigurator.Builder builder;

    HadoopConfigurator configurator;

    List<HadoopClusterConfiguration> set = new ArrayList<>();

    int pos = 0;

    public HadoopClusterConfigurationReader(BoundedSource source, HadoopClusterConfigurationInputProperties properties) {
        super(source);
        this.properties = properties;
        initConfigurator();
    }

    abstract void initBuilder();

    private void initConfigurator() {
        initBuilder();
        try {
            builder = builder.withUrl(new URL(properties.url.getValue()));
            if (properties.basicAuth.useAuth.getValue()) {
                this.builder = this.builder.withUsernamePassword(properties.basicAuth.userId.getValue(),
                        properties.basicAuth.password.getValue());
            }
            if (properties.ssl.useSsl.getValue()) {
                String filePath = properties.ssl.trustStorePath.getValue();
                String type = properties.ssl.trustStoreType.getValue().toString();
                String pwd = properties.ssl.trustStorePassword.getValue();

                this.builder = this.builder.withTrustManagers(getTrustManagers(filePath, type, pwd));
            }
            configurator = this.builder.build();
        } catch (Exception e) {
            throw new ComponentException(e);
        }
    }

    private TrustManager[] getTrustManagers(String filePath, String type, String pwd) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        TrustManager[] tms = null;
        if (filePath != null && type != null) {
            char[] password = null;
            if (pwd != null) {
                password = pwd.toCharArray();
            }
            java.security.KeyStore trustStore;
            trustStore = java.security.KeyStore.getInstance(type);
            trustStore.load(new java.io.FileInputStream(filePath), password);
            javax.net.ssl.TrustManagerFactory tmf = javax.net.ssl.TrustManagerFactory
                    .getInstance(javax.net.ssl.KeyManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);
            tms = tmf.getTrustManagers();
        }
        return tms;
    }

    @Override
    public boolean start() throws IOException {
        List<String> allClusters = configurator.getAllClusters();
        if (allClusters.size() <= 0) {
            return false;
        }
        for (String clusterName : allClusters) {
            HadoopCluster cluster = configurator.getCluster(clusterName);
            cluster.setBlacklistParams(properties.blackList.keyCol.getValue());
            Map<HadoopHostedService, HadoopClusterService> hostedServices = cluster.getHostedServices();
            for (HadoopHostedService hadoopHostedService : hostedServices.keySet()) {
                String serviceName = hadoopHostedService.name();
                HadoopClusterService hadoopClusterService = hostedServices.get(hadoopHostedService);
                Set<String> confFiles = hadoopClusterService.getConfFiles();
                for (String confFileName : confFiles) {
                    String confFileContent = hadoopClusterService.getConfFileContent(confFileName);
                    set.add(new HadoopClusterConfiguration(clusterName, serviceName, confFileName, confFileContent));
                }
            }
        }
        return set.size() > 0;
    }

    @Override
    public boolean advance() throws IOException {
        return ++pos < set.size();
    }

    @Override
    public IndexedRecord getCurrent() throws NoSuchElementException {
        return set.get(pos);
    }

    @Override
    public Map<String, Object> getReturnValues() {
        return null;
    }
}
