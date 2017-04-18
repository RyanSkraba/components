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
package org.talend.components.jdbc.runtime.dataprep;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.ReaderDataProvider;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.dataset.runtime.DatasetRuntime;
import org.talend.components.jdbc.dataset.JDBCDatasetProperties;
import org.talend.components.jdbc.runtime.JDBCSource;
import org.talend.components.jdbc.runtime.JDBCSourceOrSink;
import org.talend.components.jdbc.runtime.reader.JDBCInputReader;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.java8.Consumer;
import org.talend.daikon.properties.ValidationResult;

/**
 * the data set runtime for database
 *
 */
public class JDBCDatasetRuntime implements DatasetRuntime<JDBCDatasetProperties> {

    /**
     * 
     */
    private static final long serialVersionUID = 5829335010543623248L;

    private JDBCDatasetProperties dataset;

    private RuntimeContainer container;

    @Override
    public ValidationResult initialize(RuntimeContainer container, JDBCDatasetProperties properties) {
        this.dataset = properties;
        /*
         * This data set have to be in a read-only mode for dataprep
         * to deny queries that try to alter data or schemas
         * this will allow only read statement to be executed with the JDBC connection
         * this is also a hint to the driver to enable database optimizations
         */
        this.dataset.getRuntimeSetting().setReadOnly(true);
        this.container = container;
        return ValidationResult.OK;
    }

    @Override
    public Schema getSchema() {
        JDBCSourceOrSink jss = new JDBCSourceOrSink();
        jss.initialize(container, dataset);
        return jss.getSchemaFromQuery(container, dataset.getRuntimeSetting().getSql());
    }

    private void throwExceptionIfValidationResultIsError(ValidationResult validationResult) {
        if (validationResult == null) {
            return;
        }

        if (validationResult.getStatus() == ValidationResult.Result.ERROR) {
            throw TalendRuntimeException.createUnexpectedException(validationResult.getMessage());
        }
    }

    @Override
    public void getSample(int limit, Consumer<IndexedRecord> consumer) {
        JDBCSource js = new JDBCSource();
        throwExceptionIfValidationResultIsError(js.initialize(container, dataset));
        throwExceptionIfValidationResultIsError(js.validate(container));

        JDBCInputReader reader = (JDBCInputReader) js.createReader(container);
        ReaderDataProvider<IndexedRecord> readerDataProvider = new ReaderDataProvider<>(reader, limit, consumer);
        readerDataProvider.retrieveData();
    }

}
