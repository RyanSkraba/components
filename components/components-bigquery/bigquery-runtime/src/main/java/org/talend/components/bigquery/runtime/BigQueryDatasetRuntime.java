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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.runners.direct.DirectOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.transforms.Sample;
import org.talend.components.adapter.beam.BeamJobRuntimeContainer;
import org.talend.components.adapter.beam.BeamLocalRunnerOption;
import org.talend.components.adapter.beam.transform.DirectConsumerCollector;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.exception.error.ComponentsErrorCode;
import org.talend.components.bigquery.BigQueryDatasetProperties;
import org.talend.components.bigquery.input.BigQueryInputProperties;
import org.talend.daikon.avro.converter.IndexedRecordConverter;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.daikon.java8.Consumer;
import org.talend.daikon.properties.ValidationResult;

import com.google.api.gax.paging.Page;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.cloud.bigquery.FieldValue;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.TableResult;

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
        BigQuery bigquery = BigQueryConnection.createClient(properties.getDatastoreProperties());
        com.google.cloud.bigquery.Schema bqRowSchema = null;
        String query = null;
        boolean useLegacySql = true;
        switch (properties.sourceType.getValue()) {
        case TABLE_NAME: {
            query = String.format("select * from `%s.%s.%s` LIMIT %d",
                    properties.getDatastoreProperties().projectName.getValue(), properties.bqDataset.getValue(),
                    properties.tableName.getValue(), limit);
            useLegacySql = false;
            break;
        }
        case QUERY: {
            query = properties.query.getValue();
            useLegacySql = properties.useLegacySql.getValue();
            break;
        }
        default:
            throw new RuntimeException("To be implemented: " + properties.sourceType.getValue());
        }
        QueryJobConfiguration queryRequest = QueryJobConfiguration
                .newBuilder(query)
                .setUseLegacySql(useLegacySql)
                .build();
        // todo: proper pagination, not critical for getSample yet
        TableResult queryResponse =
                query(bigquery, queryRequest, properties.getDatastoreProperties().projectName.getValue());
        bqRowSchema = queryResponse.getSchema();
        Schema schema = BigQueryAvroRegistry.get().inferSchema(bqRowSchema);
        Iterator<FieldValueList> iterator = queryResponse.getValues().iterator();
        IndexedRecordConverter<Map<String, Object>, IndexedRecord> converter =
                new BigQueryFieldValueListIndexedRecordConverter();
        converter.setSchema(schema);
        int count = 0; // need this only for legacy sql with large result
        while (iterator.hasNext() && count < limit) {
            List<FieldValue> values = iterator.next();
            consumer.accept(converter.convertToAvro(BigQueryAvroRegistry.get().convertFileds(values, schema)));
            count++;
        }
    }

    public void getSampleDeprecated(int limit, Consumer<IndexedRecord> consumer) {
        // Create a pipeline using the input component to get records.
        DirectOptions options = BeamLocalRunnerOption.getOptions();
        final Pipeline p = Pipeline.create(options);

        // Create an input runtime based on the properties.
        BigQueryInputRuntime inputRuntime = new BigQueryInputRuntime();
        BigQueryInputProperties inputProperties = new BigQueryInputProperties(null);
        inputProperties.init();
        inputProperties.setDatasetProperties(properties);
        inputRuntime.initialize(new BeamJobRuntimeContainer(options), inputProperties);

        try (DirectConsumerCollector<IndexedRecord> collector = DirectConsumerCollector.of(consumer)) {
            // Collect a sample of the input records.
            p
                    .apply(inputRuntime) //
                    .apply(Sample.<IndexedRecord> any(limit))
                    .apply(collector);
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
        Iterator<Dataset> datasetIterator = datasets.iterateAll().iterator();
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
        Iterator<Table> tableIterator = tables.iterateAll().iterator();
        while (tableIterator.hasNext()) {
            tablesName.add(tableIterator.next().getTableId().getTable());
        }
        return tablesName;
    }

    /**
     * Get the schema by table name or query. This method also needed for read and write, because we can not get schema
     * from the ongoing data. BigQueryIO.Read return TableRow, which do not include schema in itself. So use BigQuery
     * client to get it before read and write.
     * 
     * @return
     */
    @Override
    public Schema getSchema() {
        BigQuery bigquery = BigQueryConnection.createClient(properties.getDatastoreProperties());
        com.google.cloud.bigquery.Schema bqRowSchema = null;
        switch (properties.sourceType.getValue()) {
        case TABLE_NAME: {
            TableId tableId = TableId.of(properties.getDatastoreProperties().projectName.getValue(),
                    properties.bqDataset.getValue(), properties.tableName.getValue());
            Table table = bigquery.getTable(tableId);
            if (table == null) {
                ComponentException.build(CommonErrorCodes.UNEXPECTED_EXCEPTION).setAndThrow(
                        "Table not found:" + tableId.toString());
            }
            bqRowSchema = table.getDefinition().getSchema();
            break;
        }
        case QUERY: {
            QueryJobConfiguration queryRequest = QueryJobConfiguration
                    .newBuilder(properties.query.getValue())
                    .setUseLegacySql(properties.useLegacySql.getValue())
                    .build();
            TableResult queryResponse =
                    query(bigquery, queryRequest, properties.getDatastoreProperties().projectName.getValue());
            bqRowSchema = queryResponse.getSchema();
            break;
        }
        default:
            throw new RuntimeException("To be implemented: " + properties.sourceType.getValue());
        }
        return BigQueryAvroRegistry.get().inferSchema(bqRowSchema);
    }

    private TableResult query(BigQuery bigquery, QueryJobConfiguration queryRequest, String projectId,
                              BigQuery.JobOption... options) {
        TableResult queryResponse = null;
        try {
            queryResponse = bigquery.query(queryRequest, options);
        } catch (BigQueryException exception) {
            if ("responseTooLarge".equals(exception.getReason())) {
                return queryWithLarge(bigquery, queryRequest, projectId, options);
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return loopQueryResponse(queryResponse);
    }

    private TableResult loopQueryResponse(TableResult queryResponse) {
        if (queryResponse == null) {
            TalendRuntimeException.build(ComponentsErrorCode.IO_EXCEPTION).throwIt();
        }
        /* JobException now
        if (queryResponse.hasErrors()) {
            TalendRuntimeException.build(ComponentsErrorCode.IO_EXCEPTION).setAndThrow(
                    queryResponse.getExecutionErrors().toArray(new String[] {}));
        }
        */
        return queryResponse;
    }

    private TableResult queryWithLarge(BigQuery bigquery, QueryJobConfiguration queryRequest, String projectId,
                                       BigQuery.JobOption... options) {
        String tempDataset = genTempName("dataset");
        String tempTable = genTempName("table");
        bigquery.create(DatasetInfo.of(tempDataset));
        TableId tableId = TableId.of(projectId, tempDataset, tempTable);
        QueryJobConfiguration jobConfiguration = QueryJobConfiguration
                .newBuilder(queryRequest.getQuery())
                .setAllowLargeResults(true)
                .setUseLegacySql(queryRequest.useLegacySql())
                .setDestinationTable(tableId)
                .build();
        try {
            return query(bigquery, jobConfiguration, projectId, options);
        } finally {
            bigquery.delete(tableId);
        }
    }

    private String genTempName(String prefix) {
        return "temp_" + prefix + java.util.UUID.randomUUID().toString().replaceAll("-", "")
                + "<%=cid%>".toLowerCase().replaceAll("[^a-z0-9]", "0").replaceAll("^[^a-z]", "a")
                + Integer.toHexString(java.util.concurrent.ThreadLocalRandom.current().nextInt());
    }
}
