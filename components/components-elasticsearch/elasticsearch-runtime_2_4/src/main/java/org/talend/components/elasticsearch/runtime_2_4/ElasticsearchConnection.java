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

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.talend.components.elasticsearch.ElasticsearchDatastoreProperties;

public class ElasticsearchConnection {

    public static RestClient createClient(ElasticsearchDatastoreProperties datastore) throws MalformedURLException {
        String urlStr = datastore.nodes.getValue();
        String[] urls = urlStr.split(",");
        HttpHost[] hosts = new HttpHost[urls.length];
        int i = 0;
        for (String address : urls) {
            URL url = new URL("http://" + address);
            hosts[i] = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
            i++;
        }
        RestClientBuilder restClientBuilder = RestClient.builder(hosts);
        if (datastore.auth.useAuth.getValue()) {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(datastore.auth.userId.getValue(), datastore.auth.password.getValue()));
            restClientBuilder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {

                public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
                    return httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                }
            });
        }
        return restClientBuilder.build();
    }
}
