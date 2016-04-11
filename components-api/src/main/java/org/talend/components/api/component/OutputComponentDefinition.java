package org.talend.components.api.component;

import org.talend.components.api.component.runtime.Sink;

public interface OutputComponentDefinition extends EndpointComponentDefinition {

    /**
     * Returns the runtime {@link Sink} for output components.
     */
    public Sink getRuntime();
}
