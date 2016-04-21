package org.talend.components.api.component;

import org.talend.components.api.component.runtime.Source;

/**
 * Used for components that read data.
 */
public interface InputComponentDefinition extends EndpointComponentDefinition {

    /**
     * Returns the runtime {@link Source} for input components.
     */
    public Source getRuntime();
}
