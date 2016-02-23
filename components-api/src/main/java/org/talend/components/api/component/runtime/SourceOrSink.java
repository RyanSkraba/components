package org.talend.components.api.component.runtime;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.talend.components.api.adaptor.Adaptor;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.schema.Schema;

public interface SourceOrSink extends Serializable {

    /**
     * Initialize based on the specified properties.
     */
    public void initialize(Adaptor adaptor, ComponentProperties properties);

    /**
     * Checks that this source is valid, before it can be used in a pipeline.
     */
    public ValidationResult validate(Adaptor adaptor);

    /**
     * Get the list of schema names available for this {@code Source}.
     */
    public List<NamedThing> getSchemaNames(Adaptor adaptor) throws IOException;

    /**
     * Return the schema associated with the specified schema name for this {@code Source}
     */
    public Schema getSchema(Adaptor adaptor, String schemaName) throws IOException;

}
