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
import java.util.Arrays;
import java.util.HashMap;

import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.datastore.runtime.DatastoreRuntime;
import org.talend.components.elasticsearch.ElasticsearchDatastoreProperties;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.properties.ValidationResult;

import com.google.gson.JsonObject;

public class ElasticsearchDatastoreRuntime implements DatastoreRuntime<ElasticsearchDatastoreProperties> {

    /**
     * The datastore instance that this runtime is configured for.
     */
    private ElasticsearchDatastoreProperties properties = null;

    @Override
    public ValidationResult initialize(RuntimeContainer container, ElasticsearchDatastoreProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }

    @Override
    public Iterable<ValidationResult> doHealthChecks(RuntimeContainer container) {
        ValidationResult validationResult;
        try (RestClient client = ElasticsearchConnection.createClient(properties)) {
            Response response = client.performRequest("GET", "/_cluster/health", new HashMap<String, String>(),
                    new BasicHeader("", ""));
            ElasticsearchResponse esResponse = new ElasticsearchResponse(response);
            if (esResponse.isOk()) {
                JsonObject entity = esResponse.getEntity();
                String status = entity.getAsJsonPrimitive("status").getAsString();
                if (status != "red") {
                    validationResult = ValidationResult.OK;
                } else {
                    validationResult = new ValidationResult(TalendRuntimeException.createUnexpectedException(
                            String.format("Cluster %s status is red", entity.getAsJsonPrimitive("cluster_name").getAsString())));
                }
            } else {
                validationResult = new ValidationResult(
                        TalendRuntimeException.createUnexpectedException(esResponse.getStatusLine().toString()));
            }
        } catch (IOException e) {
            validationResult = new ValidationResult(TalendRuntimeException.createUnexpectedException(e.getMessage()));
        }
        return Arrays.asList(validationResult);
    }

}
