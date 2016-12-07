// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.components.jdbc.runtime.beam;

import java.sql.ResultSet;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.io.jdbc.JdbcIO;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.talend.components.adapter.beam.LazyAvroCoder;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.avro.JDBCResultSetIndexedRecordConverter;
import org.talend.components.jdbc.dataprep.JDBCInputProperties;
import org.talend.daikon.properties.ValidationResult;

public class JDBCInputPTransformRuntime extends PTransform<PBegin, PCollection<IndexedRecord>>
        implements RuntimableRuntime<JDBCInputProperties> {

    private JDBCInputProperties properties;

    @Override
    public PCollection<IndexedRecord> apply(PBegin pBegin) {
        return (PCollection<IndexedRecord>) pBegin.apply(JdbcIO.<IndexedRecord> read()
                .withDataSourceConfiguration(
                        JdbcIO.DataSourceConfiguration
                                .create(properties.getDatasetProperties().getDatastoreProperties().getCurrentDriverClass(),
                                        properties.getDatasetProperties().getDatastoreProperties().jdbcUrl.getValue())
                                .withUsername(
                                        properties.getDatasetProperties().getDatastoreProperties().userId.getValue())
                                .withPassword(properties.getDatasetProperties().getDatastoreProperties().password
                                        .getValue()))
                .withQuery(properties.getDatasetProperties().getSql()).withRowMapper(new JdbcIO.RowMapper<IndexedRecord>() {

                    @Override
                    public IndexedRecord mapRow(ResultSet resultSet) throws Exception {
                        JDBCResultSetIndexedRecordConverter factory = new JDBCResultSetIndexedRecordConverter();
                        factory.setSchema(properties.getDatasetProperties().main.schema.getValue());
                        return factory.convertToAvro(resultSet);
                    }
                }).withCoder(getDefaultOutputCoder()));
    }

    @Override
    public Coder getDefaultOutputCoder() {
        return LazyAvroCoder.of();
    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, JDBCInputProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }

}
