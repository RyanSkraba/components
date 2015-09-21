package org.talend.components.api.runtime;

import java.util.List;
import java.util.Map;

import org.talend.components.api.properties.ComponentProperties;

/**
 * Code to execute the component's runtime. This can be used at runtime or design time as required.
 */
public abstract class ComponentRuntime {

    public abstract void inputBegin(ComponentProperties props, ComponentRuntimeContainer container,
            List<Map<String, Object>> values) throws Exception;

    public abstract void inputEnd(ComponentProperties props, ComponentRuntimeContainer container, List<Map<String, Object>> values)
            throws Exception;

    public abstract void outputBegin(ComponentProperties props, ComponentRuntimeContainer env);

    public abstract void outputMain(ComponentProperties props, ComponentRuntimeContainer env, List<Map<String, Object>> rows)
            throws Exception;

    public abstract void outputEnd(ComponentProperties props, ComponentRuntimeContainer env) throws Exception;

}
