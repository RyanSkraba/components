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
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.adapter.beam.BeamJobRuntimeContainer;
import org.talend.components.adapter.beam.gcp.GcpServiceAccountOptions;
import org.talend.components.adapter.beam.gcp.ServiceAccountCredentialFactory;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.bigquery.BigQueryDatasetProperties;
import org.talend.components.bigquery.BigQueryDatastoreProperties;
import org.talend.components.bigquery.output.BigQueryOutputProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.converter.IndexedRecordConverter;
import org.talend.daikon.properties.ValidationResult;

import com.google.api.services.bigquery.model.TableReference;
import com.google.api.services.bigquery.model.TableRow;
import com.google.api.services.bigquery.model.TableSchema;

public class BigQueryOutputRuntime extends PTransform<PCollection<IndexedRecord>, PDone>
        implements RuntimableRuntime<BigQueryOutputProperties> {

    private static Logger LOG = LoggerFactory.getLogger(BigQueryOutputRuntime.class);

    /**
     * The component instance that this runtime is configured for.
     */
    private BigQueryOutputProperties properties;

    private BigQueryDatasetProperties dataset;

    private BigQueryDatastoreProperties datastore;

    @Override
    public ValidationResult initialize(RuntimeContainer container, BigQueryOutputProperties properties) {
        this.properties = properties;
        this.dataset = properties.getDatasetProperties();
        this.datastore = dataset.getDatastoreProperties();

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

        return ValidationResult.OK;
    }

    @Override
    public PDone expand(PCollection<IndexedRecord> in) {
        TableReference table = new TableReference();
        table.setProjectId(datastore.projectName.getValue());
        table.setDatasetId(dataset.bqDataset.getValue());
        table.setTableId(dataset.tableName.getValue());

        BigQueryIO.Write bigQueryIOPTransform = BigQueryIO.writeTableRows().to(table);

        bigQueryIOPTransform = setTableOperation(bigQueryIOPTransform);
        bigQueryIOPTransform = setWriteOperation(bigQueryIOPTransform);

        in.apply(ParDo.of(new IndexedRecordToTableRowFn())).apply(bigQueryIOPTransform);
        return PDone.in(in.getPipeline());
    }

    private BigQueryIO.Write setTableOperation(BigQueryIO.Write bigQueryIOPTransform) {
        TableSchema bqSchema = null;
        if (properties.tableOperation.getValue() == BigQueryOutputProperties.TableOperation.CREATE_IF_NOT_EXISTS
                || properties.tableOperation.getValue() == BigQueryOutputProperties.TableOperation.DROP_IF_EXISTS_AND_CREATE) {
            Schema designSchema = properties.getDatasetProperties().main.schema.getValue();
            if (designSchema != null && !AvroUtils.isSchemaEmpty(designSchema) && !AvroUtils.isIncludeAllFields(designSchema)) {
                bqSchema = BigQueryAvroRegistry.get().guessBigQuerySchema(designSchema);
            }
            if (bqSchema == null) {
                throw new RuntimeException("Need to specify schema to create table");
            }
        }

        switch (properties.tableOperation.getValue()) {
        case NONE:
            bigQueryIOPTransform = bigQueryIOPTransform.withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_NEVER);
            break;
        case CREATE_IF_NOT_EXISTS:
            bigQueryIOPTransform = bigQueryIOPTransform.withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_IF_NEEDED)
                    .withSchema(bqSchema);
            break;
        case DROP_IF_EXISTS_AND_CREATE:
            bigQueryIOPTransform = bigQueryIOPTransform.withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_TRUNCATE)
                    .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_IF_NEEDED).withSchema(bqSchema);
            break;
        case TRUNCATE:
            bigQueryIOPTransform = bigQueryIOPTransform.withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_TRUNCATE)
                    .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_NEVER);
            break;
        default:
            throw new RuntimeException("To be implemented: " + properties.tableOperation.getValue());
        }
        return bigQueryIOPTransform;
    }

    private BigQueryIO.Write setWriteOperation(BigQueryIO.Write bigQueryIOPTransform) {
        if (properties.tableOperation.getValue() == BigQueryOutputProperties.TableOperation.NONE
                || properties.tableOperation.getValue() == BigQueryOutputProperties.TableOperation.CREATE_IF_NOT_EXISTS) {
            switch (properties.writeOperation.getValue()) {
            case APPEND:
                bigQueryIOPTransform = bigQueryIOPTransform.withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND);
                break;
            case WRITE_TO_EMPTY:
                bigQueryIOPTransform = bigQueryIOPTransform.withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_EMPTY);
                break;
            default:
                throw new RuntimeException("To be implemented: " + properties.writeOperation.getValue());
            }
        } else {
            if (properties.writeOperation.getValue() != null) {
                LOG.info("Write operation " + properties.writeOperation.getValue() + " be ignored when Table operation is "
                        + properties.tableOperation.getValue());
            }
        }
        return bigQueryIOPTransform;
    }

    public static class IndexedRecordToTableRowFn extends DoFn<IndexedRecord, TableRow> {

        private transient IndexedRecordConverter<TableRow, IndexedRecord> converter;

        public IndexedRecordToTableRowFn() {
        }

        @DoFn.ProcessElement
        public void processElement(ProcessContext c) throws IOException {
            IndexedRecord row = c.element();
            if (row == null) {
                return;
            }
            if (converter == null) {
                converter = new BigQueryTableRowIndexedRecordConverter();
                if (!AvroUtils.isSchemaEmpty(row.getSchema()) && !AvroUtils.isIncludeAllFields(row.getSchema())) {
                    converter.setSchema(row.getSchema());
                }
            }
            c.output(converter.convertToDatum(row));
        }
    }

}
