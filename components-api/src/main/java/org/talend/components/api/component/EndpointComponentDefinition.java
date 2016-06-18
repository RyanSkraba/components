package org.talend.components.api.component;

import org.talend.components.api.component.runtime.SourceOrSink;

/**
 * Definition of a component that's an endpoint. This may be an {@link InputComponentDefinition} or
 * {@link OutputComponentDefinition}, but it may also be just an endpoint (like a connection component).
 */
public interface EndpointComponentDefinition {

    /**
     * Returns the {@link SourceOrSink} if this is a runnable component but not either input or output (a connection for
     * example).
     */
    SourceOrSink getRuntime();
}
