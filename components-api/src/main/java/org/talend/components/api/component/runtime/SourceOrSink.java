package org.talend.components.api.component.runtime;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.schema.Schema;

import java.util.List;

public interface SourceOrSink {

    /**
     * Initialize based on the specified properties.
     */
    public void initialize(ComponentProperties properties);

    /**
     * Checks that this source is valid, before it can be used in a pipeline.
     */
    public ValidationResult validate();

    /**
     * Get the list of schema names available for this {@code Source}.
     */
    public List<NamedThing> getSchemaNames() throws Exception;

    /**
     * Return the schema associated with the specified schema name for this {@code Source}
     */
    public Schema getSchema(String schemaName) throws Exception;


}
