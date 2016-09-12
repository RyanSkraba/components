package org.talend.components.filedelimited.runtime;

import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.filedelimited.tFileInputDelimited.TFileInputDelimitedProperties;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

public abstract class FileDelimitedReader extends AbstractBoundedReader<IndexedRecord> {

    private static final long serialVersionUID = 1L;

    protected RuntimeContainer container;

    protected transient IndexedRecord currentIndexRecord;

    protected transient Schema schema;

    protected IndexedRecordConverter factory;

    protected FileDelimitedRuntime fileDelimitedRuntime;

    TFileInputDelimitedProperties properties;

    protected String[] values;

    public FileDelimitedReader(RuntimeContainer container, BoundedSource source, TFileInputDelimitedProperties properties) {
        super(source);
        this.container = container;
        this.properties = properties;
        factory = new DelimitedAdaptorFactory();
        schema = properties.main.schema.getValue();
        factory.setSchema(schema);
        fileDelimitedRuntime = new FileDelimitedRuntime(properties);

    }

    @Override
    public IndexedRecord getCurrent() {
        return currentIndexRecord;
    }

    @Override
    public Map<String, Object> getReturnValues() {
        return new Result().toMap();
    }

}
