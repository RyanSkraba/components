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
package org.talend.components.filedelimited.runtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.filedelimited.FileDelimitedProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

public abstract class FileDelimitedReader extends AbstractBoundedReader<IndexedRecord> {

    private static final long serialVersionUID = 1L;

    protected RuntimeContainer container;

    protected transient IndexedRecord currentIndexRecord;

    protected transient Schema schema;

    private IndexedRecordConverter factory;

    protected FileInputDelimitedRuntime inputRuntime;

    FileDelimitedProperties properties;

    protected String[] values;

    // TODO check reject for input
    protected final List<IndexedRecord> successfulWrites = new ArrayList<>();

    protected final List<IndexedRecord> rejectedWrites = new ArrayList<>();

    protected int dataCount;

    public FileDelimitedReader(RuntimeContainer container, BoundedSource source, FileDelimitedProperties properties) {
        super(source);
        this.container = container;
        this.properties = properties;
        schema = properties.main.schema.getValue();
        inputRuntime = new FileInputDelimitedRuntime(properties);

    }

    @Override
    public IndexedRecord getCurrent() {
        return currentIndexRecord;
    }

    abstract void retrieveValues() throws IOException;

    @Override
    public Map<String, Object> getReturnValues() {
        Result result = new Result();
        result.totalCount = dataCount;
        return result.toMap();
    }

    protected void setupDynamicSchema() throws IOException {
        if (schema != null) {
            if (AvroUtils.isIncludeAllFields(schema)) {
                schema = FileSourceOrSink.getDynamicSchema(values, "dynamicSchema", schema);
            }
        } else {
            throw new IOException("Schema is not setup!");
        }
    }

    protected IndexedRecordConverter getFactory() throws IOException {
        if (factory == null) {
            factory = new DelimitedAdaptorFactory();
            ((DelimitedAdaptorFactory) factory).setProperties(properties);
            factory.setSchema(schema);
        }
        return factory;
    }

}
