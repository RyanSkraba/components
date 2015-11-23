package org.talend.components.api.runtime;

import java.util.Date;
import java.util.Map;

/**
 * The container that's running the component provides this implementation.
 */
public interface ComponentRuntimeContainer {

    // DI global map
    public Map<String, Object> getGlobalMap();

    public String formatDate(Date date, String pattern);

    /**
     * Creates a {@link ComponentDynamicHolder} object.
     */
    public ComponentDynamicHolder createDynamicHolder();
}
