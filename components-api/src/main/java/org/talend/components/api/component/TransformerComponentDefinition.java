package org.talend.components.api.component;

/**
 * Definition of a component that's transformer (sits in the middle).
 */
public interface TransformerComponentDefinition {

    /**
     * Returns the runtime object to execute the transformer. The specific type of transformer runtime is not
     * specified in code in this framework, but is assumed to be something executable in an Apache Beam
     * environment.
     */
    <RuntimeT> RuntimeT getRuntime();
}
