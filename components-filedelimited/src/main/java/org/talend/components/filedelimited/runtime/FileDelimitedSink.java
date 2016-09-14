package org.talend.components.filedelimited.runtime;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.daikon.NamedThing;

public class FileDelimitedSink extends FileSourceOrSink implements Sink {

    @Override
    public WriteOperation<?> createWriteOperation() {
        return null;
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer runtimeContainer) throws IOException {
        return Collections.emptyList();
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer runtimeContainer, String s) throws IOException {
        return null;
    }
}
