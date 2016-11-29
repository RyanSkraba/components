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
package org.talend.components.jdbc.runtime.dataprep;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.dataset.runtime.DatasetRuntime;
import org.talend.components.jdbc.dataset.JDBCDatasetProperties;
import org.talend.components.jdbc.runtime.JDBCSource;
import org.talend.components.jdbc.runtime.JDBCSourceOrSink;
import org.talend.components.jdbc.runtime.reader.JDBCInputReader;
import org.talend.daikon.properties.ValidationResult;

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
        this.container = container;
        return ValidationResult.OK;
    }

    @Override
    public Schema getSchema() {
        JDBCSourceOrSink jss = new JDBCSourceOrSink();
        jss.initialize(container, dataset);
        return jss.getSchemaFromQuery(container, dataset.getRuntimeSetting().getSql());
    }

    @Override
    public Iterable<? extends IndexedRecord> getSample(int limit) {
        JDBCSource js = new JDBCSource();
        throwExceptionIfValidationResultIsError(js.initialize(container, dataset));
        throwExceptionIfValidationResultIsError(js.validate(container));

        try (JDBCInputReader reader = (JDBCInputReader) js.createReader(container)) {
            int count = 0;
            List<IndexedRecord> result = new ArrayList<IndexedRecord>();

            boolean available = reader.start();
            for (; available && ((count++) < limit); available = reader.advance()) {
                result.add(reader.getCurrent());
            }

            return result;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void throwExceptionIfValidationResultIsError(ValidationResult validationResult) {
        if (validationResult == null) {
            return;
        }

        if (validationResult.getStatus() == ValidationResult.Result.ERROR) {
            throw new RuntimeException(validationResult.getMessage());
        }
    }

}
