package org.talend.components.api.runtime;

import java.util.Map;

/**
 * The context values for the local environment that's running the component.
 */
public interface ComponentEnvironmentContext {

    // DI global map
    public Map<String, Object> getGlobalMap();

}
