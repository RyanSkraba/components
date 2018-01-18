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
package org.talend.components.marklogic.data;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.api.component.runtime.ReaderDataProvider;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.dataset.runtime.DatasetRuntime;
import org.talend.daikon.java8.Consumer;
import org.talend.daikon.properties.ValidationResult;

/**
 *
 *
 */
public class MarkLogicDatasetRuntime implements DatasetRuntime<MarkLogicDatasetProperties> {

    private MarkLogicDatasetProperties dataset;

    private RuntimeContainer container;

    @Override
    public ValidationResult initialize(RuntimeContainer container, MarkLogicDatasetProperties properties) {
        this.dataset = properties;
        this.container = container;
        // Need to initialize Schema before using it.
        dataset.setupSchema();
        return ValidationResult.OK;
    }

    @Override
    public Schema getSchema() {
        return dataset.main.schema.getValue();
    }

    @Override
    public void getSample(int limit, Consumer<IndexedRecord> consumer) {
        MarkLogicDataSource dataSource = new MarkLogicDataSource();
        dataSource.initialize(container, dataset);
        dataSource.validate(container);
        Reader<IndexedRecord> reader = dataSource.createReader(container);
        ReaderDataProvider<IndexedRecord> dataProvider = new ReaderDataProvider<>(reader, limit, consumer);
        dataProvider.retrieveData();
    }

}
