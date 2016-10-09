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
import org.talend.components.common.ComponentConstants;
import org.talend.components.filedelimited.tfileinputdelimited.TFileInputDelimitedProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

public abstract class FileDelimitedReader extends AbstractBoundedReader<IndexedRecord> {

    private static final long serialVersionUID = 1L;

    protected RuntimeContainer container;

    protected transient IndexedRecord currentIndexRecord;

    protected transient Schema schema;

    private IndexedRecordConverter factory;

    protected FileInputDelimitedRuntime inputRuntime;

    TFileInputDelimitedProperties properties;

    protected String[] values;

    // TODO check reject for input
    protected final List<IndexedRecord> successfulWrites = new ArrayList<>();

    protected final List<IndexedRecord> rejectedWrites = new ArrayList<>();

    protected int dataCount;

    public FileDelimitedReader(RuntimeContainer container, BoundedSource source, TFileInputDelimitedProperties properties) {
        super(source);
        this.container = container;
        this.properties = properties;
        schema = properties.main.schema.getValue();
        schema.addProp(ComponentConstants.DIE_ON_ERROR, properties.dieOnError.getStringValue());
        schema.addProp(ComponentConstants.CHECK_DATE, properties.checkDate.getStringValue());
        setupDecodeProps(schema, properties);
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
            factory.setSchema(schema);
        }
        return factory;
    }

    /**
     * Add decode property for field base on the decode table
     */
    protected void setupDecodeProps(Schema schema, TFileInputDelimitedProperties properties) {
        if (schema != null && properties != null && properties.enableDecode.getValue()) {
            List<Boolean> decodeSelect = properties.decodeTable.decode.getValue();
            for (Schema.Field field : schema.getFields()) {
                field.addProp(ComponentConstants.NUMBER_DECODE,
                        String.valueOf(decodeSelect != null && decodeSelect.get(field.pos())));
            }
        }
    }

}
