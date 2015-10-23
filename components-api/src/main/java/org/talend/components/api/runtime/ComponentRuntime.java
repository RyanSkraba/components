package org.talend.components.api.runtime;

import java.util.List;
import java.util.Map;

import org.talend.components.api.properties.ComponentProperties;

/**
 * Code to execute the component's runtime. This can be used at runtime or design time as required.
 */
public abstract class ComponentRuntime {

    public abstract void setContainer(ComponentRuntimeContainer container);

    public abstract void inputBegin(ComponentProperties props) throws Exception;

    /**
     * Returns a row from the input.
     *
     * {@link #inputBegin(ComponentProperties)} must be called first.
     * 
     * @return a {@link Map} of the row, null if there are no more rows
     * @throws Exception
     */
    public abstract Map<String, Object> inputRow() throws Exception;

    public abstract void inputEnd() throws Exception;

    public abstract void outputBegin(ComponentProperties props) throws Exception;

    public abstract void outputMain(Map<String, Object> row) throws Exception;

    public abstract void outputEnd() throws Exception;

}
