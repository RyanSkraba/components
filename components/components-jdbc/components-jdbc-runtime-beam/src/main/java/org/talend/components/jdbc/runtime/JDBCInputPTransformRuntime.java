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

package org.talend.components.jdbc.runtime;

import java.sql.ResultSet;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.io.jdbc.JdbcIO;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.jdbc.avro.ResultSetStringRecordConverter;
import org.talend.components.jdbc.dataprep.JDBCInputProperties;
import org.talend.components.jdbc.runtime.dataprep.JDBCDatasetRuntime;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.converter.IndexedRecordConverter;
import org.talend.daikon.properties.ValidationResult;

public class JDBCInputPTransformRuntime extends PTransform<PBegin, PCollection<IndexedRecord>> implements
        RuntimableRuntime<JDBCInputProperties> {

    private static final int FETCH_SIZE = 1000;
    private JDBCInputProperties properties;

    private transient IndexedRecordConverter<ResultSet, IndexedRecord> factory;

    private AvroCoder<?> defaultOutputCoder;

    @Override
    public PCollection<IndexedRecord> expand(PBegin pBegin) {
        return (PCollection<IndexedRecord>) pBegin.apply(JdbcIO
                .<IndexedRecord> read()
                .withDataSourceConfiguration(
                        JdbcIO.DataSourceConfiguration
                                .create(properties.getDatasetProperties().getDatastoreProperties().getCurrentDriverClass(),
                                        properties.getDatasetProperties().getDatastoreProperties().jdbcUrl.getValue())
                                .withUsername(properties.getDatasetProperties().getDatastoreProperties().userId.getValue())
                                .withPassword(properties.getDatasetProperties().getDatastoreProperties().password.getValue()))
                .withFetchSize(FETCH_SIZE)
                .withQuery(properties.getDatasetProperties().getSql()).withRowMapper(new JdbcIO.RowMapper<IndexedRecord>() {

                    @Override
                    public IndexedRecord mapRow(ResultSet resultSet) throws Exception {
                        if (factory == null) {
                            factory = new ResultSetStringRecordConverter();
                            factory.setSchema(defaultOutputCoder.getSchema());
                        }
                        return factory.convertToAvro(resultSet);
                    }
                }).withCoder(getDefaultOutputCoder()));
    }

    @Override
    public Coder getDefaultOutputCoder() {
        return defaultOutputCoder;
    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, JDBCInputProperties properties) {
        this.properties = properties;

        // In Beam, JdbcIO always has a repartition event, so we are obligated to fetch the schema before any processing
        // occurs in the nodes.
        Schema schema = properties.getDatasetProperties().main.schema.getValue();
        if (schema == null || AvroUtils.isSchemaEmpty(schema) || AvroUtils.isIncludeAllFields(schema)) {
            JDBCDatasetRuntime schemaFetcher = new JDBCDatasetRuntime();
            schemaFetcher.initialize(container, properties.getDatasetProperties());
            schema = schemaFetcher.getSchema();
        }

        this.defaultOutputCoder = AvroCoder.of(schema);

        return ValidationResult.OK;
    }

}
