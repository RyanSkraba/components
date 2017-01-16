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
package org.talend.components.api.component.runtime;

import java.io.IOException;
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
 * {@code SourceOrSink} implementations MUST be effectively immutable. The only acceptable use of mutable fields is to
 * cache the results of expensive operations, and such fields MUST be marked {@code transient}.
 *
 * <p>
 * {@code SourceOrSink} implementations should override {@link Object#toString}, as it will be used in important error
 * and debugging messages.
 *
 */
public interface SourceOrSink extends RuntimableRuntime<ComponentProperties> {

    /**
     * Get the list of schema names available for this {@code SourceOrSink} or an empty List if none.
     *
     * <p>
     * This uses the {@link ComponentProperties} previously specified to make any necessary connection and then gets the
     * schema names directly from the target of this component. It is not intended to update any associated
     * {@code ComponentProperties} object.
     */
    List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException;

    /**
     * Return the schema associated with the specified schema name for this {@code SourceOrSink}, or null if none.
     *
     * Works in a similar manner to {@link #getSchemaNames(RuntimeContainer)}.
     */
    Schema getEndpointSchema(RuntimeContainer container, String schemaName) throws IOException;

    /**
     * Checks that this source or sink is valid, before it can be used. This will typically make a connection and return
     * the results of the connection establishment. This method will be called in the same process where the runtime
     * will actually be executed.
     */
    ValidationResult validate(RuntimeContainer container);
}
