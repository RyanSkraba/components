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

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.talend.components.adapter.beam.BeamJobRuntimeContainer;
import org.talend.components.adapter.beam.gcp.GcpServiceAccountOptions;
import org.talend.components.adapter.beam.gcp.ServiceAccountCredentialFactory;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.bigquery.BigQueryDatasetProperties;
import org.talend.components.bigquery.BigQueryDatastoreProperties;
import org.talend.components.bigquery.input.BigQueryInputProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.converter.IndexedRecordConverter;
import org.talend.daikon.properties.ValidationResult;

import com.google.api.services.bigquery.model.TableReference;
import com.google.api.services.bigquery.model.TableRow;

public class BigQueryInputRuntime extends PTransform<PBegin, PCollection<IndexedRecord>>
        implements RuntimableRuntime<BigQueryInputProperties> {

    /**
     * The component instance that this runtime is configured for.
     */
    private BigQueryInputProperties properties = null;

    private BigQueryDatasetProperties dataset = null;

    private BigQueryDatastoreProperties datastore = null;

    private AvroCoder defaultOutputCoder;

    @Override
    public ValidationResult initialize(RuntimeContainer container, BigQueryInputProperties properties) {
        this.properties = properties;
        this.dataset = properties.getDatasetProperties();
        this.datastore = dataset.getDatastoreProperties();

        // Data returned by BigQueryIO do not contains self schema, so have to retrieve it before read and write
        // operations
        Schema schema = properties.getDatasetProperties().main.schema.getValue();
        if (schema == null || AvroUtils.isSchemaEmpty(schema) || AvroUtils.isIncludeAllFields(schema)) {
            BigQueryDatasetRuntime schemaFetcher = new BigQueryDatasetRuntime();
            schemaFetcher.initialize(container, properties.getDatasetProperties());
            schema = schemaFetcher.getSchema();
        }

        Object pipelineOptionsObj = container.getGlobalData(BeamJobRuntimeContainer.PIPELINE_OPTIONS);
        if (pipelineOptionsObj != null) {
            PipelineOptions pipelineOptions = (PipelineOptions) pipelineOptionsObj;
            GcpServiceAccountOptions gcpOptions = pipelineOptions.as(GcpServiceAccountOptions.class);
            if (!"DataflowRunner".equals(gcpOptions.getRunner().getSimpleName())) {
                // when using Dataflow runner, these properties has been set on pipeline level
                gcpOptions.setProject(datastore.projectName.getValue());
                gcpOptions.setTempLocation(datastore.tempGsFolder.getValue());
                gcpOptions.setCredentialFactoryClass(ServiceAccountCredentialFactory.class);
                gcpOptions.setServiceAccountFile(datastore.serviceAccountFile.getValue());
                gcpOptions.setGcpCredential(BigQueryConnection.createCredentials(datastore));
            }
        }

        this.defaultOutputCoder = AvroCoder.of(schema);

        return ValidationResult.OK;
    }

    @Override
    public PCollection<IndexedRecord> expand(PBegin in) {
        BigQueryIO.TypedRead<TableRow> bigQueryIOPTransform;
        switch (dataset.sourceType.getValue()) {
        case TABLE_NAME: {
            TableReference table = new TableReference();
            table.setProjectId(datastore.projectName.getValue());
            table.setDatasetId(dataset.bqDataset.getValue());
            table.setTableId(dataset.tableName.getValue());
            bigQueryIOPTransform = BigQueryIO.readTableRows().from(table);
            break;
        }
        case QUERY: {
            bigQueryIOPTransform = BigQueryIO.readTableRows().fromQuery(dataset.query.getValue());
            if (!dataset.useLegacySql.getValue()) {
                bigQueryIOPTransform = bigQueryIOPTransform.usingStandardSql();
            } else {
                // flattenResults only for legacy sql, and we do not support flattenResults till now.
                bigQueryIOPTransform = bigQueryIOPTransform.withoutResultFlattening();
            }
            break;
        }
        default:
            throw new RuntimeException("To be implemented: " + dataset.sourceType.getValue());
        }

        return in
                .apply(bigQueryIOPTransform)
                .apply(ParDo.of(new TableRowToIndexedRecordFn(defaultOutputCoder.getSchema())))
                .setCoder(defaultOutputCoder);
    }

    public static class TableRowToIndexedRecordFn extends DoFn<TableRow, IndexedRecord> {

        private transient IndexedRecordConverter<TableRow, IndexedRecord> converter;

        private String schemaStr;

        public TableRowToIndexedRecordFn(Schema schema) {
            schemaStr = schema.toString();
        }

        @DoFn.ProcessElement
        public void processElement(ProcessContext c) throws IOException {
            TableRow row = c.element();
            if (row == null) {
                return;
            }
            if (converter == null) {
                converter = new BigQueryTableRowIndexedRecordConverter();
                converter.setSchema(new Schema.Parser().parse(schemaStr));
            }
            c.output(converter.convertToAvro(row));
        }
    }
}
