package org.talend.components.marklogic.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.marklogic.connection.MarkLogicConnection;
import org.talend.components.marklogic.exceptions.MarkLogicException;
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

public class MarkLogicDataSource extends MarkLogicConnection implements BoundedSource {

    private MarkLogicDatasetProperties properties;

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException {
        return Collections.emptyList();
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer container, String schemaName) throws IOException {
        return null;
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        ValidationResult validationResult;
        try {
            connect(container);
            validationResult = ValidationResult.OK;
        } catch (MarkLogicException me) {
            validationResult = new ValidationResult(me);
        }
        return validationResult;
    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        if (properties instanceof MarkLogicDatasetProperties) {
            this.properties = (MarkLogicDatasetProperties) properties;
            return ValidationResult.OK;
        }
        return new ValidationResult(Result.ERROR);
    }

    @Override
    public List<? extends BoundedSource> splitIntoBundles(long desiredBundleSizeBytes, RuntimeContainer adaptor)
            throws Exception {
        List<BoundedSource> list = new ArrayList<>();
        list.add(this);
        return list;
    }

    @Override
    public long getEstimatedSizeBytes(RuntimeContainer adaptor) {
        return 0;
    }

    @Override
    public boolean producesSortedKeys(RuntimeContainer adaptor) {
        return false;
    }

    @Override
    public BoundedReader<IndexedRecord> createReader(RuntimeContainer adaptor) {
        return new MarkLogicInputReader(this, adaptor, properties);
    }

    @Override
    protected MarkLogicConnectionProperties getMarkLogicConnectionProperties() {
        return properties.getDatastoreProperties();
    }

}
