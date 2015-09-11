package org.talend.components.api.schema;

import org.talend.components.api.schema.internal.ComponentSchemaElementImpl;

/**
 * Gets {@link ComponentSchemaElement} objects.
 */
public class ComponentSchemaFactory {

    public static ComponentSchemaElement getComponentSchemaElement(String name) {
        return new ComponentSchemaElementImpl(name);
    }
}
