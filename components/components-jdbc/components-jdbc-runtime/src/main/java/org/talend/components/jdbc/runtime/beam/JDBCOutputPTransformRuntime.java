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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.PreparedStatement;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.io.jdbc.JdbcIO;
import org.apache.beam.sdk.io.jdbc.JdbcIO.PreparedStatementSetter;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.jdbc.dataprep.JDBCInputProperties;
import org.talend.components.jdbc.dataset.JDBCDatasetProperties;
import org.talend.components.jdbc.datastore.JDBCDatastoreProperties;
import org.talend.components.jdbc.datastream.JDBCOutputProperties;
import org.talend.components.jdbc.runtime.setting.JDBCSQLBuilder;
import org.talend.components.jdbc.runtime.type.JDBCMapping;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;

public class JDBCOutputPTransformRuntime extends PTransform<PCollection<IndexedRecord>, PDone>
        implements RuntimableRuntime<JDBCOutputProperties> {

    private JDBCOutputProperties properties;

    @Override
    public PDone expand(PCollection<IndexedRecord> input) {
        return input.apply(JdbcIO.<IndexedRecord> write().withDataSourceConfiguration(JdbcIO.DataSourceConfiguration
                .create(properties.getDatasetProperties().getDatastoreProperties().getCurrentDriverClass(),
                        properties.getDatasetProperties().getDatastoreProperties().jdbcUrl.getValue())
                .withUsername(properties.getDatasetProperties().getDatastoreProperties().userId.getValue())
                .withPassword(properties.getDatasetProperties().getDatastoreProperties().password.getValue()))
                .withStatement(
                        JDBCSQLBuilder.getInstance().generateSQL4Insert(properties.getDatasetProperties().tableName.getValue(),
                                properties.getDatasetProperties().main.schema.getValue()))
                .withPreparedStatementSetter(new PreparedStatementSetter<IndexedRecord>() {

                    @Override
                    public void setParameters(IndexedRecord indexedRecord, PreparedStatement preparedStatement) throws Exception {
                        List<Schema.Field> fields = indexedRecord.getSchema().getFields();
                        int index = 0;
                        for (Schema.Field f : fields) {
                            JDBCMapping.setValue(++index, preparedStatement, f, indexedRecord.get(f.pos()));
                        }
                    }
                }));
    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, JDBCOutputProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }

}
