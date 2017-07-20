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

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.io.elasticsearch.ElasticsearchIO;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.adapter.beam.coders.LazyAvroCoder;
import org.talend.components.adapter.beam.transform.ConvertToIndexedRecord;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.elasticsearch.ElasticsearchDatasetProperties;
import org.talend.components.elasticsearch.input.ElasticsearchInputProperties;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.properties.ValidationResult;

public class ElasticsearchInputRuntime extends PTransform<PBegin, PCollection<IndexedRecord>>
        implements RuntimableRuntime<ElasticsearchInputProperties> {

    private static Logger LOG = LoggerFactory.getLogger(ElasticsearchInputRuntime.class);

    /**
     * The component instance that this runtime is configured for.
     */
    private ElasticsearchInputProperties properties = null;

    private static String[] resolveAddresses(String nodes) {
        String[] addresses = nodes.split(",");
        for (int i = 0; i < addresses.length; i++) {
            addresses[i] = "http://" + addresses[i];
        }
        return addresses;
    }

    protected static ElasticsearchIO.ConnectionConfiguration createConnectionConf(ElasticsearchDatasetProperties dataset) {
        ElasticsearchIO.ConnectionConfiguration connectionConfiguration = null;
        try {
            connectionConfiguration = ElasticsearchIO.ConnectionConfiguration.create(
                    resolveAddresses(dataset.getDatastoreProperties().nodes.getValue()), dataset.index.getValue(),
                    dataset.type.getValue());
        } catch (IOException e) {
            throw TalendRuntimeException.createUnexpectedException(e);
        }
        if (dataset.getDatastoreProperties().auth.useAuth.getValue()) {
            connectionConfiguration = connectionConfiguration
                    .withUsername(dataset.getDatastoreProperties().auth.userId.getValue())
                    .withPassword(dataset.getDatastoreProperties().auth.password.getValue());
        }
        return connectionConfiguration;
    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, ElasticsearchInputProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }

    @Override
    public PCollection<IndexedRecord> expand(PBegin in) {
        ElasticsearchIO.Read esRead = ElasticsearchIO.read()
                .withConnectionConfiguration(createConnectionConf(properties.getDatasetProperties()));
        if (properties.query.getValue() != null) {
            esRead = esRead.withQuery(properties.query.getValue());
        }
        PCollection<String> readFromElasticsearch = in.apply("ReadFromElasticsearch", esRead);
        //TODO(bchen) String data is a json format, convert json to avro is better than string to avro
        return readFromElasticsearch.apply(ConvertToIndexedRecord.<String> of());
    }

    @Override
    public Coder getDefaultOutputCoder() {
        return LazyAvroCoder.of();
    }
}
