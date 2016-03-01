package org.talend.components.api.component;

import org.talend.components.api.component.runtime.Sink;

/**
 * Created by bchen on 16-2-26.
 */
public interface OutputComponentDefinition {
    public Sink getRuntime();
}
