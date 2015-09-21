package org.talend.components.api.schema;

import org.talend.components.api.properties.internal.Property;
import org.talend.components.api.schema.internal.SchemaImpl;

/**
 * Make objects that are related to the component schema.
 */
public class SchemaFactory {

    public static Schema newSchema() {
        return new SchemaImpl();
    }

    public static SchemaElement newProperty(String name) {
        return new Property(name);
    }

    public static SchemaElement newProperty(String name, String title) {
        return new Property(name, title);
    }

    public static SchemaElement newProperty(SchemaElement.Type type, String name, String title) {
        return new Property(type, name, title);
    }

    public static SchemaElement newProperty(SchemaElement.Type type, String name) {
        return new Property(type, name);
    }
}
