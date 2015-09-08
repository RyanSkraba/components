package org.talend.components.api;

import org.talend.components.api.internal.ComponentSchemaElementImpl;

/**
 *
 */
public abstract class ComponentRuntime {

    public ComponentSchemaElement getComponentSchemaElement()
    {
        return new ComponentSchemaElementImpl();
    }




}
