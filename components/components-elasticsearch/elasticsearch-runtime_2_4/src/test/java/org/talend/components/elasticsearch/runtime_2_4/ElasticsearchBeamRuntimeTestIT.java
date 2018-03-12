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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.PCollection;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.talend.components.adapter.beam.coders.LazyAvroCoder;
import org.talend.components.adapter.beam.transform.ConvertToIndexedRecord;
import org.talend.components.elasticsearch.ElasticsearchDatasetProperties;
import org.talend.components.elasticsearch.ElasticsearchDatastoreProperties;
import org.talend.components.elasticsearch.input.ElasticsearchInputProperties;
import org.talend.components.elasticsearch.output.ElasticsearchOutputProperties;
import org.talend.daikon.avro.converter.JsonGenericRecordConverter;
import org.talend.daikon.avro.inferrer.JsonSchemaInferrer;
import org.talend.daikon.java8.Consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ElasticsearchBeamRuntimeTestIT implements Serializable {

    public static final String INDEX_NAME = "beam";

    @Rule
    public final TestPipeline pipeline = TestPipeline.create();

    ElasticsearchDatastoreProperties datastoreProperties;

    Client client;

    JsonGenericRecordConverter jsonGenericRecordConverter = null;

    @Before
    public void init() throws IOException, ExecutionException, InterruptedException {
        client = ElasticsearchTestUtils.createClient(ElasticsearchTestConstants.HOSTS.split(":")[0],
                ElasticsearchTestConstants.TRANSPORT_PORT, ElasticsearchTestConstants.CLUSTER_NAME);

        datastoreProperties = new ElasticsearchDatastoreProperties("datastore");
        datastoreProperties.init();
        datastoreProperties.nodes.setValue(ElasticsearchTestConstants.HOSTS);
        RestClient restClient = ElasticsearchConnection.createClient(datastoreProperties);

        BasicHeader emptyHeader = new BasicHeader("", "");
        Map<String, String> emptyParams = new HashMap<>();

        ElasticsearchTestUtils.deleteIndex(INDEX_NAME, client);

        Response checkExistsResponse = restClient.performRequest("HEAD", "/" + INDEX_NAME, emptyParams);
        ElasticsearchResponse checkExists = new ElasticsearchResponse(checkExistsResponse);
        if (!checkExists.isOk()) {
            // create index for test, name is 'beam'
            restClient.performRequest("PUT", "/" + INDEX_NAME, emptyHeader);
        }
    }

    @Test
    public void getSampleTest() {
        final String TYPE_NAME = "getsampletest";

        List<String> records = Arrays.asList("r1", "r2", "r3");

        List<IndexedRecord> avroRecords = new ArrayList<>();
        for (String record : records) {
            avroRecords.add(ConvertToIndexedRecord.convertToAvro(record));
        }

        ElasticsearchDatasetProperties datasetProperties = new ElasticsearchDatasetProperties("datasetProperties");
        datasetProperties.init();
        datasetProperties.setDatastoreProperties(datastoreProperties);
        datasetProperties.index.setValue(INDEX_NAME);
        datasetProperties.type.setValue(TYPE_NAME);

        ElasticsearchOutputProperties outputProperties = new ElasticsearchOutputProperties("outputProperties");
        outputProperties.init();
        outputProperties.setDatasetProperties(datasetProperties);

        ElasticsearchOutputRuntime outputRuntime = new ElasticsearchOutputRuntime();
        outputRuntime.initialize(null, outputProperties);

        PCollection<IndexedRecord> inputRecords = (PCollection<IndexedRecord>) pipeline
                .apply(Create.of(avroRecords).withCoder(LazyAvroCoder.of()));
        inputRecords.apply(outputRuntime);

        pipeline.run();

        ElasticsearchTestUtils.upgradeIndexAndGetCurrentNumDocs(INDEX_NAME, TYPE_NAME, client);

        ElasticsearchDatasetRuntime datasetRuntime = new ElasticsearchDatasetRuntime();
        datasetRuntime.initialize(null, datasetProperties);
        final List<IndexedRecord> samples = new ArrayList<>();
        datasetRuntime.getSample(3, new Consumer<IndexedRecord>() {

            @Override
            public void accept(IndexedRecord indexedRecord) {
                samples.add(indexedRecord);
            }
        });

        compareListIndexedRecord(samples, avroRecords);
        assertThat(samples.size(), is(3));
    }

    @Test
    public void getSampleNumericalTest() {
        final String TYPE_NAME = "getsamplenumericaltest";

        List<Integer> records = Arrays.asList(1, 5, 50, 555);

        List<IndexedRecord> avroRecords = new ArrayList<>();
        for (Integer record : records) {
            avroRecords.add(ConvertToIndexedRecord.convertToAvro(record));
        }

        ElasticsearchDatasetProperties datasetProperties = new ElasticsearchDatasetProperties("datasetProperties");
        datasetProperties.init();
        datasetProperties.setDatastoreProperties(datastoreProperties);
        datasetProperties.index.setValue(INDEX_NAME);
        datasetProperties.type.setValue(TYPE_NAME);

        ElasticsearchOutputProperties outputProperties = new ElasticsearchOutputProperties("outputProperties");
        outputProperties.init();
        outputProperties.setDatasetProperties(datasetProperties);

        ElasticsearchOutputRuntime outputRuntime = new ElasticsearchOutputRuntime();
        outputRuntime.initialize(null, outputProperties);

        PCollection<IndexedRecord> inputRecords = (PCollection<IndexedRecord>) pipeline
                .apply(Create.of(avroRecords).withCoder(LazyAvroCoder.of()));
        inputRecords.apply(outputRuntime);

        pipeline.run();

        ElasticsearchTestUtils.upgradeIndexAndGetCurrentNumDocs(INDEX_NAME, TYPE_NAME, client);

        ElasticsearchDatasetRuntime datasetRuntime = new ElasticsearchDatasetRuntime();
        datasetRuntime.initialize(null, datasetProperties);
        final List<IndexedRecord> samples = new ArrayList<>();
        datasetRuntime.getSample(4, new Consumer<IndexedRecord>() {

            @Override
            public void accept(IndexedRecord indexedRecord) {
                samples.add(indexedRecord);
            }
        });

        compareListIndexedRecord(samples, avroRecords);
        assertThat(samples.size(), is(4));
    }

    @Test
    public void basicTest() throws MalformedURLException {
        final String TYPE_NAME = "basictest";

        List<String> records = Arrays.asList("{\"field\":\"r1\"}", "{\"field\":\"r2\"}", "{\"field\":\"r3\"}");

        List<IndexedRecord> avroRecords = new ArrayList<>();
        for (String record : records) {
            if (jsonGenericRecordConverter == null) {
                JsonSchemaInferrer jsonSchemaInferrer = new JsonSchemaInferrer(new ObjectMapper());
                Schema jsonSchema = jsonSchemaInferrer.inferSchema(record);
                jsonGenericRecordConverter = new JsonGenericRecordConverter(jsonSchema);
            }
            GenericRecord outputRecord = jsonGenericRecordConverter.convertToAvro(record);
            avroRecords.add(outputRecord);
        }

        ElasticsearchDatasetProperties datasetProperties = new ElasticsearchDatasetProperties("datasetProperties");
        datasetProperties.init();
        datasetProperties.setDatastoreProperties(datastoreProperties);
        datasetProperties.index.setValue(INDEX_NAME);
        datasetProperties.type.setValue(TYPE_NAME);

        ElasticsearchOutputProperties outputProperties = new ElasticsearchOutputProperties("outputProperties");
        outputProperties.init();
        outputProperties.setDatasetProperties(datasetProperties);

        ElasticsearchOutputRuntime outputRuntime = new ElasticsearchOutputRuntime();
        outputRuntime.initialize(null, outputProperties);

        PCollection<IndexedRecord> inputRecords = (PCollection<IndexedRecord>) pipeline
                .apply(Create.of(avroRecords).withCoder(LazyAvroCoder.of()));
        inputRecords.apply(outputRuntime);

        pipeline.run();

        ElasticsearchTestUtils.upgradeIndexAndGetCurrentNumDocs(INDEX_NAME, TYPE_NAME, client);

        // input pipeline start
        ElasticsearchInputProperties inputProperties = new ElasticsearchInputProperties("inputProperties");
        inputProperties.init();
        inputProperties.setDatasetProperties(datasetProperties);

        ElasticsearchInputRuntime inputRuntime = new ElasticsearchInputRuntime();
        inputRuntime.initialize(null, inputProperties);

        PCollection<IndexedRecord> outputRecords = pipeline.apply(inputRuntime);

        PAssert.that(outputRecords).containsInAnyOrder(avroRecords);
        pipeline.run();

    }

    @Test
    public void filterTest() throws MalformedURLException {
        final String TYPE_NAME = "filtertest";

        List<String> records = Arrays.asList("{\"field\":\"r1\"}", "{\"field\":\"r2\"}", "{\"field\":\"r3\"}",
                "{\"field\":\"q1\"}", "{\"field\":\"q2\"}");
        List<String> expectedRecords = Arrays.asList("{\"field\":\"r1\"}", "{\"field\":\"r2\"}", "{\"field\":\"r3\"}");

        List<IndexedRecord> expectedRecord = new ArrayList<>();
        for (String record : expectedRecords) {
            if (jsonGenericRecordConverter == null) {
                JsonSchemaInferrer jsonSchemaInferrer = new JsonSchemaInferrer(new ObjectMapper());
                Schema jsonSchema = jsonSchemaInferrer.inferSchema(record);
                jsonGenericRecordConverter = new JsonGenericRecordConverter(jsonSchema);
            }
            GenericRecord outputRecord = jsonGenericRecordConverter.convertToAvro(record);
            expectedRecord.add(outputRecord);
        }

        List<IndexedRecord> avroRecords = new ArrayList<>();
        for (String record : records) {
            if (jsonGenericRecordConverter == null) {
                JsonSchemaInferrer jsonSchemaInferrer = new JsonSchemaInferrer(new ObjectMapper());
                Schema jsonSchema = jsonSchemaInferrer.inferSchema(record);
                jsonGenericRecordConverter = new JsonGenericRecordConverter(jsonSchema);
            }
            GenericRecord outputRecord = jsonGenericRecordConverter.convertToAvro(record);
            avroRecords.add(outputRecord);
        }

        ElasticsearchDatasetProperties datasetProperties = new ElasticsearchDatasetProperties("datasetProperties");
        datasetProperties.init();
        datasetProperties.setDatastoreProperties(datastoreProperties);
        datasetProperties.index.setValue(INDEX_NAME);
        datasetProperties.type.setValue(TYPE_NAME);

        ElasticsearchOutputProperties outputProperties = new ElasticsearchOutputProperties("outputProperties");
        outputProperties.init();
        outputProperties.setDatasetProperties(datasetProperties);

        ElasticsearchOutputRuntime outputRuntime = new ElasticsearchOutputRuntime();
        outputRuntime.initialize(null, outputProperties);

        PCollection<IndexedRecord> inputRecords = (PCollection<IndexedRecord>) pipeline
                .apply(Create.of(avroRecords).withCoder(LazyAvroCoder.of()));
        inputRecords.apply(outputRuntime);

        pipeline.run();

        ElasticsearchTestUtils.upgradeIndexAndGetCurrentNumDocs(INDEX_NAME, TYPE_NAME, client);

        // input pipeline start
        ElasticsearchInputProperties inputProperties = new ElasticsearchInputProperties("inputProperties");
        inputProperties.init();
        inputProperties.setDatasetProperties(datasetProperties);
        inputProperties.query.setValue("{\"query\":{\"regexp\":{\"field\":\"r[1-3]\"}}}");

        ElasticsearchInputRuntime inputRuntime = new ElasticsearchInputRuntime();
        inputRuntime.initialize(null, inputProperties);

        PCollection<IndexedRecord> outputRecords = pipeline.apply(inputRuntime);

        PAssert.that(outputRecords).containsInAnyOrder(expectedRecord);
        pipeline.run();

    }

    public static class ExtractJson extends SimpleFunction<IndexedRecord, String> {

        private static final ObjectMapper mapper = new ObjectMapper();

        @Override
        public String apply(IndexedRecord input) {
            try {
                JsonNode jsonNode = mapper.readValue(String.valueOf(input.get(0)), JsonNode.class);
                return jsonNode.path("field").asText();
            } catch (IOException e) {
                return null;
            }
        }
    }

    private void compareListIndexedRecord(List<IndexedRecord> expectedRecord, List<IndexedRecord> outputRecord) {
        List<String> expectedRecordList = new ArrayList<>();
        List<String> outputRecordList = new ArrayList<>();
        for (IndexedRecord value : expectedRecord) {
            expectedRecordList.add(value.toString());
        }
        for (IndexedRecord value : outputRecord) {
            outputRecordList.add(value.toString());
        }
        assertThat(expectedRecordList, containsInAnyOrder(outputRecordList.toArray()));
    }
}
