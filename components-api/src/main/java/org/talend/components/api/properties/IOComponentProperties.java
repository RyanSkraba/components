package org.talend.components.api.properties;

public abstract class IOComponentProperties extends ComponentProperties {
    /**
     * named constructor to be used is these properties are nested in other properties. Do not subclass this method for
     * initialization, use {@link #init()} instead.
     *
     * @param name
     */
    public IOComponentProperties(String name) {
        super(name);
    }

    /**
     * to indicate if this component can resolve schema when there is no schema provided on runtime
     *
     * @return
     */
    public abstract boolean supportEmptySchema();
}
