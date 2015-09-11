package org.talend.components.api.schema;

import org.talend.components.api.schema.internal.ComponentSchemaElementImpl;
import org.talend.components.api.schema.internal.ComponentSchemaImpl;

/**
 * Make objects that are related to the component schema.
 */
public class ComponentSchemaFactory {

    public static ComponentSchema getComponentSchema() {
        return new ComponentSchemaImpl();
    }

    public static ComponentSchemaElement getComponentSchemaElement(String name) {
        return new ComponentSchemaElementImpl(name);
    }
}
