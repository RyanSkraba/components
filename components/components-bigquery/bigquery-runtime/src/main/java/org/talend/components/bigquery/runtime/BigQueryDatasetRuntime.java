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

package org.talend.components.bigquery.runtime;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.runners.direct.DirectOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.transforms.Sample;
import org.talend.components.adapter.beam.BeamLocalRunnerOption;
import org.talend.components.adapter.beam.coders.LazyAvroCoder;
import org.talend.components.adapter.beam.transform.DirectConsumerCollector;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.bigquery.BigQueryDatasetProperties;
import org.talend.components.bigquery.input.BigQueryInputProperties;
import org.talend.daikon.java8.Consumer;
import org.talend.daikon.properties.ValidationResult;

import com.google.cloud.Page;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.QueryRequest;
import com.google.cloud.bigquery.QueryResponse;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableId;

// import org.apache.beam.runners.direct.DirectRunner;

public class BigQueryDatasetRuntime implements IBigQueryDatasetRuntime {

    /**
     * The dataset instance that this runtime is configured for.
     */
    private BigQueryDatasetProperties properties = null;

    @Override
    public ValidationResult initialize(RuntimeContainer container, BigQueryDatasetProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }

    @Override
    public void getSample(int limit, Consumer<IndexedRecord> consumer) {
        // Create an input runtime based on the properties.
        BigQueryInputRuntime inputRuntime = new BigQueryInputRuntime();
        BigQueryInputProperties inputProperties = new BigQueryInputProperties(null);
        inputProperties.init();
        inputProperties.setDatasetProperties(properties);
        inputRuntime.initialize(null, inputProperties);

        // Create a pipeline using the input component to get records.
        DirectOptions options = BeamLocalRunnerOption.getOptions();
        final Pipeline p = Pipeline.create(options);
        LazyAvroCoder.registerAsFallback(p);

        try (DirectConsumerCollector<IndexedRecord> collector = DirectConsumerCollector.of(consumer)) {
            // Collect a sample of the input records.
            p.apply(inputRuntime) //
                    .apply(Sample.<IndexedRecord> any(limit)).apply(collector);
            PipelineResult pr = p.run();
            pr.waitUntilFinish();
        }
    }

    @Override
    public Set<String> listDatasets() throws IOException {
        BigQuery bigquery = BigQueryConnection.createClient(properties.getDatastoreProperties());
        Page<Dataset> datasets = bigquery.listDatasets(properties.getDatastoreProperties().projectName.getValue(),
                BigQuery.DatasetListOption.pageSize(100));
        Set<String> datasetsName = new HashSet<>();
        Iterator<Dataset> datasetIterator = datasets.iterateAll();
        while (datasetIterator.hasNext()) {
            datasetsName.add(datasetIterator.next().getDatasetId().getDataset());
        }
        return datasetsName;
    }

    @Override
    public Set<String> listTables() throws IOException {
        BigQuery bigquery = BigQueryConnection.createClient(properties.getDatastoreProperties());
        DatasetId datasetId = DatasetId.of(properties.getDatastoreProperties().projectName.getValue(),
                properties.bqDataset.getValue());
        Page<Table> tables = bigquery.listTables(datasetId, BigQuery.TableListOption.pageSize(100));
        Set<String> tablesName = new HashSet<>();
        Iterator<Table> tableIterator = tables.iterateAll();
        while (tableIterator.hasNext()) {
            tablesName.add(tableIterator.next().getTableId().getTable());
        }
        return tablesName;
    }

    /**
     * Get the schema by table name or query.
     * This method also needed for read and write, because we can not get schema from the ongoing data.
     * BigQueryIO.Read return TableRow, which do not include schema in itself.
     * So use BigQuery client to get it before read and write.
     * @return
     */
    @Override
    public Schema getSchema() {
        BigQuery bigquery = BigQueryConnection.createClient(properties.getDatastoreProperties());
        DatasetId datasetId = DatasetId.of(properties.getDatastoreProperties().projectName.getValue(),
                properties.bqDataset.getValue());
        com.google.cloud.bigquery.Schema bqRowSchema = null;
        switch (properties.sourceType.getValue()) {
        case TABLE_NAME: {
            TableId tableId = TableId.of(properties.getDatastoreProperties().projectName.getValue(),
                    properties.bqDataset.getValue(), properties.tableName.getValue());
            Table table = bigquery.getTable(tableId);
            bqRowSchema = table.getDefinition().getSchema();
            break;
        }
        case QUERY: {
            QueryRequest queryRequest = QueryRequest.newBuilder(properties.query.getValue()).setUseLegacySql(properties.useLegacySql.getValue()).build();
            QueryResponse queryResponse = bigquery.query(queryRequest);
            bqRowSchema = queryResponse.getResult().getSchema();
            break;
        }
        default:
            throw new RuntimeException("To be implemented: " + properties.sourceType.getValue());
        }
        return BigQueryAvroRegistry.get().inferSchema(bqRowSchema);
    }
}
