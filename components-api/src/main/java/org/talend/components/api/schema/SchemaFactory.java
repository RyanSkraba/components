package org.talend.components.api.schema;

import org.talend.components.api.schema.internal.SchemaElementImpl;
import org.talend.components.api.schema.internal.SchemaImpl;

/**
 * Make objects that are related to the component schema.
 */
public class SchemaFactory {

    public static Schema newSchema() {
        return new SchemaImpl();
    }

    public static SchemaElement newSchemaElement(String name) {
        return new SchemaElementImpl(name);
    }

    public static SchemaElement newSchemaElement(String name, String title) {
        return new SchemaElementImpl(name, title);
    }

    public static SchemaElement newSchemaElement(SchemaElement.Type type, String name, String title) {
        return new SchemaElementImpl(type, name, title);
    }
}
