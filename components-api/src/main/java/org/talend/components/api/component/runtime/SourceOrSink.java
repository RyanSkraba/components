package org.talend.components.api.component.runtime;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;

/**
 * Base interface for defining properties and common methods necessary to do I/O operations; defines methods common to
 * {@link Source} or {@link Sink}.
 *
 * <p>
 * A {@code SourceOrSink} may be passed between partititions for distributed operation and therefore must be {@code Serializable}.
 * This allows the {@code SourceOrSink} instance created in this "main program" to be sent (in serialized form) to remote worker
 * machines and reconstituted for each batch of elements being processed. A {@code SourceOrSink} can have instance variable state,
 * and non-transient instance variable state will be serialized in the main program and then deserialized on remote worker
 * machines.
 *
 * <p>
 * {@code SourceOrSink} implementations MUST be effectively immutable. The only acceptable use of mutable fields is to cache the
 * results of expensive operations, and such fields MUST be marked {@code transient}.
 *
 * <p>
 * {@code SourceOrSink} implementations should override {@link Object#toString}, as it will be used in important error and
 * debugging messages.
 *
 */
public interface SourceOrSink extends Serializable {

    /**
     * Initialize based on the specified properties. This will typically store the {@link ComponentProperties} in the
     * object.
     */
    void initialize(RuntimeContainer container, ComponentProperties properties);

    /**
     * Checks that this source or sink is valid, before it can be used. This will typically make a connection and return
     * the results of the connection establishment.
     */
    ValidationResult validate(RuntimeContainer container);

    /**
     * Get the list of schema names available for this {@code SourceOrSink} or an empty List if none.
     *
     * <p>
     * This uses the {@link ComponentProperties} previously specified to make any necessary connection and then gets the schema
     * names directly from the target of this component. It is not intended to update any associated {@code ComponentProperties}
     * object.
     */
    List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException;

    /**
     * Return the schema associated with the specified schema name for this {@code SourceOrSink}, or null if none.
     *
     * Works in a similar manner to {@link #getSchemaNames(RuntimeContainer)}.
     */
    Schema getEndpointSchema(RuntimeContainer container, String schemaName) throws IOException;

}
