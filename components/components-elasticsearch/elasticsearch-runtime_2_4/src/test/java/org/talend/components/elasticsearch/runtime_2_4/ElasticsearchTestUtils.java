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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.upgrade.post.UpgradeRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.IndexNotFoundException;

public class ElasticsearchTestUtils {

    /**
     * Create a {@TransportClient} which can help to maintenance elastic search server
     * 
     * @param host
     * @param port
     * @return
     */
    static Client createClient(String host, Integer port, String clusterName) throws UnknownHostException {
        Settings settings = Settings.settingsBuilder().put("cluster.name", clusterName).build();
        return TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
    }

    /**
     * Force an upgrade of all indices to make recently inserted documents available for search.
     *
     * @param client Elasticsearch TCP client to use for upgrade
     * @return The number of docs in the index
     */
    static long upgradeIndexAndGetCurrentNumDocs(String index, String type, Client client) {
        try {
            client.admin().indices().upgrade(new UpgradeRequest(index)).actionGet();
            SearchResponse response = client.prepareSearch(index).setTypes(type).execute().actionGet(5000);
            return response.getHits().getTotalHits();
            // it is fine to ignore bellow exceptions because in testWriteWithBatchSize* sometimes,
            // we call upgrade before any doc have been written
            // (when there are fewer docs processed than batchSize).
            // In that cases index/type has not been created (created upon first doc insertion)
        } catch (IndexNotFoundException e) {
        } catch (java.lang.IllegalArgumentException e) {
            if (!e.getMessage().contains("No search type")) {
                throw e;
            }
        }
        return 0;
    }

    /**
     * Deletes an index and block until deletion is complete.
     *
     * @param index The index to delete
     * @param client The client which points to the Elasticsearch instance
     * @throws InterruptedException if blocking thread is interrupted or index existence check failed
     * @throws java.util.concurrent.ExecutionException if index existence check failed
     * @throws IOException if deletion failed
     */
    static void deleteIndex(String index, Client client)
            throws InterruptedException, java.util.concurrent.ExecutionException, IOException {
        IndicesAdminClient indices = client.admin().indices();
        IndicesExistsResponse indicesExistsResponse =
                indices.exists(new IndicesExistsRequest(index)).get();
        if (indicesExistsResponse.isExists()) {
            indices.prepareClose(index).get();
            // delete index is an asynchronous request, neither refresh or upgrade
            // delete all docs before starting tests. WaitForYellow() and delete directory are too slow,
            // so block thread until it is done (make it synchronous!!!)
            AtomicBoolean indexDeleted = new AtomicBoolean(false);
            AtomicBoolean waitForIndexDeletion = new AtomicBoolean(true);
            indices.delete(
                    Requests.deleteIndexRequest(index),
                    new DeleteActionListener(indexDeleted, waitForIndexDeletion));
            while (waitForIndexDeletion.get()) {
                Thread.sleep(100);
            }
            if (!indexDeleted.get()) {
                throw new IOException("Failed to delete index " + index);
            }
        }
    }

    private static class DeleteActionListener implements ActionListener<DeleteIndexResponse> {

        public DeleteActionListener(AtomicBoolean indexDeleted, AtomicBoolean waitForIndexDeletion) {
            this.indexDeleted = indexDeleted;
            this.waitForIndexDeletion = waitForIndexDeletion;
        }

        private AtomicBoolean indexDeleted;
        private AtomicBoolean waitForIndexDeletion;

        @Override
        public void onResponse(DeleteIndexResponse deleteIndexResponse) {
            waitForIndexDeletion.set(false);
            indexDeleted.set(true);
        }

        @Override
        public void onFailure(Throwable throwable) {
            waitForIndexDeletion.set(false);
            indexDeleted.set(false);
        }
    }
}
